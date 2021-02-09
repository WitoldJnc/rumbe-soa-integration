### [README RUS](READMERUS.md)
Integration service (IS) for checking and storing documents received via SOAP protocol, based on bpel process of composite Oracle SOA application.

The service is developed using Spring Boot, Apache Camel. The logic is implemented through Java DSL routing.


### Description of IS workflow
Integration between external services is implemented through both according to the wsdl specification of already existing services  
[wsdl of current service](src/main/resources/wsdl/internal/checkRumbeDocuments.wsdl)  
[wsdl of external store document service](src/main/resources/wsdl/external/lifecycle/documentLifeCycleService.wsdl)  
[wsdl of external transfer document service](src/main/resources/wsdl/external/transfer/transferDocumentService.wsdl)




```
from("cxf:bean:importDocumentService")
...
```

and through [consume Kafka Topic](src/main/java/ru/rumbe/check/route/ImportDocumentRoute.java) broker messages from
Apache Kafka.

```
 from("{{kafka-import-document-url}}")
 ...
```
properties:
```
#kafka
kafka.broker-url=kafka:9092
kafka.group-id=rumbe
kafka-import-document-topic=rumbe_importDocument_topic
kafka-import-document-url=kafka://brokers=${kafka.broker-url:}?topic=${kafka-import-document-topic:}&groupId=${kafka.group-id:}
```

> Further, in the course of the development of the general IS project, it is planned to abandon the use of internal wsdl in favor of message brokers.

In the case of receiving IS messages via SOAP, following the [logic](src/main/java/ru/rumbe/check/route/ExternalRoutes.java),
external services will be accessed via classes generated from wsdl POJO using wsdl2java maven plugin and CXF Beans [created for each service](src/main/java/ru/rumbe/check/config/common/ExternalEndpointConfig.java)
```
    @Bean(name = "storeDocEndpoint")
    public CxfEndpoint storeDocEndpoint() {
       ..
        cxfEndpoint.setServiceClass(StoreDocumentServicePtt.class);
        cxfEndpoint.setWsdlURL("wsdl/external/lifecycle/documentLifeCycleService.wsdl");
        cxfEndpoint.setDataFormat(DataFormat.CXF_MESSAGE);
        return cxfEndpoint;
    }
```

You can also see how Camel refers to the CXF Beans of common external services [here](src/main/java/ru/rumbe/check/route/ExternalRoutes.java)

```
//In the main route with business logic, there is a transfer to a route with the logic of call to a common external service
 from("direct:billLogicMain")
    ...
    .to(DIRECT_TRANSFER_REQUEST)  //wsdl/external/store-service/transferDocumentService.wsdl
    ...
    .end();
    
   
//Select type of referring  
    from(DIRECT_TRANSFER_REQUEST)
    ...
    .choice()
        .when(simple("${header.Message-Type} == 'KAFKA'"))
            ...
            //to Kafka
            .to("{{kafka-soa-transfer-document-url}}")
        .otherwise()
            //in the Bean call processing route
            .to("direct:transferRequestProcess")
    .end()
   
    from("direct:transferRequestProcess")
        ...
        .to("cxf:bean:transferDocEndpoint")
        ...
```

### Transformations and validations
Since all messages received by the IS, have the format of the message SOAP protocol, regardless of whether it's receiving by wsdl or from the Kafka topic, and contain an incoming document, in the project there is a need for validation and transformation of the documents in the format of the internal subsystem.

Validation of xsd documents is performed by internal means provided by Apache Camel, through Validator
```
 from("direct:validateDocument")
    ...
    .setProperty("validationPath", simple("document/${property.billType}/${property.subSystem}/" +
                        "${property.clientType}/${property.documentName}_validation.xsd?useSharedSchema=false"))
    ...
    .toD("validator:${property.validationPath}")
    ...
    .end();
```
Transformation, accordingly, too.
```
from("direct:toLocalDocumentTransfrom")
    ...
    .setProperty("transfromPath", simple("transform/create/${property.subSystem}/" +
                            "${property.clientType}/${property.documentName}_fromSub.xsl"))
    ...
    .toD("xslt:${property.transfromPath}")
    ...
    .end();
```
>Further, when more than 2-3 documents and more than 1 version for each document are added, it is planned to transfer all the details required for transformation and validation to the cloud storage, or to the Hashicorp Consul service's kv storage. This solution will allow adding new files only to the storage without reloading the IS.


### Logging
At each step of message processing the intermediate result is logged in the Kafka current log topic
[KafkaLogRoute](src/main/java/ru/rumbe/check/route/KafkaLogRoute.java)  .  
There is nothing to note about the logging itself, everything is handled by standard Apache Camel methods

To display the logs conveniently, the ELK stack is configured in the project portainer from the image [sebp/elk](https://hub.docker.com/r/sebp/elk/) and also a logstash subscription is configured for all IS-log topics

### Containerization
The creation of the  application image takes place during the building of the project. This is done with the io.fabric8 plugin.  
[Dockerfile](Dockerfile)
```
            <plugin>
                <groupId>io.fabric8</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <version>0.24.0</version>
                <configuration>
                    <images>
                        <image>
                            <name>ph-daily-trash-bot</name>
                            <build>
                                <dockerFileDir>${project.basedir}</dockerFileDir>
                            </build>
                        </image>
                    </images>
                </configuration>
                <executions>
                    <execution>
                        <id>build</id>
                        <phase>install</phase>
                        <goals>
                            <goal>build</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```
### Deploying
Deploying the application to different environments is done through Ansible, configured for each environment.

[Ansible readme file](ansible/README.md)

### Distribution
The distribution a application is implemented with maven.assembly.plugin and when assembled includes:
* application jar
* actual kv storage, to be updated in the environment
* ansible scripts for deploying an application in an environment
* current version description

[distribution configuration](src/main/assembly/dist.xml)

### release branch git metadata 
to see branch metadata:  **host:port/actuator/info**

**Stack involved in the development of the IS**   
Spring Boot  
Apache Camel (Java DSL, xslt, xsd validation)   
Apache Kafka  
CXF  
Postgres, Flyway (migration)
Ansible (deployment)