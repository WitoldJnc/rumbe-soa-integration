package ru.rumbe.check.processor;

import org.apache.camel.Exchange;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.stereotype.Component;

import org.apache.camel.Processor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import java.io.StringReader;

@Component("soapMessageConverter")
@AutoConfigureBefore
public class SoapMessageConverter implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String stringBody = exchange.getIn().getBody(String.class);
        SOAPMessage soapMessage = stringToSoap(stringBody);
        exchange.getIn().setBody(soapMessage);
    }

    public static SOAPMessage stringToSoap(String text) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(text));
        Document document = db.parse(is);

        SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();

        SOAPBody soapBody = soapMessage.getSOAPBody();
        soapBody.addDocument(document);
        return soapMessage;
    }
}

