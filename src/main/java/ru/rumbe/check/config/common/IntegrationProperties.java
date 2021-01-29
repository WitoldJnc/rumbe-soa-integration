package ru.rumbe.check.config.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntegrationProperties {
    private String url;
    private String kafkaTopic;
    private String kafkaEndpointUri;
}
