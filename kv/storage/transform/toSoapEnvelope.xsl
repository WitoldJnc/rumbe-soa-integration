<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                exclude-result-prefixes="soapenv">
    <xsl:template match="/">
        <soapenv:Envelope>
            <soapenv:Header>
            </soapenv:Header>
            <soapenv:Body>
                <xsl:copy-of select="/"/>
            </soapenv:Body>
        </soapenv:Envelope>
    </xsl:template>
</xsl:stylesheet>