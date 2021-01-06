package ru.rumbe.check.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ImportDocumentRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        from("cxf:bean:importDocumentService")
                .routeId("ImportDocumentService")
                .process(x -> {
                    x.getIn();
                })
                .end();
    }
}
