<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:db="http://discobits.org/ontology#"
  xmlns:rss="http://purl.org/rss/1.0/"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="rdf dc db rss" 
  version="1.0">
   
  <xsl:import href="style"/>

  

<xsl:template match="rdf:RDF" mode="htmlHead">
		<xsl:apply-imports/>
		<title>RSS Items</title>

  </xsl:template> 

<xsl:template match="rdf:RDF" mode="main">
  	<div id="main">
  		<xsl:apply-templates select="." mode="itemsMainContent"/>
	 </div>
  </xsl:template>

  <xsl:template match="rdf:RDF" mode="itemsMainContent">
		<xsl:for-each select="rdf:Description[rdf:type/@rdf:resource = 'http://purl.org/rss/1.0/item']">
			<xsl:sort select="dc:date" order="descending"/> 
			<h2><a href="{rss:link}"><xsl:value-of select="rss:title" /></a></h2> 
			<xsl:apply-templates select="rss:description" mode="description"/>
		</xsl:for-each> 
	</xsl:template>
	
	<xsl:template match="*" mode="description">
		<div style="border:1px solid red">
			<xsl:apply-imports/>
		</div>
	</xsl:template>
	
</xsl:stylesheet>