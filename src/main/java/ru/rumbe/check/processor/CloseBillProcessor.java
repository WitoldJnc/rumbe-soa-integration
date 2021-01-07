package ru.rumbe.check.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.rumbe.check.repo.BillService;

@Component("closeBillProcessor")
public class CloseBillProcessor implements Processor {
//
//    @Autowired
//    protected BillService billService;

    @Override
    public void process(Exchange exchange) throws Exception {
        final String guid = exchange.getProperty("guid", String.class);
    }
}
