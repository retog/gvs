/*
 (c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: ModelMetaStore.java,v 1.8 2007/06/27 12:50:16 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.StoreException;
import com.hp.hpl.jena.gvs.storage.MetaStore;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author reto
 * 
 */
public class ModelMetaStore implements MetaStore {

	private Model metaModel;
	private Set<Source> sources;

	/**
	 * @param metaModel
	 *            the model on which this <code>MetaStore</code> is based
	 */
	public ModelMetaStore(Model metaModel) {
		this.metaModel = metaModel;
		sources = readSourcesFromMetaModel();
	}

	/**
	 * @return
	 */
	private Set<Source> readSourcesFromMetaModel() {
		Set<Source> result = new HashSet<Source>();
		StmtIterator stmtIterator = metaModel.listStatements(null,
				METAMODEL.asserter, (Resource) null);
		while (stmtIterator.hasNext()) {
			result.add(new SourceImpl(stmtIterator.nextStatement()
					.getResource().getURI()));
		}
		return result;
	}

	public boolean assertComponent(Source source, NamedNode moleculeRef,
			Date moment, Resource type) {
		if (isAsserting(source, moleculeRef, moment, type)) {
			return false;
		}
		sources.add(source);
		Resource moleculeResource = metaModel.createResource(moleculeRef
				.getURIRef());
		Resource assertion = metaModel.createResource();
		assertion.addProperty(METAMODEL.asserter, metaModel
				.createResource(source.getURIRef()));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(moment);
		Literal momentLit = metaModel.createTypedLiteral(new XSDDateTime(
				calendar));
		/*
		 * Literal momentLit = metaModel.createTypedLiteral( new
		 * MillisDateFormat().format(moment), XSDDatatype.XSDdateTime);
		 */
		assertion.addProperty(METAMODEL.assertionTime, momentLit);
		assertion.addProperty(METAMODEL.assertedComponent, moleculeResource);
		moleculeResource.addProperty(RDF.type, type);
		return true;

	}

	private boolean isAsserting(Source source, NamedNode moleculeRef,
			Date moment, Resource type) {
		return getAssertionOf(source, moleculeRef, moment) != null;
	}

	public Set<NamedNode> getAsserted(Set<Source> sources, Date moment,
			Resource type) {
		Set<NamedNode> result = new HashSet<NamedNode>();
		for (Source source : sources) {
			Resource sourceRes = metaModel.createResource(source.getURIRef());
			ResIterator assertions = metaModel.listSubjectsWithProperty(
					METAMODEL.asserter, sourceRes);
			while (assertions.hasNext()) {
				Resource assertion = assertions.nextResource();
				Literal assertionTimeLit = assertion.getProperty(
						METAMODEL.assertionTime).getLiteral();
				Resource assertedComponent = assertion.getProperty(
						METAMODEL.assertedComponent).getResource();
				if (!assertedComponent.hasProperty(RDF.type, type)) {
					continue;
				}

				Date assertionTime;
				assertionTime = ((XSDDateTime) assertionTimeLit.getValue())
						.asCalendar().getTime();
				/*
				 * try { assertionTime = new MillisDateFormat()
				 * .parse(assertionTimeLit.getLexicalForm()); } catch
				 * (ParseException e) { throw new RuntimeException(e); }
				 */
				if (moment.before(assertionTime)) {
					continue;
				}
				Statement revocationTimeStmt = assertion
						.getProperty(METAMODEL.revocationTime);
				if (revocationTimeStmt != null) {
					Literal revocationTimeLit = revocationTimeStmt.getLiteral();
					Date revocationTime = ((XSDDateTime) revocationTimeLit
							.getValue()).asCalendar().getTime();
					// not before rather than after as in the moment of
					// revocation it is revoked
					if (!moment.before(revocationTime)) {
						continue;
					}
				}

				result.add(new NamedNodeImpl(assertedComponent.getURI()));
			}
		}
		return result;
	}

