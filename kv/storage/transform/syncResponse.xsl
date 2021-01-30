<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:bpws="http://schemas.xmlsoap.org/ws/2003/03/business-process/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
                xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:doc="http://www.rumbe.ru/internal/services/checkDocumentService/docs"
                exclude-result-prefixes="xsi xsl xsd soap wsdl doc bpws">

    <xsl:param name="statusCode"/>
    <xsl:param name="statusDetail"/>
    <xsl:param name="error"/>
    <xsl:param name="stack"/>

    <xsl:template match="/">
        <doc:checkDocumentResponse>
            <xsl:attribute name="versionId">
                <xsl:value-of select="/doc:transferDocumentRequest/@versionId"/>
            </xsl:attribute>
            <doc:guid>
                <xsl:value-of select="/doc:transferDocumentRequest/doc:header/doc:guid"/>
            </doc:guid>
            <doc:responseDateTime>
                <xsl:value-of select="current-dateTime()"/>
            </doc:responseDateTime>
            <doc:responseStatus>
                <doc:code>
                    <xsl:value-of select="$statusCode"/>
                </doc:code>
                <xsl:if test="$error">
                    <doc:message>
                        <xsl:value-of select="concat($error, $stack)"/>
                    </doc:message>
                </xsl:if>

                <doc:detail>
                    <xsl:value-of select="$statusDetail"/>
                </doc:detail>
            </doc:responseStatus>
        </doc:checkDocumentResponse>
    </xsl:template>
</xsl:stylesheet>
