package ru.rumbe.check.service;

import org.springframework.stereotype.Component;
import ru.rumbe.check.repo.RumbeDocument;
import ru.rumbe.check.repo.CreatedDocumentService;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Component
public class CreatedDocumentServiceImpl implements CreatedDocumentService {

    @PersistenceContext
    protected EntityManager em;

    @Override
    public Optional<RumbeDocument> getLastDocument(String table, String guid, Class clazz) {
        final String query = String.format("SELECT * FROM documents.%s dc WHERE dc.guid = '%s' \n" +
                "ORDER BY dc.upd_dt desc \n" +
                "LIMIT 1", table, guid);

        try {
            return Optional.of((RumbeDocument) em.createNativeQuery(query, clazz).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
