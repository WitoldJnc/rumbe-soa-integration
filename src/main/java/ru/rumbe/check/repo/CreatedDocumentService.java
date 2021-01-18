package ru.rumbe.check.repo;

import java.util.Optional;

public interface CreatedDocumentService {

    Optional<RumbeDocument> getLastDocument(String table, String guid, Class tClass);

}
