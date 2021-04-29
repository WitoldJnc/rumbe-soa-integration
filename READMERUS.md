### [README ENG](README.md)
Интеграционный сервис (ИС) по проверке и сохранению документов принятых по SOAP протоколу, созданный на основе bpel процесса композитного Oracle SOA приложения.

Сервис написан с использованием Spring Boot, Apache Camel. Логика реализована посредством Java DSL роутинга.



### Описание работы ИС  
Интеграция между внешними сервисами осуществляется как по спецификации wsdl уже существующих сервисов  
[wsdl самого сервиса](src/main/resources/wsdl/internal/checkRumbeDocuments.wsdl)  
[wsdl сервиса сохранения документов](src/main/resources/wsdl/external/lifecycle/documentLifeCycleService.wsdl)  
[wsdl сервиса-транспортировщика](src/main/resources/wsdl/external/transfer/transferDocumentService.wsdl)  


```
from("cxf:bean:importDocumentService")
...
```

так и посредством [подписки и чтения](src/main/java/ru/rumbe/check/route/ImportDocumentRoute.java) сообщений брокера из
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

> В дальнейшем, по ходу развития общего ИС проекта, планируется отказаться от использования внутренних wsdl в сторону брокеров. 

В случае приема ИС сообщения по SOAP, следуя [логике](src/main/java/ru/rumbe/check/route/ExternalRoutes.java)
обращение к внешним сервисам будет происходить через сгенерированные из wsdl POJO классы при помощи wsdl2java maven плагина и [созданных на каждый сервис CXF бинов](src/main/java/ru/rumbe/check/config/common/ExternalEndpointConfig.java)
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

Так же обращение из Camel к CXF бинам общих внешних сервисов можно посмотреть [тут](src/main/java/ru/rumbe/check/route/ExternalRoutes.java)

```
//в главном роуте с бизнес логикой происходит переход в роут с логикой обращения к общему внешнему сервису
 from("direct:billLogicMain")
    ...
    .to(DIRECT_TRANSFER_REQUEST)  //wsdl/external/store-service/transferDocumentService.wsdl
    ...
    .end();
    
   
//выбор типа обращения  
    from(DIRECT_TRANSFER_REQUEST)
    ...
    .choice()
        .when(simple("${header.Message-Type} == 'KAFKA'"))
            ...
            //в кафку
            .to("{{kafka-soa-transfer-document-url}}")
        .otherwise()
            //в роут обработки обращения к бину
            .to("direct:transferRequestProcess")
    .end()
   
    from("direct:transferRequestProcess")
        ...
        .to("cxf:bean:transferDocEndpoint")
        ...
```

### Трансформации и валидации 
Т.к. все сообщения, входящие в интеграцию, имеют формат сообщения SOAP протокола, не важно, будь то прием по wsdl 
или из топика, и содержат в себе входящий документ, в проекте присутствует необходимость валидации и конвертации
документов в формат внутренний подсистемы.

Валидация документов по xsd происходит внутренними средствами, предоставленными Apache Camel, а именно - через Validator  
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
Трансформация, соотв, тоже.
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
>В дальнейшем, при добавлении более 2-3 документов и более 1 версии по каждому документу, 
планируется перенос всех необходимых для трансформации и валидации реквизитов в облачное хранилище, 
либо в kv хранилище Hashicorp Consul сервиса. Данное решение позволит не перезагружая ИС добавлять новые файлы только в хранилище. 


### Логирование
На каждом этапе обработки сообщения происходит логирование промежуточного результата в топик Kafka по логам соотв. сервиса
[KafkaLogRoute](src/main/java/ru/rumbe/check/route/KafkaLogRoute.java)  . 
По самому логированию отметить нечего, все стандартными методами Apache Camel  

Для удобного отображения логов, в portainer проекта настроен ELK стек из образа [sebp/elk](https://hub.docker.com/r/sebp/elk/) и так же 
настроена подписка logstash на все ИС-лог топики

### Контейнеризация  
Создание image приложение происходит во время сборки проекта. Осуществляется плагином io.fabric8 .  
Сам [Dockerfile](Dockerfile)
```
            <plugin>
                <groupId>io.fabric8</groupId>
                <artifactId>docker-maven-plugin</artifactId>
                <version>0.24.0</version>
                <configuration>
                    <images>
                        <image>
                            <name>rumbe-check-document-service</name>
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


### Деплой на стенды
Для удобства разворачивания приложения на различные контуры, испольуется Ansible с конфигурацией под каждый стенд.

[Ansible readme file](ansible/README.md)

### Распространение
Поставка ПО реализована с помощью maven.assembly.plugin и при сборке включает в себя:
* jar с приложением
* актуальное на момент разработки kv хранилище, для актуализации на стенде
* ansible скрипты для разворачивания приложения в контуре
* описание актуальной версии

[конфигурация распространения](src/main/assembly/dist.xml)

**Задействованные при создании ИС технологии**   
Spring Boot  
Apache Camel (Java DSL, xslt, xsd validation)   
Apache Kafka  
CXF  
Postgres, Flyway (migration)
Ansible (deployment)