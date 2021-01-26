package ru.rumbe.check.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rumbe.check.utils.LogType;

@Data
@NoArgsConstructor
@JsonInclude
public class KafkaLogEntity {
    private String elkIndexName;
    private String documentType;
    private String documentName;
    private String guid;
    private String timestamp;
    private String error;
    private String stackTrace;
    private LogType logType;

    private String routeId;
    private String requestBody;
    private String responseBody;
}
