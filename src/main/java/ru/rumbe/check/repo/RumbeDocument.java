package ru.rumbe.check.repo;


import ru.rumbe.check.utils.InternalDocType;

public interface RumbeDocument {
    InternalDocType getType();

    String getTableName();

    String getDocStatus();

    Long getId();

    void setId(Long id);

}
