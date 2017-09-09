/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: ServicesCommand.java,v 1.2 2007/05/16 14:25:26 rebach Exp $
 */
package com.hp.hpl.jena.gvs.tool;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wymiwyg.rdf.graphs.Graph;
import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;
import org.wymiwyg.rdf.graphs.jenaimpl.JenaUtil;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.gvs.Clock;
import com.hp.hpl.jena.gvs.GraphOverTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.Store;
import com.hp.hpl.jena.gvs.impl.FCAGraphImpl;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.services.SERVICES;
import com.hp.hpl.jena.gvs.services.Service;
import com.hp.hpl.jena.gvs.services.ServiceFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 * 
 */
@CommandDescription(description = "Start services as described in a graph")
public class ServicesCommand extends GVSToolCommand<ServicesCommandArguments> {

	private static final Log log = LogFactory.getLog(ServicesCommand.class);
	private static final Source defaultConfigSource = new SourceImpl(
			"http://gvs.hpl.hp.com/default-config#");

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.tool.GVSToolCommand#execute(com.hp.hpl.jena.gvs.Store,
	 *      com.hp.hpl.jena.gvs.tool.GVSToolArguments)
	 */
	@Override
	protected void execute(Store store, ServicesCommandArguments arguments) {
		/*
		 * File directory; try { directory = new
		 * File(getClass().getResource("defaultconfig").toURI()); } catch
		 * (URISyntaxException e) { throw new RuntimeException(e); } String
		 * baseURLString = "http://gvs.hpl.hp.com/";
		 * CannedDataReader.readCannedData(store, directory, baseURLString);
		 */

		Set<Source> trustedSources = new HashSet<Source>();
		for (SourceImpl source : arguments.getTrustedSources()) {
			trustedSources.add(source);
		}
		// trustedSources.addAll(Arrays.asList(arguments.getTrustedSources()));
		if (trustedSources.contains(defaultConfigSource)) {
			ensureDefaultConfigLoaded(store);
		}
		Graph serviceConfig = store.getGraphOverTime(trustedSources).getGraph(
				new Date());
		Model serviceConfigModel = JenaUtil.getModelFromGraph(serviceConfig);
		serviceConfigModel.write(System.out);
		ResIterator servicesIter = serviceConfigModel.listSubjectsWithProperty(RDF.type, SERVICES.Service);
		while (servicesIter.hasNext()) {
			try {
				launchService(store, servicesIter.nextResource());
			} catch (Exception e) {
				log.error("Error launching service", e);
			}
		}

	}

	/**
	 * @param store 
	 * @param serviceResource
	 */
	private void launchService(Store store, Resource serviceResource) {
		String factoryClassName = serviceResource.getRequiredProperty(SERVICES.factoryJavaClass).getString();
		Class<? extends ServiceFactory> serviceFactoryClass;
		try {
			serviceFactoryClass = Class.forName(factoryClassName).asSubclass(ServiceFactory.class);
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		ServiceFactory serviceFactory;
		try {
			serviceFactory = serviceFactoryClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		Set<Source> trustedSources = getTrustedSources(serviceResource);
		Source identity = new SourceImpl(serviceResource.getRequiredProperty(SERVICES.identity).getResource().getURI());
		trustedSources.add(identity); //trust yourself
		NamedNode configuration = new NamedNodeImpl(serviceResource.getRequiredProperty(SERVICES.configuration).getResource().getURI());
		final Service service  = serviceFactory.getService(store, identity, trustedSources, configuration);
		new Thread() {
			public void run() {
				service.start();
			}
		}.start();
		
		
	}

	/**
	 * @param serviceResource
	 * @return
	 */
	private Set<Source> getTrustedSources(Resource serviceResource) {
		Set<Source> result = new HashSet<Source>();
		StmtIterator trustProrpertyStmts = serviceResource.listProperties(SERVICES.trustedSource);
		while (trustProrpertyStmts.hasNext()) {
			Statement propertyStmt = trustProrpertyStmts.nextStatement();
			result.add(new SourceImpl(propertyStmt.getResource().getURI()));
		}
		return result;
	}

	/**
	 * @param store
	 * 
	 */
	private void ensureDefaultConfigLoaded(Store store) {
		Model cannedDefaultConfigModel = ModelFactory.createDefaultModel();
		URL cannedDefaultConfigURL = getClass()
				.getResource("defaultconfig.rdf");
		String cannedDefaultConfigURLString = cannedDefaultConfigURL.toString();
		try {
			InputStream cannedDefaultConfigInputStream = cannedDefaultConfigURL
					.openStream();
			cannedDefaultConfigModel.read(cannedDefaultConfigInputStream,
					cannedDefaultConfigURLString);
			cannedDefaultConfigInputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Resource cannedDefaultConfigResource = cannedDefaultConfigModel
				.getResource(cannedDefaultConfigURLString+"#");
		Statement cannedVersionDateStmt = cannedDefaultConfigResource
				.getRequiredProperty(DC.date);
		final Date cannedVersionDate = ((XSDDateTime) cannedVersionDateStmt
				.getLiteral().getValue()).asCalendar().getTime();
		cannedVersionDateStmt.remove();
		
		if (isDefaultConfigLoadingNeeded(store, cannedVersionDate)) {
			Clock origClock = store.getClock();
			store.setClock(new Clock() {

				public Date getTime() {
					return cannedVersionDate;
				}
				
			});
			store.updateGraph(defaultConfigSource, new FCAGraphImpl(
					cannedDefaultConfigModel));
			store.setClock(origClock);
		}

	}

	/**
	 * @param store
	 * @param cannedVersionDate
	 * @return
	 */
	private boolean isDefaultConfigLoadingNeeded(Store store,
			Date cannedVersionDate) {
		GraphOverTime configOverTime = store.getGraphOverTime(Collections
				.singleton(defaultConfigSource));
		Iterator<Date> followingChanges = configOverTime
				.followingChanges(new Date(cannedVersionDate.getTime() - 1));
		return !followingChanges.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.tool.GVSToolCommand#getArgumentsClass()
	 */
	@Override
	protected Class<ServicesCommandArguments> getArgumentsClass() {
		return ServicesCommandArguments.class;
	}

}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

