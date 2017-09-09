/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: GetCommand.java,v 1.2 2007/05/01 09:57:24 rebach Exp $
*/
package com.hp.hpl.jena.gvs.tool;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;

import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author reto
 *
 */
@CommandDescription (
		description = "Gets the currentversion of a graph from a store"
	)
public class GetCommand extends GVSToolCommand<GetCommandArguments> {

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.tool.GVSToolCommand#execute(com.hp.hpl.jena.gvs.Store, com.hp.hpl.jena.gvs.tool.GVSToolArguments)
	 */
	@Override
	protected void execute(Store store, GetCommandArguments arguments) {
		Source[] sources = arguments.getTrustedSources();
		Graph graph = store.getGraphOverTime(new HashSet<Source>(Arrays.asList(sources))).getGraph(new Date());
		Model model = JenaUtil.getModelFromGraph(graph);
		model.write(System.out);
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.tool.GVSToolCommand#getArgumentsClass()
	 */
	@Override
	protected Class<GetCommandArguments> getArgumentsClass() {
		return GetCommandArguments.class;
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

