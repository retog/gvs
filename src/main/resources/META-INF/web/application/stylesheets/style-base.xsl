<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="rdf" 
  version="1.0">
  
  <xsl:key name="byID" match="/rdf:RDF/rdf:Description" 
    use="@rdf:nodeID|@rdf:about"/>
  
  <xsl:output indent="yes"
      method="xml"
      omit-xml-declaration="yes" 
      doctype-public="-//W3C//DTD XHTML 1.1//EN"
      doctype-system="http://www.w3.org/TR/2001/REC-xhtml11-20010531/DTD/xhtml11-flat.dtd"/>
      
  <xsl:template
      match="/">
    <xsl:apply-templates select="/rdf:RDF" />
  </xsl:template>
  
  <xsl:template
      match="/rdf:RDF">
    <html>
      <head>
        <xsl:apply-templates select="." mode="htmlHead"/>
      </head>
      <xsl:variable name="bodyOnloadValue">
        <xsl:apply-templates select="." mode="bodyOnloadValue"/>
      </xsl:variable>
      <body onload="{$bodyOnloadValue}">
      <div id="head">
          <xsl:apply-templates select="." mode="header"/>
        </div>
        <div id="body">
          <div id="content">
            <xsl:apply-templates select="." mode="main"/>
            <xsl:comment>end of main</xsl:comment>
          </div>
        </div>
        <div id="footer">
          <xsl:apply-templates mode="footer" select="."/>
        </div>
      </body>
    </html>
  </xsl:template>
  
  <!-- the following templates are typically overridden -->
  
    <xsl:template
      match="node()" mode="header">
    HEADER
  </xsl:template>
  
  <xsl:template
      match="node()" mode="htmlHead">

  </xsl:template>
  
  <xsl:template
      match="node()" mode="main">
    MAIN
  </xsl:template>
  
  <xsl:template
      match="node()" mode="footer">
    FOOTER
  </xsl:template>
  
  <xsl:template
      match="node()" mode="bodyOnloadValue"></xsl:template>
  
 </xsl:stylesheet>