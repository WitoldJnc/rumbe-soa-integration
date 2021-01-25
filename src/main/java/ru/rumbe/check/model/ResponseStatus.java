package ru.rumbe.check.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseStatus {
    SUCCESS("0", "SUCCESS"),
    VALIDATION_ERROR("1", "VALIDATION_ERROR"),
    SIGNATURE_CHECK_ERROR("2", "SIGNATURE_CHECK_ERROR"),
    IN_PROGRESS("3", "IN_PROGRESS"),
    INCOME_MESSAGE_ERROR("4", "INCOME_MESSAGE_ERROR"),
    SYSTEM_ERROR("9", "SYSTEM_ERROR");

    private String code;
    private String detail;

}
