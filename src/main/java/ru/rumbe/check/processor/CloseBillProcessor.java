package ru.rumbe.check.processor;

import lombok.val;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.rumbe.check.repo.CloseBillService;
import ru.rumbe.check.repo.RumbeDocument;
import ru.rumbe.check.utils.ExternalDocType;
import ru.rumbe.check.utils.InternalDocType;
import ru.rumbe.check.utils.RubmeDocumentFactory;
import ru.rumbe.internal.services.checkdocumentservice.docs.EmploymentTypeType;

import java.util.Map;

@Component("closeBillProcessor")
public class CloseBillProcessor implements Processor {

    @Autowired
    private RubmeDocumentFactory factory;

    @Autowired
    private CloseBillService closeBillService;

    @Override
    public void process(Exchange exchange) throws Exception {
        val docType = exchange.getProperty("documentName", ExternalDocType.class);
        final String guid = exchange.getProperty("guid", String.class);
        EmploymentTypeType clientType = exchange.getProperty("clientType", EmploymentTypeType.class);

        Class<? extends RumbeDocument> clazz = factory.createdDocument(docType).getClass();
        RumbeDocument record = exchange.getIn().getBody(clazz);
        final String docStatus = record.getDocStatus();

        closeBillService.closeBillByGuid(record.getTableName(), clientType, guid);


    }
}
