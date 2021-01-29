package ru.rumbe.check.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.rumbe.check.repo.CloseBillService;
import ru.rumbe.internal.services.checkdocumentservice.docs.EmploymentTypeType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Component
@Slf4j
public class CloseBillServiceImpl implements CloseBillService {

    @PersistenceContext
    protected EntityManager em;

    @Override
    @Transactional
    public void closeBillByGuid(String table, EmploymentTypeType clientType, String guid) {
        final String query = String.format("UPDATE documents.%1$s dc\n" +
                "SET current_status = 'Closed'\n" +
                "WHERE dc.id =\n" +
                "    COALESCE(\n" +
                "        (SELECT pr.id FROM documents.%1$s pr\n" +
                "            JOIN documents.pim_%2$s_doc cl ON cl.guid = pr.guid AND cl.current_status IS NOT 'Closed'\n" +
                "            WHERE pr.guid = '%3$s'\n" +
                "            ORDER BY pr.upd_dt DESC\n" +
                "            LIMIT 1),\n" +
                "        (SELECT reest.doc_id FROM documents.%1$s pr\n" +
                "            JOIN documents.pim_%2$s_doc cl ON cl.guid = pr.guid\n" +
                "            JOIN documents.organizations org ON org.code = cl.org_code\n" +
                "            JOIN documents.dc_reest reest ON reest.org_id = org.id\n" +
                "                AND reest.doc_guid = '%3$s'\n" +
                "            WHERE reest.doc_status IS NOT 'Closed'\n" +
                "            ORDER BY reest.upd_dt DESC\n" +
                "            LIMIT 1)\n" +
                "    )\n" +
                "ORDER BY upd_dt DESC\n" +
                "LIMIT 1;", table, clientType.name(), guid);
        log.debug(query);
        em.createNativeQuery(query)
                .executeUpdate();

    }
}
