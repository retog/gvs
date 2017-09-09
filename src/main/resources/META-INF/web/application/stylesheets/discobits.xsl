<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:db="http://discobits.org/ontology#"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="rdf dc db" 
  version="1.0">
  
  <xsl:import href="style"/>

  

<xsl:template match="rdf:RDF" mode="htmlHead">
		<xsl:apply-imports/>
		<title>Discobit</title>
		<!-- <link rel="stylesheet" type="text/css" href="/application/stylesheets/discobits.css"/> -->
		<style type="text/css">
			
		</style>
		<script type="text/javascript" src="/application/scripts/print-links/PrintLinks">
		</script>
  </xsl:template> 
		
  <xsl:template
      match="node()" mode="bodyOnloadValue">new PrintLinks()</xsl:template> 		
  <xsl:template match="rdf:RDF" mode="main">
  	<div id="main">
  		<xsl:apply-templates select="." mode="discoMainContent"/>
	 </div>
  </xsl:template>


  <xsl:template match="rdf:RDF" mode="discoMainContent">
    <xsl:for-each
        select="
        rdf:Description[((rdf:type/@rdf:resource = 'http://discobits.org/ontology#DiscoBit')
        or (rdf:type/@rdf:resource = 'http://discobits.org/ontology#TitledContent')
        or (rdf:type/@rdf:resource = 'http://discobits.org/ontology#XHTMLInfoDB')
        or (db:contains)
        ) and (@rdf:about)]" >
        <!-- find root: check that it is not PositionedDiscoBitand that it not contained somewhere-->
        <xsl:if test="not(/*/*[db:holds/@rdf:resource = current()/@rdf:about]/@rdf:nodeID = /*/*/db:contains/@rdf:nodeID)">
        	<xsl:apply-templates select="." mode="discoBit">
        		<xsl:with-param name="root">true</xsl:with-param>
        	</xsl:apply-templates>
        </xsl:if>
        
    </xsl:for-each>
  </xsl:template>

<xsl:template
      match="rdf:Description" mode="discoBit"  priority="1">
		<xsl:param name="root">false</xsl:param>
		<span class="db:infoBit">
			<xsl:copy-of select="db:infoBit/node()"/> <!--  db:infoBit/@*| -->
		</span>
  </xsl:template>

 <xsl:template
      match="rdf:Description[db:contains]" mode="discoBit"  priority="2">
		<xsl:param name="root">false</xsl:param>      
		<xsl:if test="$root = 'true'">
	      <a href="{@rdf:about}" onclick="gvsBrowser.addSelectedResource('{@rdf:about}'); return false"><img src="/application/images/arrow-left" alt="go to container"/></a><br />	
    	</xsl:if>
      <div name="db:components">      
	      <xsl:for-each select="key('byID',db:contains/@rdf:resource|db:contains/@rdf:nodeID)">
	      	<xsl:sort select="db:pos"/>
	      	<xsl:apply-templates select="." mode="entryBlock"/>
	      </xsl:for-each>
      </div> 
  </xsl:template>
  
  <xsl:template
      match="rdf:Description[rdf:type/@rdf:resource = 'http://discobits.org/ontology#TitledContent']" mode="discoBit" priority="3">
		<xsl:param name="root">false</xsl:param>      
    <input type="hidden" name="rdf:type/@rdf:resource" value="http://discobits.org/ontology#TitledContent"/>
    
      <h1>
	    <!-- <a href="{@rdf:about}" onclick="gvsBrowser.selectResource('{@rdf:about}'); return false">
	      	<img src="/application/images/select-current" alt="select current"/>
	      </a> -->
	      <xsl:apply-templates select="key('byID',db:contains/@rdf:resource|db:contains/@rdf:nodeID)[db:pos = 0]" mode="entry"/>
      </h1>
      <div class="contentSection">      
	      <xsl:for-each select="key('byID',db:contains/@rdf:resource|db:contains/@rdf:nodeID)[db:pos > 0]">
	      	<xsl:sort select="db:pos"/>
	      	<xsl:apply-templates select="." mode="entry"/>
	      </xsl:for-each>
      </div> 
  </xsl:template>
  
  <xsl:template
      match="rdf:Description" mode="entryBlock">
      <div class="entry">
      	<xsl:apply-templates select="." mode="entry" />
      </div>
  </xsl:template>

  

  <xsl:template
      match="rdf:Description" mode="entry">
        <xsl:if test="not(key('byID',db:holds/@rdf:resource|db:holds/@rdf:nodeID)/@rdf:about)">
      		<a href="{db:holds/@rdf:resource}" onclick="gvsBrowser.addSelectedResource('{db:holds/@rdf:resource}'); return false"><img src="/application/images/arrow-right" alt="go to contained"/></a><br />	
      	</xsl:if>
      	<xsl:apply-templates select="key('byID',db:holds/@rdf:resource|db:holds/@rdf:nodeID)" mode="discoBit"/>
  </xsl:template>
  
  
</xsl:stylesheet>