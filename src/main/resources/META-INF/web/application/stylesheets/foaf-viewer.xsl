<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:db="http://discobits.org/ontology#"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="rdf dc db foaf" 
  version="1.0">
  
   
  <xsl:import href="style"/> 
  <xsl:import href="knobot/foaf"/>

  

<xsl:template match="rdf:RDF" mode="htmlHead">
		<xsl:apply-imports/>
		<title>FOAF</title>
		<link rel="stylesheet" type="text/css" href="/application/stylesheets/foaf.css"/>
  </xsl:template> 

<xsl:template match="rdf:RDF" mode="main">
  	<div id="main">
  		<xsl:apply-templates select="." mode="foafMainContent"/>
	 </div>
  </xsl:template>

  <xsl:template match="rdf:RDF" mode="foafMainContent">
		<xsl:for-each select="rdf:Description[(rdf:type/@rdf:resource = 'http://xmlns.com/foaf/0.1/Agent') or (rdf:type/@rdf:resource = 'http://xmlns.com/foaf/0.1/Person')]">
			<xsl:sort select="foaf:name" order="ascending"/> 
			<xsl:if test="foaf:name and not(foaf:name = '')">
			<h3><xsl:value-of select="foaf:name" /></h3>
			<xsl:apply-templates select="." mode="agent">
	        	<xsl:with-param name="omitName">yes</xsl:with-param>
			</xsl:apply-templates>
			</xsl:if>
		</xsl:for-each> 
	</xsl:template>
	
	
</xsl:stylesheet>