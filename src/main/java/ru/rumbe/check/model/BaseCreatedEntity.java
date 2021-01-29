package ru.rumbe.check.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@Entity
public abstract class BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long Id;

}
