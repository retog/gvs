<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
  exclude-result-prefixes="rdf rdfs" 
  version="1.0">

  <xsl:key name="byID" match="/rdf:RDF/rdf:Description" 
    use="@rdf:nodeID|@rdf:about"/>
  <!-- a generic container processor -->
  <xsl:template name="container-processor">
    <xsl:param name="container"/>
    <xsl:param name="single-template"/>
    <xsl:param name="attribute"/>
    <xsl:for-each select="$container/rdf:li">
      <xsl:apply-templates select="$single-template">
        <xsl:with-param name="item" 
          select="key('byID',@rdf:resource|@rdf:nodeID)"/>
        <xsl:with-param name="position" select="position()"/>
        <xsl:with-param name="root" select="/"/>
        <xsl:with-param name="attribute" select="$attribute"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>