	public void revokeComponent(Source source, NamedNode moleculeRef,
			Date moment) {
		Resource revokingComponentRes = getAssertionOf(source, moleculeRef,
				moment);
		if (revokingComponentRes == null) {
			throw new StoreException("Assertion not found");
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(moment);
		Literal momentLit = metaModel.createTypedLiteral(new XSDDateTime(
				calendar));
		/*
		 * Literal momentLit = metaModel.createTypedLiteral( new
		 * MillisDateFormat().format(moment), XSDDatatype.XSDdateTime);
		 */
		revokingComponentRes.addProperty(METAMODEL.revocationTime, momentLit);

	}

	public Iterator<Date> getFollowingChanges(Set<Source> sources, Date moment) {
		return getDateList(sources, moment, true);
	}

	public Iterator<Date> getPreviousChanges(Set<Source> sources, Date moment) {
		return getDateList(sources, moment, false);
	}

	private Resource getAssertionOf(Source source, NamedNode componentName,
			Date moment) {
		Resource sourceRes = metaModel.createResource(source.getURIRef());
		Resource component = metaModel
				.createResource(componentName.getURIRef());
		ResIterator assertionsOfComponent = metaModel.listSubjectsWithProperty(
				METAMODEL.assertedComponent, component);
		while (assertionsOfComponent.hasNext()) {
			Resource assertion = assertionsOfComponent.nextResource();
			if (!assertion.hasProperty(METAMODEL.asserter, sourceRes)) {
				continue;
			}
			Literal assertionTimeLit = assertion.getProperty(
					METAMODEL.assertionTime).getLiteral();
			Date assertionTime;
			assertionTime = ((XSDDateTime) assertionTimeLit.getValue())
					.asCalendar().getTime();

			if (moment.before(assertionTime)) {
				continue;
			}
			Statement revocationTimeStmt = assertion
					.getProperty(METAMODEL.revocationTime);
			if (revocationTimeStmt != null) {
				Literal revocationTimeLit = revocationTimeStmt.getLiteral();
				Date revocationTime = ((XSDDateTime) revocationTimeLit
						.getValue()).asCalendar().getTime();

				// not before rather than after as in the moment of
				// revocation it is revoked
				if (!moment.before(revocationTime)) {
					continue;
				}
			}
			return assertion;
		}
		return null;

	}

	/**
	 * 
	 * @param sources
	 * @param moment
	 * @param forward
	 *            determines also the order of the list
	 * @return
	 */
	private Iterator<Date> getDateList(Set<Source> sources, Date moment,
			final boolean forward) {

		Set<Date> resultDateSet = new HashSet<Date>();
		for (Source source : sources) {
			Resource sourceRes = metaModel.createResource(source.getURIRef());
			ResIterator assertions = metaModel.listSubjectsWithProperty(
					METAMODEL.asserter, sourceRes);
			while (assertions.hasNext()) {
				Resource assertion = assertions.nextResource();
				Literal assertionTimeLit = assertion.getProperty(
						METAMODEL.assertionTime).getLiteral();
				Date assertionTime;
				try {
					assertionTime = ((XSDDateTime) assertionTimeLit.getValue())
							.asCalendar().getTime();
					/*
					 * assertionTime = new MillisDateFormat()
					 * .parse(assertionTimeLit.getLexicalForm());
					 */
				} catch (/* Parse */Exception e) {
					throw new RuntimeException(e);
				}
				if (forward) {
					if (assertionTime.after(moment)) {
						resultDateSet.add(assertionTime);
					}
				} else {
					if (!assertionTime.after(moment)) {
						resultDateSet.add(assertionTime);
					}
				}
				Statement revocationTimeStmt = assertion
						.getProperty(METAMODEL.revocationTime);
				if (revocationTimeStmt != null) {
					Literal revocationTimeLit = revocationTimeStmt.getLiteral();
					Date revocationTime;
					revocationTime = ((XSDDateTime) revocationTimeLit
							.getValue()).asCalendar().getTime();
					/*
					 * try { revocationTime = new MillisDateFormat()
					 * .parse(revocationTimeLit.getLexicalForm()); } catch
					 * (ParseException e) { throw new RuntimeException(e); }
					 */
					Date momentAfterRevocation = revocationTime;// new
					// Date(revocationTime.getTime()
					// + 1);
					if (forward) {
						if (momentAfterRevocation.after(moment)) {
							resultDateSet.add(momentAfterRevocation);
						}
					} else {
						if (!momentAfterRevocation.after(moment)) {
							resultDateSet.add(momentAfterRevocation);
						}
					}
				}
			}
		}

		List<Date> result = new ArrayList<Date>(resultDateSet);
		Collections.sort(result, new Comparator<Date>() {

			public int compare(Date d1, Date d2) {
				if (forward) {
					return d1.compareTo(d2);
				} else {
					return -d1.compareTo(d2);
				}
			}

		});
		return result.iterator();
	}

	public Set<Source> getSources() {
		return sources;
	}

	@Override
	public String toString() {
		StringWriter resultWriter = new StringWriter();
		resultWriter.write("ModelMetaStore\n");
		metaModel.write(resultWriter);
		return resultWriter.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MetaStore#getAssertions(com.hp.hpl.jena.gvs.Source,
	 *      java.util.Date, com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Set<NamedNode> getAssertions(Source source, Date moment,
			Resource type) {
		Resource sourceRes = metaModel.createResource(source.getURIRef());
		ResIterator assertions = metaModel.listSubjectsWithProperty(
				METAMODEL.asserter, sourceRes);
		Set<NamedNode> result = new HashSet<NamedNode>();
		while (assertions.hasNext()) {
			Resource assertion = assertions.nextResource();
			Literal assertionTimeLit = assertion.getProperty(
					METAMODEL.assertionTime).getLiteral();
			Resource assertedComponent = assertion.getProperty(
					METAMODEL.assertedComponent).getResource();
			if (!assertedComponent.hasProperty(RDF.type, type)) {
				continue;
			}

			Date assertionTime;
			assertionTime = ((XSDDateTime) assertionTimeLit.getValue())
					.asCalendar().getTime();
			if (moment.equals(assertionTime)) {
				result.add(new NamedNodeImpl(assertedComponent.getURI()));
			}

		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MetaStore#getRevocations(com.hp.hpl.jena.gvs.Source,
	 *      java.util.Date, com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Set<NamedNode> getRevocations(Source source, Date moment,
			Resource type) {
		Resource sourceRes = metaModel.createResource(source.getURIRef());
		ResIterator assertions = metaModel.listSubjectsWithProperty(
				METAMODEL.asserter, sourceRes);
		Set<NamedNode> result = new HashSet<NamedNode>();
		while (assertions.hasNext()) {
			Resource assertion = assertions.nextResource();
			Resource assertedComponent = assertion.getProperty(
					METAMODEL.assertedComponent).getResource();
			if (!assertedComponent.hasProperty(RDF.type, type)) {
				continue;
			}
			Statement revocationTimeStmt = assertion
					.getProperty(METAMODEL.revocationTime);
			if (revocationTimeStmt != null) {
				Literal revocationTimeLit = revocationTimeStmt.getLiteral();
				Date revocationTime = ((XSDDateTime) revocationTimeLit
						.getValue()).asCalendar().getTime();
				if (moment.equals(revocationTime)) {
					result.add(new NamedNodeImpl(assertedComponent.getURI()));
				}
			}

		}
		return result;
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

