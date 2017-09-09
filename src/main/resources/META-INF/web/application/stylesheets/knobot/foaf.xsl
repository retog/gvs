<?xml version="1.0"?>


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:document="http://wymiwyg.org/ontologies/document#"
    xmlns:lang="http://wymiwyg.org/ontologies/language-selection#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:foaf="http://xmlns.com/foaf/0.1/"
    xmlns:addr="http://wymiwyg.org/ontologies/foaf/postaddress#"
    xmlns:role="http://wymiwyg.org/ontologies/foaf/role#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:trans="http://wymiwyg.org/ontologies/transaction#"
    xmlns:foafex="http://wymiwyg.org/ontologies/foaf/extensions#"
    xmlns="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="rdf document lang dc foaf addr role rdfs trans foafex"
    version="1.0">
  
  <xsl:import href="r3x-util"/>
  
  
  <!-- <xsl:key name="byID" match="/rdf:RDF/rdf:Description" 
    use="@rdf:nodeID|@rdf:about"/> -->
  
  <xsl:template match="rdf:Description" mode="agent">
    <xsl:param name="omitName">no</xsl:param>
    <xsl:choose>
      <xsl:when test="rdf:type/@rdf:resource ='http://wymiwyg.org/ontologies/foaf/role#Participation'">
        <xsl:apply-templates select="." mode="participation"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="personAndGroup">
        	<xsl:with-param name="omitName" select="$omitName"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose><xsl:text> </xsl:text>
  </xsl:template>
  
  <!-- Shows a business-card of the agent -->
  <xsl:template match="rdf:Description" mode="agentVCard">
      <div class="vcard">
      <xsl:apply-templates mode="agentShort" select="."/>
      <xsl:apply-templates mode="address" select="key('byID', 
          addr:address/@rdf:resource|addr:address/@rdf:nodeID)"/>
    </div>
  </xsl:template>
  
  <xsl:template match="rdf:Description" mode="personAndGroup">
  	<xsl:param name="omitName">no</xsl:param>
    <div class="personOrGroup">
      <xsl:apply-templates mode="personAndGroupShort" select=".">
      	<xsl:with-param name="omitName" select="$omitName"/>
      </xsl:apply-templates>
      <xsl:if test="foaf:knows">
        <h2>Knows:</h2>
        <xsl:apply-templates select="key('byID',foaf:knows/@rdf:nodeID|foaf:knows/@rdf:resource)" mode="foafReference" />
      </xsl:if>
      <xsl:if test="foafex:knownBy">
        <h2>Known by:</h2>
        <xsl:apply-templates select="key('byID',foafex:knownBy/@rdf:nodeID|foaf:knows/@rdf:resource)" mode="foafReference" />
      </xsl:if>
      <xsl:if test="role:participatesIn">
        <h3>Participations</h3>
        <xsl:for-each select="role:participatesIn">
          <xsl:apply-templates mode="participationShort"
            select="key('byID',@rdf:resource|@rdf:nodeID)"/>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="role:hasParticipation">
        <h3>Participants</h3>
        <xsl:for-each select="role:hasParticipation">
          <xsl:apply-templates mode="participationShort"
            select="key('byID',@rdf:resource|@rdf:nodeID)"/>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="role:providesRole">
        <h3>Roles</h3>
        <ul><xsl:for-each select="role:providesRole">
          <li><xsl:apply-templates mode="roleShort"
            select="key('byID',@rdf:resource|@rdf:nodeID)"/></li>
        </xsl:for-each></ul>
      </xsl:if>
    </div>
    
  </xsl:template>
  
  <xsl:template match="rdf:Description" mode="participation">
    <h2><xsl:value-of select="key('byID', role:group/@rdf:nodeID)/foaf:name" />, 
      <xsl:value-of select="key('byID', role:actsInRole/@rdf:nodeID
      |role:actsInRole/@rdf:resource)/rdfs:label|key('byID', role:actsInRole/@rdf:nodeID
      |role:actsInRole/@rdf:resource)/role:denotation" />: <xsl:value-of select="key('byID', role:participant/@rdf:nodeID)/foaf:name" /></h2>  
    <p>
    <xsl:apply-templates mode="participationShort"
      select="."/>
    </p>
    <h3>Personal</h3>
    <xsl:apply-templates mode="personAndGroupShort" 
      select="key('byID',role:participant/@rdf:nodeID)"/>
    <h3>Group</h3>
    <xsl:apply-templates mode="agentShort" 
      select="key('byID',role:group/@rdf:nodeID)"/>
    <!-- <xsl:call-template name="agentShort">
      <xsl:with-param name="agent" select="key('byID',role:group/@rdf:nodeID)"/>
    </xsl:call-template>-->
  </xsl:template>
  
  <xsl:template mode="roleShort" match="node()" >
	<div class="roleShort">
    		<h4><xsl:value-of select="rdfs:label" /></h4>
    		<xsl:if test="foaf:mbox">Mbox: <xsl:value-of select="foaf:mbox" /></xsl:if>
    		<xsl:for-each select="../rdf:Description[role:actsInRole/@rdf:nodeID = current()/@rdf:nodeID]">
    			<xsl:apply-templates select="." mode="participationShort" />
    		</xsl:for-each>
  	</div>
  </xsl:template>
  
  <xsl:template match="node()" mode="agentShort">
    <xsl:choose>
      <xsl:when test="rdf:type/@rdf:resource ='http://wymiwyg.org/ontologies/foaf/role#Participation'">
        <xsl:apply-templates select="." mode="participationShort"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="personAndGroupShort"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="node()" mode="personAndGroupShort">
  	<xsl:param name="omitName">no</xsl:param>
    <xsl:variable name="agent" select="."/>
    	<xsl:if test="$omitName = 'no'">
      		<xsl:apply-templates select="$agent/foaf:name" mode="langVariantsBR"/>
      	</xsl:if>
      <xsl:apply-templates select="$agent/foaf:nick" mode="labeledNode"/>
      <xsl:apply-templates select="$agent/foaf:mbox/@rdf:resource" mode="labeledLinkNode">
        <xsl:with-param name="label">Email</xsl:with-param>
      </xsl:apply-templates>
      <xsl:apply-templates select="$agent/foaf:homepage/@rdf:resource" mode="labeledLinkNode">
        <xsl:with-param name="label">Website</xsl:with-param>
      </xsl:apply-templates>
      <xsl:for-each select="$agent/foaf:depiction">
        <img  src="{@rdf:resource}" alt="Picture of {$agent/foaf:name}"/><br/>
      </xsl:for-each>
      <xsl:apply-templates select="$agent/foaf:phone/@rdf:resource" mode="labeledNode">
        <xsl:with-param name="label">Phone</xsl:with-param>
      </xsl:apply-templates>
      <!-- no such property <xsl:if test="$agent/foaf:personalProfileDocument">
        Personal profile at:<a href="{$agent/foaf:personalProfileDocument/@rdf:resource}"><xsl:value-of select="$agent/foaf:personalProfileDocument/@rdf:resource" /></a><br/>
      </xsl:if> -->

      <!--<xsl:for-each select="$agent/foaf:isPrimaryTopicOf">
        Profile at: <a href="{@rdf:resource}">
          <xsl:value-of select="@rdf:resource" /></a><br/>
      </xsl:for-each>-->
      <!-- <xsl:apply-templates select="$agent/foaf:isPrimaryTopicOf/@rdf:resource" mode="labeledLinkNode">
        <xsl:with-param name="label">Profile at</xsl:with-param>
      </xsl:apply-templates> -->
  </xsl:template>
  
  <xsl:template match="node()" mode="foafReference">
    <xsl:variable name="primaryDescription" select=
      "key('byID',foaf:isPrimaryTopicOf/@rdf:resource)[rdf:type/@rdf:resource 
      = 'http://wymiwyg.org/ontologies/rwcf#AuthoritativelyServedResource']"/>
    <xsl:variable name="caption">
      <xsl:choose>
        <xsl:when test="foaf:name">
          <xsl:value-of select="foaf:name" />
        </xsl:when>
        <xsl:otherwise>
          name unknown
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$primaryDescription">
        <a href="{$primaryDescription/@rdf:about}">
          <xsl:value-of select="$caption" />
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$caption" />
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="foaf:mbox">
      (<xsl:value-of select="foaf:mbox/@rdf:resource"/>)
    </xsl:if>
    <xsl:variable name="personalProfile" select=
      "key('byID',foaf:isPrimaryTopicOf/@rdf:resource)[rdf:type/@rdf:resource 
      = 'http://xmlns.com/foaf/0.1/PersonalProfileDocument']"/>
    <xsl:if test="$personalProfile"><xsl:text> </xsl:text><a href="{$personalProfile/@rdf:about}">PPD</a>
    </xsl:if>  
    <br/>
  </xsl:template>
  
  <xsl:template match="node()" mode="address">
    <h3>Address</h3>
    <xsl:variable name="location" select="key('byID', addr:location/@rdf:nodeID)"/>
    <xsl:variable name="specification" 
      select="key('byID', addr:serviceDeliveryPointSpecification/@rdf:nodeID)"/>
    <xsl:value-of select="$location/addr:thoroughfareType"/><xsl:text> </xsl:text><xsl:value-of select="$location/addr:thoroughfareName"/><xsl:text> </xsl:text> <xsl:value-of select="$location/addr:streetNr"/><br/>
    <xsl:if test='$specification/addr:deliveryServiceIndicator'>
      <xsl:value-of select="$specification/addr:deliveryServiceType"/> <xsl:value-of select="$specification/addr:deliveryServiceIndicator"/><br/>
    </xsl:if>
	 <xsl:value-of select="$specification/addr:postcode"/><xsl:text> </xsl:text><xsl:value-of select="$location/addr:town"/><br/>
  </xsl:template>
  
  <xsl:template match="node()" mode="participationShort">
    <xsl:variable name="participation" select="."/>
    <div class="participation">
      <a href="{$participation/foaf:isPrimaryTopicOf/@rdf:resource}"><xsl:value-of select="key('byID', $participation/role:group/@rdf:nodeID)/foaf:name" />, 
      <xsl:value-of select="key('byID', $participation/role:actsInRole/@rdf:nodeID
      |$participation/role:actsInRole/@rdf:resource)/rdfs:label|key('byID', $participation/role:actsInRole/@rdf:nodeID
      |$participation/role:actsInRole/@rdf:resource)/role:denotation" /></a>: <xsl:value-of select="key('byID', $participation/role:participant/@rdf:nodeID)/foaf:name" /><br/>
      <xsl:apply-templates select="$participation/foaf:phone/@rdf:resource" mode="labeledNode">
        <xsl:with-param name="label">Phone</xsl:with-param>
      </xsl:apply-templates>
      <xsl:apply-templates select="$participation/foaf:homepage/@rdf:resource" mode="labeledNode">
        <xsl:with-param name="label">Website</xsl:with-param>
      </xsl:apply-templates>
      <xsl:apply-templates select="$participation/foaf:mbox/@rdf:resource" mode="labeledLinkNode">
        <xsl:with-param name="label">Email</xsl:with-param>
      </xsl:apply-templates>
    </div>
  </xsl:template>
  
  <xsl:template match="node()|@rdf:resource" mode="labeledNode">
    <xsl:param name="label" />
    <xsl:if test="$label">
      <xsl:value-of select="$label"/>:
    </xsl:if>
    <xsl:value-of select="."/><br/>
  </xsl:template>
  
  <xsl:template match="node()|@rdf:resource" mode="labeledLinkNode">
    <xsl:param name="label" />
    <xsl:if test="$label">
      <xsl:value-of select="$label"/>:
    </xsl:if>
    <a href="{.}"><xsl:value-of select="."/></a><br/>
  </xsl:template>
  
  <xsl:template match="node()" mode="langVariantsBR">
    <xsl:value-of select="."/>
    <xsl:if test="@xml:lang">
      (<xsl:value-of select="@xml:lang"/>)
    </xsl:if>
    <br/>
  </xsl:template>
</xsl:stylesheet>