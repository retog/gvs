<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:db="http://discobits.org/ontology#"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="rdf dc db" 
  version="1.0">
  
  <xsl:import href="discobits"/>
  <xsl:import href="item-viewer"/>
  <xsl:import href="foaf-viewer"/>
  
  <xsl:template match="rdf:RDF" mode="htmlHead">
		<xsl:apply-imports/>
		<title>Combined stylesheet</title>
		<!-- <link rel="stylesheet" type="text/css" href="/application/stylesheets/discobits.css"/> -->
		<script type="text/javascript" src="/application/scripts/linking-serializer">
		</script>
		<script type="text/javascript">
			gvsBrowser = new Object();
		</script>
		<script type="text/javascript" src="/application/scripts/print-links/PrintLinks">
		</script>
  </xsl:template> 
  
  <xsl:template
      match="node()" mode="bodyOnloadValue">new PrintLinks()</xsl:template> 
      
  <xsl:template match="rdf:RDF" mode="main">
  	<div id="main">
  		<xsl:apply-templates select="." mode="mainContent"/>
	 </div>
  </xsl:template>
  
  <xsl:template match="rdf:RDF" mode="mainContent">
  	<xsl:apply-templates select="." mode="discoMainContent" />
  	<xsl:if test="rdf:Description[rdf:type/@rdf:resource = 'http://purl.org/rss/1.0/item']">
  	<h2>RSS-Items:</h2>
  	<xsl:apply-templates select="." mode="itemsMainContent" />
  	</xsl:if>
  	<xsl:if test="rdf:Description[(rdf:type/@rdf:resource = 'http://xmlns.com/foaf/0.1/Agent')  or (rdf:type/@rdf:resource = 'http://xmlns.com/foaf/0.1/Person')]">
	  	<h2>FOAF-Agents:</h2>
	  	<xsl:apply-templates select="." mode="foafMainContent" />
  	</xsl:if>
  	<p class="navigation">
  	<a href="#" id="combinedShowSourceLink" onclick="
  	gvsBrowser.combined.viewSource = true; 
  	document.getElementById('combinedSource').style.display = ''; 
  	document.getElementById('combinedShowSourceLink').style.display = 'none'; 
  	document.getElementById('combinedHideSourceLink').style.display = ''; 
  	return false;">show RDF/XML</a>
  	<a href="#" id="combinedHideSourceLink" style="display: none" onclick="
  	gvsBrowser.combined.viewSource = false; 
  	document.getElementById('combinedSource').style.display = 'none';
  	document.getElementById('combinedShowSourceLink').style.display = ''; 
  	document.getElementById('combinedHideSourceLink').style.display = 'none'; 
  	return false;">hide RDF/XML</a>
    <div id="combinedSource" style="display: none; background-color: #eeeeee"/>
    </p>
    <div id="originalData" style="display: none">
    	<xsl:copy-of select="/rdf:RDF" />
    </div>
    <script type="text/javascript">
    	if (!gvsBrowser.combined) {
    		gvsBrowser.combined = new Object();
    	}
    	if (gvsBrowser.combined.viewSource) {
    		document.getElementById('combinedSource').style.display = ''
    		document.getElementById('combinedHideSourceLink').style.display = ''; 
    		document.getElementById('combinedShowSourceLink').style.display = 'none'; 
    	}
    	LinkingSerializer.serializeToElement(document.getElementById('originalData').getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF")[0], document.getElementById('combinedSource'));
    </script>
  </xsl:template>
</xsl:stylesheet>