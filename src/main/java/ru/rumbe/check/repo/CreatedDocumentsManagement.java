package ru.rumbe.check.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rumbe.check.model.BaseCreatedEntity;

public interface CreatedDocumentsManagement<T extends BaseCreatedEntity> extends JpaRepository<T, Long>{

}
