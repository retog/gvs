<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:auth="http://gvs.hpl.hp.com/ontologies/authorization#"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="rdfs rdf dc" 
    version="1.0">

	
	<xsl:template match="/">
		<span>
 			<xsl:apply-templates select="rdf:RDF" /> 
		</span>
	</xsl:template>
	
	<xsl:template match="rdf:RDF">
		<a href="#" onclick="gvsBrowser.selectEditableSources(); return false;">Select editable</a><xsl:text> </xsl:text>
		<a href="#" onclick="gvsBrowser.selectAllSources(); return false;">Select all</a><xsl:text> </xsl:text>
		<a href="#" onclick="gvsBrowser.deselectAllSources(); return false;">Deselect all</a>
		<span id="filterArea">filter: <input id="sourceFilter" type="text" onchange="gvsBrowser.filterSources()"/></span>
		<table>
			<xsl:for-each select="rdf:Description[rdf:type/@rdf:resource = 'http://jena.hpl.hp.com/gvs/metamodel#Source']">
				<xsl:sort select="@rdf:about" /> 
				
				<xsl:variable name="uri" select="@rdf:about" />
				<tr class="sourceRow">
					<td><input type="checkbox" onclick="if (this.checked) gvsBrowser.addSourceURL('{@rdf:about}'); else gvsBrowser.removeSourceURL('{@rdf:about}')">
						<xsl:attribute name="class">sourceCB
						<xsl:if test="../rdf:Description/auth:mayImpersonate/@rdf:resource = @rdf:about">editableCB</xsl:if>
						<xsl:if test="@rdf:about = 'http://gvs.hpl.hp.com/welcome'">welcomeCB</xsl:if>
						</xsl:attribute>
					</input>
					<script type="text/javascript">
						gvsBrowser.registerSource("<xsl:value-of select="@rdf:about" />"<xsl:if test="../rdf:Description/auth:mayImpersonate/@rdf:resource = @rdf:about">, true</xsl:if>);
					</script>
					</td>
					<td><xsl:value-of select="@rdf:about" /></td>
					<td>
					<xsl:if test="../rdf:Description/auth:mayImpersonate/@rdf:resource = @rdf:about"><form action="edit-graph"><input type="hidden" name="source" value="{@rdf:about}"/> <input type="submit" name="edit" value="edit" /></form></xsl:if>
					</td>
				
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	
</xsl:stylesheet>