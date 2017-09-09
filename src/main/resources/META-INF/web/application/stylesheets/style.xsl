<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="rdf" 
  version="1.0">
  
  <xsl:import href="style-base.xsl"/> 

  <xsl:template
      match="node()" mode="header">
    <h1><a href="http://www.hp.com/"><img src="/application/images/hpc60_topnav_hp_logo" width="63" height="53" alt="hp.com home"  /></a>
		GVS</h1>
  </xsl:template> 
  
  <xsl:template
      match="node()" mode="htmlHead">
	<title>GVS - Graph Versioning System</title>
		<link rel="stylesheet" type="text/css" href="/application/stylesheets/gvs" />
		<link rel="stylesheet" type="text/css" media="print" href="/application/stylesheets/gvs-print" />
		<link rel="shortcut icon" type="image/x-icon" href="/application/images/favicon.ico" />
  </xsl:template>
  

  
  <xsl:template
      match="node()" mode="footer">
    	<div class="navigation">
			<a href="/application/gvs-browser">Back to the GVS-Browser</a>
		</div>
		<div id="footer" class="small">
		<div class="navigation">
		     <a href="http://validator.w3.org/check?uri=referer"><img
		        src="/application/images/valid-xhtml11"
		        alt="Valid XHTML 1.1" height="31" width="88" /></a> 
		</div>
		

		<div id="copyright" class="small"> &#169; 2006-2007 Hewlett-Packard Development Company,
		  L.P. </div>
		</div> 
  </xsl:template>
  
  <xsl:template
      match="node()" mode="bodyOnloadValue"></xsl:template>
  
 </xsl:stylesheet>