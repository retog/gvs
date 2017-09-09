/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: UpdateCommand.java,v 1.4 2007/05/02 10:52:47 rebach Exp $
*/
package com.hp.hpl.jena.gvs.tool;

import java.io.File;
import java.net.MalformedURLException;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;

import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author reto
 *
 */
@CommandDescription (
		description = "Updates a model in a store from a file"
	)
public class UpdateCommand extends GVSToolCommand<UpdateAssertCommandArguments> {

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.tool.GVSToolCommand#execute(com.hp.hpl.jena.gvs.Store, com.hp.hpl.jena.gvs.tool.GVSToolArguments)
	 */
	@Override
	protected void execute(Store store, UpdateAssertCommandArguments arguments) {
		File uploadFile = arguments.getUploadFile();
		Model model = ModelFactory.createDefaultModel();
		String fileURLString;
		try {
			fileURLString = uploadFile.toURL().toString();
		} catch (MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
		String lang = "RDF/XML";
		if (fileURLString.endsWith(".nt")) {
			lang = "N-TRIPLE";
		} else {
			if (fileURLString.endsWith(".n3") || fileURLString.endsWith(".turtle")) {
				lang = "N3";
			}
		}
		model.read(fileURLString, lang);
		Graph graph = JenaUtil.getGraphFromModel(model, true);
		Source identity = arguments.getIdentity();
		store.updateGraph(identity, new FCAGraphImpl(graph));	
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.tool.GVSToolCommand#getArgumentsClass()
	 */
	@Override
	protected Class<UpdateAssertCommandArguments> getArgumentsClass() {
		return UpdateAssertCommandArguments.class;
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

