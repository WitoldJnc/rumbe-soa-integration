package ru.rumbe.check.config.internal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import ru.rumbe.check.config.common.IntegrationProperties;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "rumbe.check-document")
@Validated
@Getter
@Setter
public class CheckImportDocumentConfigProperties {

    @NotNull
    private IntegrationProperties checkImportDocument;

}
