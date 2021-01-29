package ru.rumbe.check.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.rumbe.check.model.KafkaLogEntity;
import ru.rumbe.check.utils.LogType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component("convertToKafkaMessage")
public class ConvertToKafkaMessageProcessor implements Processor {
    @Value("${elk.index.id}")
    String elkIndexId;

    @Override
    public void process(Exchange exchange) throws Exception {
        KafkaLogEntity logEntity = new KafkaLogEntity();
        logEntity.setRouteId((String) exchange.getProperty("routeId"));
        Object request = exchange.getProperty("request");
        if (request instanceof String) {
            logEntity.setRequestBody((String) request);
        } else if (request instanceof byte[]) {
            logEntity.setRequestBody(new String((byte[]) request));
        }

        if (exchange.getProperty("error") == null) {
            logEntity.setLogType(LogType.INFO);
            //Наши логи
            Object response = exchange.getProperty("response");
            if (response instanceof String) {
                logEntity.setResponseBody((String) response);
            } else if (response instanceof byte[]) {
                logEntity.setResponseBody(new String((byte[]) response));
            }
            //
        } else {
            logEntity.setLogType(LogType.ERROR);
            logEntity.setError((String) exchange.getProperty("error"));
            logEntity.setStackTrace((String) exchange.getProperty("stack"));
        }

        logEntity.setElkIndexName(exchange.getProperty(elkIndexId, String.class));
        logEntity.setDocumentType(exchange.getProperty("documentType", String.class));
        logEntity.setDocumentType(exchange.getProperty("documentName", String.class));
        logEntity.setGuid(exchange.getProperty("guid", String.class));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        logEntity.setTimestamp(dateFormat.format(new Date()));
        exchange.getIn().setBody(logEntity);

    }
}
