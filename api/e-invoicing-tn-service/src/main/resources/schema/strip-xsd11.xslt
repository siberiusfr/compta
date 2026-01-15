<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSLT pour convertir XSD 1.1 en XSD 1.0 compatible JAXB
  Supprime: xs:assert, xs:alternative, attributs vc:*
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:vc="http://www.w3.org/2007/XMLSchema-versioning">

  <!-- Copier tout par défaut -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Supprimer xs:assert (XSD 1.1) -->
  <xsl:template match="xs:assert"/>

  <!-- Supprimer xs:alternative (XSD 1.1) -->
  <xsl:template match="xs:alternative"/>

  <!-- Supprimer les attributs vc:* (versioning) -->
  <xsl:template match="@vc:*"/>

  <!-- Supprimer xs:openContent (XSD 1.1) -->
  <xsl:template match="xs:openContent"/>

  <!-- Supprimer xs:defaultOpenContent (XSD 1.1) -->
  <xsl:template match="xs:defaultOpenContent"/>

</xsl:stylesheet>
