package ru.rumbe.check.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.stereotype.Component;

/**
 * Отправка во внешние сервисы. Если сообщение пришло по топику - отправить в топик соотв. сервиса, если по соапу - в ендпоинт через cxf
 */
@Component
@AutoConfigureBefore
public class ExternalRoutes extends RouteBuilder {

    public static final String DIRECT_STORE_REQUEST = "direct:storeRequest";
    public static final String DIRECT_TRANSFER_REQUEST = "direct:transferRequest";

    @Override
    public void configure() throws Exception {
        errorHandler(noErrorHandler());

        from(DIRECT_STORE_REQUEST)
                .routeId("external-store-request-{{routeversion}}")
                .setHeader(CxfConstants.OPERATION_NAME, constant("storeRumbeDoc"))
                .to("xslt:transform/toSoapEnvelope.xsl")
                .convertBodyTo(String.class)
                .choice()
                    .when(simple("${header.Message-Type} == 'KAFKA'"))
                        .setHeader(KafkaConstants.PARTITION_KEY, constant(0))
                        .setHeader(KafkaConstants.KEY).exchangeProperty("guid")
                        .setHeader(KafkaConstants.TOPIC).simple("{{store-document.kafka_topic}}")
                        .to("{{kafka-soa-store-document-url}}")
                    .otherwise()
                        .to("direct:storeRequestProcess")
                .end();

        from(DIRECT_TRANSFER_REQUEST)
                .routeId("external-transfer-request-{{routeversion}}")
                .setHeader(CxfConstants.OPERATION_NAME, constant("transferRumbeDoc"))
                .to("xslt:transform/toSoapEnvelope.xsl")
                .convertBodyTo(String.class)
                .choice()
                    .when(simple("${header.Message-Type} == 'KAFKA'"))
                        .setHeader(KafkaConstants.PARTITION_KEY, constant(0))
                        .setHeader(KafkaConstants.KEY).exchangeProperty("guid")
                        .setHeader(KafkaConstants.TOPIC).simple("{{transfer-document.kafka_topic}}")
                        .to("{{kafka-soa-transfer-document-url}}")
                    .otherwise()
                        .to("direct:transferRequestProcess")
                .end();

        from("direct:transferRequestProcess")
                .routeId("TransferRequestProcess-{{routeversion}}")
                .setHeader(Exchange.CHARSET_NAME, constant("UTF-8"))
                .setHeader("SOAPAction", constant("transferRumbeDoc"))
                .setHeader(CxfConstants.OPERATION_NAMESPACE, constant("http://www.rumbe.ru/soa/lc/1_3/transfer"))
                .to("cxf:bean:transferDocEndpoint")
                .convertBodyTo(String.class)
                .to("log:end?level=INFO&showAll=true&multiline=true");

        from("direct:storeRequestProcess")
                .routeId("StoreRequestProcess-{{routeversion}}")
                .setHeader(Exchange.CHARSET_NAME, constant("UTF-8"))
                .setHeader("SOAPAction", constant("storeRumbeDoc"))
                .setHeader(CxfConstants.OPERATION_NAMESPACE, constant("http://www.rumbe.ru/soa/lc/1_2/lifecycle"))
                .to("cxf:bean:storeDocEndpoint")
                .convertBodyTo(String.class)
                .to("log:end?level=INFO&showAll=true&multiline=true");
    }
}
