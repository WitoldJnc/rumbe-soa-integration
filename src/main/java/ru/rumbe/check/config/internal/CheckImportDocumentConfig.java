package ru.rumbe.check.config.internal;

import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.DataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.rumbe.internal.services.checkdocumentservice.CheckDocumentServicePtt;

@Configuration
@EnableConfigurationProperties(CheckImportDocumentConfigProperties.class)
public class CheckImportDocumentConfig {

    @Autowired
    private CheckImportDocumentConfigProperties properties;

    @Bean("importDocumentService")
    public CxfEndpoint importDocumentService() {
        CxfEndpoint cxfEndpoint = new CxfEndpoint();
        cxfEndpoint.setAddress(properties.getCheckImportDocument().getUrl());
        cxfEndpoint.setServiceClass(CheckDocumentServicePtt.class);
        cxfEndpoint.setWsdlURL("wsdl/internal/checkRumbeDocuments.wsdl");
        cxfEndpoint.setDataFormat(DataFormat.PAYLOAD);
        return cxfEndpoint;
    }

}
