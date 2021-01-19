package ru.rumbe.check.config.common;

import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.DataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.rumbe.soa.lc._1_2.lifecycle.StoreDocumentServicePtt;
import ru.rumbe.soa.lc._1_3.transfer.TransferDocumentServicePtt;

@Configuration
@AutoConfigureBefore
public class ExternalEndpointConfig {

    @Value("${store-document.url}")
    private String storeDocUrl;

    @Value("${transfer-document.url}")
    private String transferDocUrl;

    @Bean(name = "storeDocEndpoint")
    public CxfEndpoint storeDocEndpoint() {
        CxfEndpoint cxfEndpoint = new CxfEndpoint();
        cxfEndpoint.setAddress(storeDocUrl);
        cxfEndpoint.setServiceClass(StoreDocumentServicePtt.class);
        cxfEndpoint.setWsdlURL("wsdl/external/lifecycle/documentLifeCycleService.wsdl");
        cxfEndpoint.setDataFormat(DataFormat.CXF_MESSAGE);
        return cxfEndpoint;
    }

    @Bean(name = "transferDocEndpoint")
    public CxfEndpoint transferDocEndpoint() {
        CxfEndpoint cxfEndpoint = new CxfEndpoint();
        cxfEndpoint.setAddress(transferDocUrl);
        cxfEndpoint.setServiceClass(TransferDocumentServicePtt.class);
        cxfEndpoint.setWsdlURL("wsdl/external/transfer/transferDocumentService.wsdl");
        cxfEndpoint.setDataFormat(DataFormat.CXF_MESSAGE);
        return cxfEndpoint;
    }
}
