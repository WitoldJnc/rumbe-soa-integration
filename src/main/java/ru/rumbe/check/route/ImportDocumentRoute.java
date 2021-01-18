package ru.rumbe.check.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.springframework.stereotype.Component;
import ru.rumbe.check.model.ResponseStatus;

import static ru.rumbe.check.route.ExternalRoutes.DIRECT_STORE_REQUEST;
import static ru.rumbe.check.route.ExternalRoutes.DIRECT_TRANSFER_REQUEST;


@Component
public class ImportDocumentRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {


        Namespaces ns = new Namespaces("doc", "http://www.rumbe.ru/internal/services/checkDocumentService/docs");
                ns.add("lc", "http://www.rumbe.ru/soa/lc/1_2");

        onException(Exception.class)
                .handled(true)
                .logHandled(true)
                .to("direct:errorHandle");

        from("direct:errorHandle")
                .setProperty("error", simple("${exception.message}"))
                .setProperty("stack", simple("${exception.stacktrace}"))
                .setProperty("routeId", simple("${routeId} Exception"))
                .choice()
                    .when(simple("${property.statusCode} == null && ${property.statusDetail} == null"))
                        .setProperty("statusCode", simple(ResponseStatus.SYSTEM_ERROR.getCode()))
                        .setProperty("statusDetail", simple(ResponseStatus.SYSTEM_ERROR.getDetail()))
                .log("exception:  ${body}")
                //todo log exception to kafka
                .to("xslt:transform/syncResponse.xsl");

        /**
         * роут входяшего сообщения по соапу. отправляет в промежуточный роут для фетча данных из вход. сообщ
         * todo добавить лог в кафку входящего сообщения
         * todo добавить прием сообщения из топика кафки в отдельный роут
         */
        from("cxf:bean:importDocumentService")
                .routeId("ImportDocumentService-SOAP")
                .convertBodyTo(String.class)
                .log("${body}")
                .to("direct:incomeDocumentProcess")
                .end();

        /**
         * нужен для того, чтобы не дублировать функционал обработки документа, пришедшего из кафки или соапа
         */
        from("direct:incomeDocumentProcess")
                .routeId("FetchData")
                //todo log to kafka income message
                .setProperty("income", bodyAs(String.class))
                .setProperty("guid", ns.xpath("//doc:header/doc:guid/text()", String.class))
                .setProperty("documentType", ns.xpath("//doc:header/doc:documentType/text()", String.class))
                .setProperty("documentName", ns.xpath("//doc:header/doc:documentName/text()", String.class))
                .setProperty("subSystem", ns.xpath("//doc:header/doc:subSystemFrom/text()", String.class))
                .setProperty("clientType", ns.xpath("//doc:header/doc:employmentType/text()", String.class))
                .setProperty("code", ns.xpath("//doc:header/doc:docParams/doc:param[@name='Code']/@value", String.class))
                .to("direct:checkDocumentMain")
                .end();

        /**
         * главный роут проверки документа.
         * 1. отправить синхронный ответ о статусе обработки
         * 2. свалидировать входящий прикрепленный xml документ по xsd
         * 3. в зависимости от типа документа, отправить на соотв. обработку
         */
        from("direct:checkDocumentMain")
                .routeId("CheckDocumentMain")
                .to("direct:sendSyncResponse")
                .setBody(exchangeProperty("income"))
                .setProperty("document", ns.xpath("//doc:rumbeDocument/*[1]"))
                .to("direct:validateDocument")
                .to("direct:billLogicMain")
                .end();

        /**
         * отправка синхронного ответа о статусе обработки
         */
        from("direct:sendSyncResponse")
                .routeId("send sync")
                .setProperty("statusCode", simple(ResponseStatus.IN_PROGRESS.getCode()))
                .setProperty("statusDetail", simple(ResponseStatus.IN_PROGRESS.getDetail()))
                .to("xslt:transform/syncResponse.xsl")
                .removeProperty("statusCode")
                .removeProperty("statusDetail");

        /**
         * валидация входящего xml документа по xsd схеме
         * todo было бы неплохо, если схемы и xsl для документов лежали в облаке/kv консула для изменения функционала не трогая код
         */
        from("direct:validateDocument")
                .routeId("Validation")
                .process(exchange -> {
                    String documentType = exchange.getProperty("documentType", String.class);
                    String typeForPath = documentType.substring(0, documentType.indexOf("_"));
                    exchange.setProperty("billType", typeForPath);
                })
                .setProperty("validationPath", simple("document/${property.billType}/${property.subSystem}/" +
                        "${property.clientType}/${property.documentName}_validation.xsd?useSharedSchema=false"))
                .setBody(exchangeProperty("document"))
                .convertBodyTo(String.class)
                .doTry()
                    .toD("validator:${property.validationPath}")
                    .log("${property.guid} : validation success")
                //todo log to kafka validation status
                .doCatch(Exception.class)
                    .setProperty("statusCode", simple(ResponseStatus.VALIDATION_ERROR.getCode()))
                    .setProperty("statusDetail", simple(ResponseStatus.VALIDATION_ERROR.getDetail()))
                    .to("direct:errorHandle")
                .end();

        /**
         * 1. принять входящий отвалидированный документ
         * 2. трансформировать документ в внутренний формат документа, если новый документ или в транспортный док для закрытого
         * 3. отправить в глав. подсистему новый док. или на траснпорт закрытый
         * 4. запрос в бд на наличие документа (процессор)
         * 5. открыть или закрыть счет (процессор)
         */
        from("direct:billLogicMain")
                .routeId("billLogicMain")
                .choice()
                    .when(simple("${property.documentType} == 'create_bill'"))
                        .to("direct:toLocalDocumentTransfrom")
                        .setBody(exchangeProperty("transformedDocument"))
                        .to("direct:createBillRoute")
                        .to("direct:toStore")
                    .when(simple("${property.documentType} == 'close_bill'"))
                        .to("direct:closeBillRoute")
//                        .to("xslt:transform/transferDocumentRequest.xsl")
                        .to(DIRECT_TRANSFER_REQUEST)
                    .otherwise()
                        .setProperty("statusCode", simple(ResponseStatus.INCOME_MESSAGE_ERROR.getCode()))
                        .setProperty("statusDetail", simple(ResponseStatus.INCOME_MESSAGE_ERROR.getDetail()))
                        .throwException(new IllegalArgumentException("document type is not supported"))
                .endChoice()
                .end();
        from("direct:closeBillRoute")
                .routeId("CloseBillRoute")
                .to("closeBillProcessor");

        from("direct:createBillRoute")
                .routeId("CreateBillRoute")

                .convertBodyTo(String.class)
                .to("createBillProcessor");

        from("direct:toStore")
                .routeId("ToStoreReq")
                .setBody(exchangeProperty("storeReq"))
                .toD("xslt:transform/toSoapEnvelope.xsl")
                .to(DIRECT_STORE_REQUEST) //wsdl/external/store-service/storeDocumentService.wsdl
                .end();

        from("direct:toLocalDocumentTransfrom")
                .routeId("ToLocalDocTrans")
                .setBody(exchangeProperty("document"))
                .setProperty("transfromPath", simple("transform/create/${property.subSystem}/" +
                        "${property.clientType}/${property.documentName}_fromSub.xsl"))
                .toD("xslt:${property.transfromPath}")
                .setProperty("storeReq", bodyAs(String.class))
                .setProperty("docStatus", ns.xpath("//lc:document[1]/@status", String.class))
                .setProperty("transformedDocument", ns.xpath("//lc:storeDocReq/lc:document/*[1]"))
                .end();

    }

}
