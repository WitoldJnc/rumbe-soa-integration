package ru.rumbe.check.repo;

import ru.rumbe.internal.services.checkdocumentservice.docs.EmploymentTypeType;

public interface CloseBillService {
    void closeBillByGuid(String table, EmploymentTypeType clientType, String guid);

}
