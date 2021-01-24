package ru.rumbe.check.model.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rumbe.check.model.BaseClosedEntity;
import ru.rumbe.check.repo.RumbeDocument;
import ru.rumbe.check.utils.InternalDocType;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class RumbeCloseOAOKRK extends BaseClosedEntity implements RumbeDocument {
    private String greenOprotedStatus;
    private LocalDateTime updDt = LocalDateTime.now();

    @Override
    public InternalDocType getType() {
        return InternalDocType.KRK;
    }

    @Override
    public String getTableName() {
        return "pim_krk";
    }

    @Override
    public String getDocStatus() {
        return this.greenOprotedStatus;
    }

    @Override
    public Long getId() {
        return super.getId();
    }

    @Override
    public void setId(Long Id) {
        super.setId(Id);
    }
}
