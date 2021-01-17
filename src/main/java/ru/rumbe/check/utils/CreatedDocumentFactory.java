package ru.rumbe.check.utils;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import ru.rumbe.check.model.create.RumbeCreateIPNotice;
import ru.rumbe.check.repo.CreatedDocument;

@Component
public class CreatedDocumentFactory {

    @SneakyThrows
    public CreatedDocument createdDocument(CreateDocumentTypes types) {
        CreatedDocument document = null;
        switch (types) {
            case rumbeCreateIPNotice:
                document = new RumbeCreateIPNotice();
                break;
        }

        return document;
    }
}
