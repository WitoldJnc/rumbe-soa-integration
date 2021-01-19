package ru.rumbe.check.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.stereotype.Component;

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
                .to("direct:storeRequestProcess");

        from(DIRECT_TRANSFER_REQUEST)
                .routeId("external-transfer-request-{{routeversion}}")
                .setHeader(CxfConstants.OPERATION_NAME, constant("transferRumbeDoc"))
                .to("direct:transferRequestProcess");

        from("direct:transferRequestProcess")
                .routeId("TransferRequestProcess-{{routeversion}}")
                .to("xslt:transform/toSoapEnvelope.xsl")
                .setHeader(Exchange.CHARSET_NAME, constant("UTF-8"))
                .setHeader("SOAPAction", constant("transferRumbeDoc"))
                .setHeader(CxfConstants.OPERATION_NAMESPACE, constant("http://www.rumbe.ru/soa/lc/1_3/transfer"))
                .to("cxf:bean:transferDocEndpoint")
                .convertBodyTo(String.class)
                .to("log:end?level=INFO&showAll=true&multiline=true");

        from("direct:storeRequestProcess")
                .routeId("StoreRequestProcess-{{routeversion}}")
                .to("xslt:transform/toSoapEnvelope.xsl")
                .setHeader(Exchange.CHARSET_NAME, constant("UTF-8"))
                .setHeader("SOAPAction", constant("storeRumbeDoc"))
                .setHeader(CxfConstants.OPERATION_NAMESPACE, constant("http://www.rumbe.ru/soa/lc/1_2/lifecycle"))
                .to("cxf:bean:storeDocEndpoint")
                .convertBodyTo(String.class)
                .to("log:end?level=INFO&showAll=true&multiline=true");
    }
}
