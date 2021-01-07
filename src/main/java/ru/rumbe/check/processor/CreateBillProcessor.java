package ru.rumbe.check.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component("createBillProcessor")
public class CreateBillProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {

    }
}
