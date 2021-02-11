<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:transfer="http://www.rumbe.ru/soa/lc/1_3/transfer"
                xmlns:docs="http://www.rumbe.ru/internal/services/checkDocumentService/docs"
                version="3.0">

    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
    <xsl:param name="TransformMode" select="'default'"/>

    <xsl:param name="packageId"/>
    <xsl:param name="subSystem"/>
    <xsl:param name="documentType"/>
    <xsl:param name="documentName"/>
    <xsl:param name="guid"/>

    <xsl:template match="/">
                <transfer:transferDocReq>
                    <transfer:header>
                        <transfer:packageId>
                            <xsl:value-of select="$packageId"/>
                        </transfer:packageId>
                        <transfer:targetSystem>
                            <xsl:value-of select="$subSystem"/>
                        </transfer:targetSystem>
                        <transfer:documentType>
                            <xsl:value-of select="$documentType"/>
                        </transfer:documentType>
                        <transfer:documentName>
                            <xsl:value-of select="$documentName"/>
                        </transfer:documentName>
                        <transfer:documentGuid>
                            <xsl:value-of select="$guid"/>
                        </transfer:documentGuid>
                        <transfer:creationDateTime>
                            <xsl:value-of select="current-dateTime()"/>
                        </transfer:creationDateTime>
                    </transfer:header>
                    <transfer:document>
                        <xsl:value-of select="//docs:rumbeDocument[1]/*"/>
                    </transfer:document>
                </transfer:transferDocReq>
    </xsl:template>

</xsl:stylesheet>