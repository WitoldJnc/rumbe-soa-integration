package ru.rumbe.check.model.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rumbe.check.model.BaseCreatedEntity;
import ru.rumbe.check.repo.RumbeDocument;
import ru.rumbe.check.utils.InternalDocType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

@XmlRootElement(name = "rumbeCreateIPNotice", namespace = "http://www.rumbe.ru/create/createProphetBillNotice")
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
@Data
@Table(name = "pim_ip_notice", schema = "documents")
@Entity
public class RumbeCreateIPNotice extends BaseCreatedEntity implements RumbeDocument {

    @XmlElement(name = "orgGuid")
    @NotNull
    private String orgGuid;

    @XmlElement(name = "docType")
    @NotNull
    private String docType;
    @XmlElement(name = "noticeDocTypeLock")
    private String noticeDocTypeLock;
    @XmlElement(name = "noticeTypeStopped")
    private String noticeTypeStopped;
    @XmlElement(name = "noticeAccNum")
    private String noticeAccNum;
    @XmlElement(name = "noticeAccActivate")
    private String noticeAccActivate;
    @XmlElement(name = "noticeDocumentStatus")
    private String noticeDocumentStatus;
    @XmlElement(name = "guid")
    private String guid;

    private LocalDateTime updDt = LocalDateTime.now();

    @Override
    public InternalDocType getType() {
        return InternalDocType.rumbeCreateIPNotice;
    }

    @Override
    public String getTableName() {
        return "pim_ip_notice";
    }

    @Override
    public String getDocStatus() {
        return this.noticeDocumentStatus;
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
