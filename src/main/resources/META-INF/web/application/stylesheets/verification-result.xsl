<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:am="http://gvs.hpl.hp.com/ontologies/account-manager#"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="rdfs rdf dc" 
    version="1.0">

	<xsl:import href="style"/>
	<xsl:template
      match="node()" mode="main">
    The password for user <xsl:value-of select="/rdf:RDF/rdf:Description/am:userName"/> has been set.
  </xsl:template>

</xsl:stylesheet>