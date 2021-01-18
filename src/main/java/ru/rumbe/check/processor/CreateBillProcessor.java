package ru.rumbe.check.processor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.rumbe.check.repo.RumbeDocument;
import ru.rumbe.check.repo.CreatedDocumentService;
import ru.rumbe.check.repo.CreatedDocumentsManagement;
import ru.rumbe.check.utils.ExternalDocType;
import ru.rumbe.check.utils.RubmeDocumentFactory;

import java.util.Optional;

import static ru.rumbe.check.utils.DocumentStatusTypes.*;

@Component("createBillProcessor")
@Slf4j
public class CreateBillProcessor implements Processor {

    @Autowired
    private RubmeDocumentFactory factory;

    @Autowired
    private CreatedDocumentsManagement createdDocumentsManagement;

    @Autowired
    private CreatedDocumentService createdDocumentService;


    /**
     * через фактори получить тип документа
     * если статус документа - created/formed/ -> проверить наличие документа по гуда в бд и если нет совпадений, сохранить
     * если статус документа - actualized -> взять последний документ по гуиду, сортированные по дате обновлений -> обновить последнюю запись
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        val externalDocType = exchange.getProperty("documentName", ExternalDocType.class);
        final String guid = exchange.getProperty("guid", String.class);
        final String docStatus = exchange.getProperty("docStatus", String.class);

        Class<? extends RumbeDocument> clazz = factory.createdDocument(externalDocType).getClass();

        RumbeDocument record = exchange.getIn().getBody(clazz);
        log.debug("record type: " + record.getType());

        Optional<RumbeDocument> lastDocument = createdDocumentService.getLastDocument(record.getTableName(), guid, clazz);

        if ((docStatus.equals(Created.name()) || docStatus.equals(Formed.name())) && !lastDocument.isPresent()) {
            createdDocumentsManagement.save(record);

        } else if (docStatus.equals(Actualized.name()) && lastDocument.isPresent()) {
            Optional findedEntity = createdDocumentsManagement.findById(lastDocument.get().getId());
            if(!findedEntity.get().equals(lastDocument.get())){
                record.setId(lastDocument.get().getId());
                createdDocumentsManagement.save(record);
            }
        }

    }
}
