package ru.rumbe.check.route;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.rumbe.check.model.KafkaLogEntity;

import java.io.InputStream;

@Component
public class KafkaLogRoute extends RouteBuilder {
    @Value("${elk.index.id}")
    private String elkIndexId;

    @Value("${elk.index.name}")
    private String elkIndexName;

    @Override
    public void configure() throws Exception {
        if (elkIndexId.isEmpty()) {
            from("direct:log-to-kafka")
                    .to("log:body");
        } else {
            from("direct:log-to-kafka")
                    .setProperty("Content-type", simple("${header.Content-type}"))
                    .setProperty("originalBody", simple("${body}"))
                    .setProperty("routeId", simple("${property.routeId}"))
                    .setProperty(elkIndexId, constant(elkIndexName))
                    .setHeader(KafkaConstants.PARTITION_KEY, constant(0))
                    .setHeader(KafkaConstants.KEY).exchangeProperty("guid")
                    .setHeader(KafkaConstants.TOPIC).constant("{{kafka-log-topic}}")
                    .to("convertToKafkaMessage")
                    //todo marshal pojo to json format
                    .doTry()
                        .convertBodyTo(InputStream.class)
                        .to("{{kafka-log-url}}")
                    .doCatch(Exception.class)
                        .log("Exception in KafkaRoute ${exception.message} ${exception.stacktrace}")
                    .doFinally()
                        .setBody(exchangeProperty("originalBody"))
                        .removeProperties("request")
                        .removeProperties("response")
                        .removeProperties("originalBody")
                        .setHeader("Content-type").exchangeProperty("Content-type")
                    .endDoTry();
        }

        from("direct:exception-log-to-kafka")
                .setProperty("error", simple("${exception.message}"))
                .setProperty("stack", simple("${exception.stacktrace}"))
                .setProperty(elkIndexId, constant(elkIndexName))
                .to("direct:log-to-kafka");
    }
}
