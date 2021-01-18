package ru.rumbe.check.utils;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.rumbe.check.model.document.RumbeCloseOAOKRK;
import ru.rumbe.check.model.document.RumbeCreateIPNotice;
import ru.rumbe.check.repo.RumbeDocument;

@Component
public class RubmeDocumentFactory {

    @SneakyThrows
    public RumbeDocument createdDocument(ExternalDocType types) {
        RumbeDocument document = null;
        switch (types) {
            case createProphetBillNotice:
                document = new RumbeCreateIPNotice();
                break;
        }

        return document;
    }
}
