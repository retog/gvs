/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: DefaultPolicy.java,v 1.6 2007/06/07 13:51:09 rebach Exp $
*/
package com.hp.hpl.jena.gvs.security;

import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;

import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 *
 */
public class DefaultPolicy extends Policy {
	
	//private final static Log log = LogFactory.getLog(DefaultPolicy.class);
	private GraphOverTime configGOT;

	/**
	 * @param configGOT
	 */
	public DefaultPolicy(GraphOverTime configGOT) {
		this.configGOT = configGOT;
	}


	/* (non-Javadoc)
	 * @see java.security.Policy#getPermissions(java.security.CodeSource)
	 */
	@Override
	public PermissionCollection getPermissions(CodeSource codeSource) {
		PermissionCollection result = new Permissions();
		return result;
	}
	
	/**
	 * @param username
	 * @return
	 */
	private Resource getUserResource(String username) {
		Graph graph  = configGOT.getGraph(new Date());
		Model model = JenaUtil.getModelFromGraph(graph);
		ResIterator resIter = model.listSubjectsWithProperty(ACCOUNTMANAGER.userName, username);
		if (resIter.hasNext()) {
			return resIter.nextResource();
		} else {
			return model.createResource();
		}
	}

	/* (non-Javadoc)
	 * @see java.security.Policy#refresh()
	 */
	@Override
	public void refresh() {

	}

	@Override
	public PermissionCollection getPermissions(ProtectionDomain domain) {
		Principal[] principals = domain.getPrincipals();
		GVSPrincipal[] gvsPrincipals = getGVSPrincipals(principals);

		PermissionCollection result = new Permissions();
		 
		/*if (gvsPrincipals.length == 0) {
				//allow everything
				result.add(new GVSImpersonatePermission());
				result.add(new GVSSetClockPermission());
		} else {*/
			//Resource userRes = gvsPrincipals[0].getUserResource(); 
			//This variant ensures up-to-date authorization
			for (GVSPrincipal principal : gvsPrincipals) {
				if (principal == SuperUserGVSPrincipal.instance) {
					result.add(new GVSImpersonatePermission());
					result.add(new GVSSetClockPermission());
				}
				Resource userRes = getUserResource(principal.getUserName());
				if (userRes.hasProperty(RDF.type, AUTHORIZATION.ClockMaster));
				StmtIterator impersonateStmt = userRes.listProperties(AUTHORIZATION.mayImpersonate);
				while (impersonateStmt.hasNext()) {
					result.add(new GVSImpersonatePermission(new SourceImpl(impersonateStmt.nextStatement().getResource().toString())));
				}
			}
			
		//}
		return result;
		//return super.getPermissions(domain);
	}

	/**
	 * @param principals
	 * @return
	 */
	private GVSPrincipal[] getGVSPrincipals(Principal[] principals) {
		List<GVSPrincipal> resList = new ArrayList<GVSPrincipal>();
		for (Principal principal : principals) {
			if (principal instanceof GVSPrincipal) {
				resList.add((GVSPrincipal)principal);
			}
		}
		return resList.toArray(new GVSPrincipal[resList.size()]);
	}

}


/*
    (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

