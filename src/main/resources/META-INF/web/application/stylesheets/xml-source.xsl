<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="rdfs rdf dc" 
    version="1.0">

	 <xsl:import href="xml-to-string"/> 
	
	<xsl:template match="/">
		<body>
				 <xsl:call-template name="xml-to-string">
		        	<xsl:with-param name="node-set" select="/rdf:RDF"/>
		        </xsl:call-template>
		</body>
	</xsl:template>
</xsl:stylesheet>