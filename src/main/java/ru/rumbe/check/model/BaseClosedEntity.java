package ru.rumbe.check.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Data
public abstract class BaseClosedEntity {
    private Long Id;

}
