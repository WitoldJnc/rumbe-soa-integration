package ru.rumbe.check.route;

import junit.framework.TestCase;
import org.apache.camel.*;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.InterceptSendToMockEndpointStrategy;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.w3c.dom.Document;
import ru.rumbe.check.RumbeApplication;
import ru.rumbe.check.utils.DocumentStatusTypes;
import ru.rumbe.check.utils.ExternalDocType;
import ru.rumbe.internal.services.checkdocumentservice.docs.EmploymentTypeType;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@RunWith(CamelSpringBootRunner.class)
@SpringBootConfiguration
@TestPropertySource(value = {"/applicationTest.properties"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = {RumbeApplication.class})
public class ImportDocumentRouteTest extends CamelTestSupport {
    protected static final String DIRECT_TEST_ROUTER = "direct:testRouter";

    @Autowired
    protected CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:direct:createBillRoute")
    private MockEndpoint createBillProcessorRoute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
//        camelContext.addRegisterEndpointCallback(new InterceptSendToMockEndpointStrategy("direct:log-to-kafka", true));
        mockEnpointToInject("billLogicMain", "direct:createBillRoute", "mock:direct:createBillRoute");
        startCamelContext();
        camelContext.getRouteDefinition("ImportDocumentService-SOAP").adviceWith(camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                replaceFromWith(DIRECT_TEST_ROUTER);
            }
        });

        mockEndpoint("direct:createBillRoute", "test");

    }

    @Test
    public void testContext() {
        assertNotNull(camelContext);
    }

    @Test
    public void testImportCreateIPBillNoticeDocument() throws IOException {
        Exchange exchange = camelContext.getEndpoint("direct:incomeDocumentProcess").createExchange();
        exchange.getIn().setBody(readFileFromResource("income.xml"));
        String body = producerTemplate.send("direct:incomeDocumentProcess", exchange).getIn().getBody(String.class);
        assertNotNull(body);

        assertEquals(EmploymentTypeType.IP, exchange.getProperty("clientType", EmploymentTypeType.class));
        assertEquals(DocumentStatusTypes.Actualized, exchange.getProperty("docStatus", DocumentStatusTypes.class));
        assertEquals("create", exchange.getProperty("billType", String.class));
        assertEquals(ExternalDocType.createProphetBillNotice, exchange.getProperty("documentName", ExternalDocType.class));
    }


    public void mockEnpointToInject(String routeIdWhereNeedToMock, String routeURIToReplace, String mockEndpointUriFull) throws Exception {

        camelContext.getRouteDefinition(routeIdWhereNeedToMock).adviceWith(camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() {
                weaveByToUri(routeURIToReplace).replace().to(mockEndpointUriFull);
            }
        });
    }

    public void mockEndpoint(String endpointBean, String response) {
        MockEndpoint mockEndpoint = (MockEndpoint) camelContext.getEndpoint("mock://" + endpointBean);
        mockEndpoint.expectedMessageCount(1);
        mockEndpoint.returnReplyBody(new Expression() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T evaluate(Exchange exchange, Class<T> type) {
                return (T) response;
            }
        });
    }

    public static String readFileFromResource(String filePath) throws IOException {

        ClassLoader classLoader = ImportDocumentRouteTest.class.getClassLoader();
        URL resource = classLoader.getResource(filePath);

        if (resource == null) {
            throw new IllegalArgumentException("Фаил не найден!");
        } else {
            return FileUtils.readFileToString(new File(resource.getFile()));
        }
    }



}