/* CVS $Id: AUTHORIZATION.java,v 1.3 2007/06/07 13:51:10 rebach Exp $ */
package com.hp.hpl.jena.gvs.security; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from /home/reto/workspace/gvs/rdf/authorization.rdf 
 * @author Auto-generated by schemagen on 07 Jun 2007 16:04 
 */
public class AUTHORIZATION {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://gvs.hpl.hp.com/ontologies/authorization#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    public static final Property mayRead = m_model.createProperty( "http://gvs.hpl.hp.com/ontologies/authorization#mayRead" );
    
    public static final Property mayImpersonate = m_model.createProperty( "http://gvs.hpl.hp.com/ontologies/authorization#mayImpersonate" );
    
    public static final Resource ClockMaster = m_model.createResource( "http://gvs.hpl.hp.com/ontologies/authorization#ClockMaster" );
    
}
