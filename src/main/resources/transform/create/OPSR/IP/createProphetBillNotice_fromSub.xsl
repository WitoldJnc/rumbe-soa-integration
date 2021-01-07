<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:timq="http://www.rumbe.ru/create/createProphetBillNotice"
                xmlns:tvr="http://www.rumbe.ru/create/createProphetBillNotice"
                xmlns:lc="http://www.rumbe.ru/soa/lc/1_2"
                exclude-result-prefixes="xsl timq tvr">
    <xsl:output indent="yes" encoding="UTF-8" method="xml"/>
    <xsl:template match="/">
        <xsl:apply-templates select="//timq:createProphetBillNotice"/>
    </xsl:template>
    <xsl:template match="//timq:createProphetBillNotice">
        <lc:storeDocReq>
            <lc:document>
                <xsl:attribute name="use-signature">
                    <xsl:text disable-output-escaping="no">false</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="document-type">
                    <xsl:text disable-output-escaping="no">createProphetBillNotice</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="guid">
                    <xsl:value-of select="//SI_TF_GUID"/>
                </xsl:attribute>
                <xsl:attribute name="status">
                    <xsl:value-of select="//SI_TF_DOCSTATE"/>
                </xsl:attribute>
                <tvr:rumbeCloseIPNotice xmlns:tvr="http://www.rumbe.ru/create/createProphetBillNotice">

                    <xsl:if test="//TypeLock">
                        <noticeDocTypeLock>
                            <xsl:value-of select="//TypeLock"/>
                        </noticeDocTypeLock>
                    </xsl:if>
                    <xsl:if test="//TypeStopedOperation">
                        <noticeTypeStopped>
                            <xsl:value-of select="//TypeStopedOperation"/>
                        </noticeTypeStopped>
                    </xsl:if>
                    <xsl:if test="//PersAccountNum">
                        <noticeAccNum>
                            <xsl:value-of select="//PersAccountNum"/>
                        </noticeAccNum>
                    </xsl:if>

                    <xsl:if test="//StartDateActive">
                        <noticeAccActivate>
                            <xsl:value-of select="//StartDateActive"/>
                        </noticeAccActivate>
                    </xsl:if>
                    <xsl:if test="//SI_TF_DOCSTATE">
                        <noticeDocumentStatus>
                            <xsl:value-of select="//SI_TF_DOCSTATE"/>
                        </noticeDocumentStatus>
                    </xsl:if>
                    <xsl:if test="//SI_TF_GUID">
                        <noticeGuid>
                            <xsl:value-of select="//SI_TF_GUID"/>
                        </noticeGuid>
                    </xsl:if>
                    <xsl:if test="//LockDocСause">
                        <noticeAccStopCause>
                            <xsl:value-of select="//LockDocСause"/>
                        </noticeAccStopCause>
                    </xsl:if>
                </tvr:rumbeCloseIPNotice>
            </lc:document>
        </lc:storeDocReq>
    </xsl:template>
</xsl:stylesheet>
