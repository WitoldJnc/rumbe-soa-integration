FROM openjdk:8-jdk-alpine3.7
ARG JAR_FILE=target/rumbeCheckDocumentsService.jar
ADD ${JAR_FILE} rumbeCheckDocumentsService.jar
ENTRYPOINT ["java","-jar","/rumbeCheckDocumentsService.jar"]