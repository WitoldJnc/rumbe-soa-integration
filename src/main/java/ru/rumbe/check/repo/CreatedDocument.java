package ru.rumbe.check.repo;

import ru.rumbe.check.utils.CreateDocumentTypes;


public interface CreatedDocument {
    CreateDocumentTypes getType();

    String getTableName();

    String getDocStatus();

    Long getId();

    void setId(Long id);

}
