/*
	(c) Copyright 2005, 2006, Hewlett-Packard Development Company, LP
  	[See end of file]
 	$Id: NativeMemoryMetaStore.java,v 1.5 2007/06/27 12:50:14 rebach Exp $
 */
package com.hp.hpl.jena.gvs.impl.memory;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.wymiwyg.rdf.graphs.NamedNode;
import org.wymiwyg.rdf.graphs.impl.NamedNodeImpl;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.gvs.Source;
import com.hp.hpl.jena.gvs.StoreException;
import com.hp.hpl.jena.gvs.impl.METAMODEL;
import com.hp.hpl.jena.gvs.impl.SourceImpl;
import com.hp.hpl.jena.gvs.storage.MetaStore;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * An implementation of <code>MetaStore</code> using HashMaps and TreeSet to
 * store the data.
 * 
 * @author reto
 */
public class NativeMemoryMetaStore implements MetaStore {

	/**
	 * Allows to associate revocations and assertions (events) to a moment. For
	 * equals/hashcode and sorting only the date is relevant, the using code has
	 * to make sure all events happening at the same moment are stored in the
	 * same object. The natural order is the inverse of Date (i.e. more recent
	 * dates are 'smaller')
	 * 
	 * @author reto
	 * 
	 */
	private class RelevantMoment implements Comparable<RelevantMoment> {

		Set<SourceComponent> assertions = new HashSet<SourceComponent>();
		Date moment;
		Set<SourceComponent> revocations = new HashSet<SourceComponent>();

		/**
		 * the source that in this moment revoke all except what the assert in
		 * this moment
		 */
		Set<Source> revokeAllSources = new HashSet<Source>();

		RelevantMoment(Date moment) {
			this.moment = moment;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(RelevantMoment o) {
			return -moment.compareTo(o.moment);
		}

		@Override
		public boolean equals(Object obj) {
			RelevantMoment other = (RelevantMoment) obj;
			return other.moment.equals(moment);
		}

		@Override
		public int hashCode() {
			return moment.hashCode();
		}
	}

	/**
	 * associated a source and a component
	 */
	private class SourceComponent {
		NamedNode moleculeRef;
		Source source;

		SourceComponent(Source source, NamedNode moleculeRef) {
			this.source = source;
			this.moleculeRef = moleculeRef;
		}

		@Override
		public boolean equals(Object obj) {
			SourceComponent other = (SourceComponent) obj;
			return other.moleculeRef.equals(moleculeRef)
					&& source.equals(source);
		}

		@Override
		public int hashCode() {
			return moleculeRef.hashCode() ^ source.hashCode();
		}
	}

	private Map<NamedNode, Resource> componentTypeMap = new HashMap<NamedNode, Resource>();
	private SortedSet<Date> eventDates = new TreeSet<Date>();
	private Map<Date, RelevantMoment> relevantMomentsMap = new HashMap<Date, RelevantMoment>();
	private Map<SourceComponent, SortedSet<RelevantMoment>> whatWhenMap = new HashMap<SourceComponent, SortedSet<RelevantMoment>>();
	private Map<Source, SortedSet<RelevantMoment>> whoWhat = new HashMap<Source, SortedSet<RelevantMoment>>();

	public boolean assertComponent(Source source, NamedNode moleculeRef,
			Date moment, Resource type) {
		SourceComponent sourceComponent = new SourceComponent(source,
				moleculeRef);
		componentTypeMap.put(moleculeRef, type);
		SortedSet<RelevantMoment> existingMoments = whatWhenMap
				.get(sourceComponent);
		RelevantMoment relevantMoment;
		if (existingMoments != null) {
			RelevantMoment lastMoment = existingMoments.first();
			if (lastMoment.assertions.contains(sourceComponent)) {
				return false;
			}
			// otherwise the component was revoked in the last relevant moment
			// for this sourceComponent
			if (lastMoment.moment.equals(moment)) {
				relevantMoment = lastMoment;
			} else {
				relevantMoment = getRelevantMoment(moment);
				existingMoments.add(relevantMoment);
				putToValueSet(whoWhat, source, relevantMoment);
			}
		} else {
			existingMoments = new TreeSet<RelevantMoment>();
			whatWhenMap.put(sourceComponent, existingMoments);
			relevantMoment = getRelevantMoment(moment);
			existingMoments.add(relevantMoment);
			putToValueSet(whoWhat, source, relevantMoment);
		}
		relevantMoment.assertions.add(sourceComponent);
		return true;
	}

	public Set<NamedNode> getAsserted(Set<Source> sources, Date moment,
			Resource type) {
		// Go back the relevantMoment for every source till there is with a
		// SourceRevokeAll for this source, for this, there should be
		// 1) a revoleAll(source, moment) method
		// 2) (possibly) revokeComponent(...) should occasionally (like every
		// 100 relevantMoments) make a revokeAll combined with the re-assertions
		// of the remaining molecules
		Set<NamedNode> result = new HashSet<NamedNode>();
		for (Source source : sources) {
			Set<NamedNode> revocations = new HashSet<NamedNode>();
			SortedSet<RelevantMoment> relevantMoments = whoWhat.get(source);
			RelevantMoment fromElement = new RelevantMoment(moment);
			if (relevantMoments == null) {
				continue;
			}
			SortedSet<RelevantMoment> relevantMomentsInTimeRange = relevantMoments
					.tailSet(fromElement);
			for (RelevantMoment relevantMoment : relevantMomentsInTimeRange) {
				for (SourceComponent assertion : relevantMoment.assertions) {
					if (!assertion.source.equals(source)) {
						continue;
					}
					// check if this source has revoked that compoent after the
					// current relevantMoment
					if (revocations.contains(assertion.moleculeRef)) {
						continue;
					}
					// TODO does the type really belongs in the metastore?
					if (!componentTypeMap.get(assertion.moleculeRef).equals(
							type)) {
						continue;
					}
					result.add(assertion.moleculeRef);
				}
				for (SourceComponent revocation : relevantMoment.revocations) {
					if (!revocation.source.equals(source)) {
						continue;
					}
					// not checking type
					revocations.add(revocation.moleculeRef);
				}
				if (relevantMoment.revokeAllSources.contains(source)) {
					break;
				}
			}
		}
		return result;
	}

	public Iterator<Date> getFollowingChanges(Set<Source> sources, Date moment) {
		SortedSet<RelevantMoment> resultSortedSet = new TreeSet<RelevantMoment>(
				new Comparator<RelevantMoment>() {

					public int compare(RelevantMoment o1, RelevantMoment o2) {
						return o2.compareTo(o1);
					}

				});
		for (Source source : sources) {
			SortedSet<RelevantMoment> moments = whoWhat.get(source);
			if (moments == null) {
				continue;
			}
			RelevantMoment toElement = new RelevantMoment(moment);
			resultSortedSet.addAll(moments.headSet(toElement));
		}
		final Iterator<RelevantMoment> baseIter = resultSortedSet.iterator();
		return new Iterator<Date>() {

			public boolean hasNext() {
				return baseIter.hasNext();
			}

			public Date next() {
				return baseIter.next().moment;
			}

			public void remove() {
				throw new UnsupportedOperationException();
				
			}
			
		};
		
	}

	public Iterator<Date> getPreviousChanges(Set<Source> sources, Date moment) {
		SortedSet<RelevantMoment> resultSortedSet = new TreeSet<RelevantMoment>();
		for (Source source : sources) {
			SortedSet<RelevantMoment> moments = whoWhat.get(source);
			RelevantMoment toElement = new RelevantMoment(moment);
			resultSortedSet.addAll(moments.tailSet(toElement));
		}
		final Iterator<RelevantMoment> baseIter = resultSortedSet.iterator();
		return new Iterator<Date>() {

			public boolean hasNext() {
				return baseIter.hasNext();
			}

			public Date next() {
				return baseIter.next().moment;
			}

			public void remove() {
				throw new UnsupportedOperationException();
				
			}
			
		};

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MetaStore#getSources()
	 */
	public Set<Source> getSources() {
		return whoWhat.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.gvs.storage.MetaStore#revokeComponent(com.hp.hpl.jena.gvs.Source,
	 *      org.wymiwyg.rdf.graphs.NamedNode, java.util.Date)
	 */
	public void revokeComponent(Source source, NamedNode moleculeRef,
			Date moment) {
		SourceComponent sourceComponent = new SourceComponent(source,
				moleculeRef);
		SortedSet<RelevantMoment> existingRelevantMoments = whoWhat.get(source);
		for (RelevantMoment relevantMoment : existingRelevantMoments) {
			if (relevantMoment.assertions.contains(sourceComponent)) {
				break;
			}
			if (relevantMoment.revocations.contains(sourceComponent)) {
				throw new StoreException(
						"Assertion "+moleculeRef+" of source "+source+" not found (already revoked at "+relevantMoment.moment+" which is before "+moment+")");
			}
			if (relevantMoment.revokeAllSources.contains(source)) {
				throw new StoreException(
						"Assertion "+moleculeRef+" of source "+source+" not found (not between "+relevantMoment.moment+" and "+moment+")");
			}
		}
		RelevantMoment relevantMoment = getRelevantMoment(moment);

		relevantMoment.revocations.add(sourceComponent);
		putToValueSet(whatWhenMap, sourceComponent, relevantMoment);
		putToValueSet(whoWhat, source, relevantMoment);

	}

	/**
	 * @param moment
	 * @return
	 */
	private RelevantMoment getRelevantMoment(Date moment) {
		RelevantMoment relevantMoment = relevantMomentsMap.get(moment);
		if (relevantMoment == null) {
			relevantMoment = new RelevantMoment(moment);
			relevantMomentsMap.put(moment, relevantMoment);
			eventDates.add(moment);
		}
		return relevantMoment;
	}

	private <K, V> void putToValueSet(Map<K, SortedSet<V>> map, K key, V value) {
		SortedSet<V> valueSet = map.get(key);
		if (valueSet == null) {
			valueSet = new TreeSet<V>();
			map.put(key, valueSet);
		}
		valueSet.add(value);

	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MetaStore#getAssertions(com.hp.hpl.jena.gvs.Source, java.util.Date, com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Set<NamedNode> getAssertions(Source source, Date moment,
			Resource type) {
		Set<NamedNode> result = new HashSet<NamedNode>();
		for (SourceComponent assertion : relevantMomentsMap.get(moment).assertions) {
			if (assertion.source.equals(source)) {
				result.add(assertion.moleculeRef);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.gvs.storage.MetaStore#getRevocations(com.hp.hpl.jena.gvs.Source, java.util.Date, com.hp.hpl.jena.rdf.model.Resource)
	 */
	public Set<NamedNode> getRevocations(Source source, Date moment,
			Resource type) {
		Set<NamedNode> result = new HashSet<NamedNode>();
		for (SourceComponent revocation : relevantMomentsMap.get(moment).revocations) {
			if (revocation.source.equals(source)) {
				result.add(revocation.moleculeRef);
			}
		}
		return result;
	}

	/**
	 * @param metaModel
	 */
	public void loadFromModel(Model metaModel) {
//		ResIterator	resIterator = metaModel.listSubjectsWithProperty(RDF.type,
//				METAMODEL.Assertion);
		ResIterator	resIterator = metaModel.listSubjectsWithProperty(METAMODEL.asserter);
		while (resIterator.hasNext()) {
			Resource assertion = resIterator.nextResource();
			
			Resource assertedComponent = assertion.getProperty(
					METAMODEL.assertedComponent).getResource();
			Resource type = null;
			StmtIterator typeIter = assertedComponent.listProperties(RDF.type);
			while (typeIter.hasNext()) {
				Resource currentType = typeIter.nextStatement().getResource();
				if (currentType.getNameSpace().equals(METAMODEL.getURI())) {
					type = currentType;
					break;
				}
			}
			Resource assertionSource = assertion.getProperty(
					METAMODEL.asserter).getResource();
			Source source = new SourceImpl(assertionSource.getURI());
			NamedNode component = new NamedNodeImpl(assertedComponent.getURI());
			componentTypeMap.put(component, type);
			SourceComponent sourceComponent = new SourceComponent(source, component);
			{
				//adding assertion
				Literal assertionTimeLit = assertion.getProperty(
						METAMODEL.assertionTime).getLiteral();
				Date assertionTime = ((XSDDateTime) assertionTimeLit.getValue())
				.asCalendar().getTime();
				RelevantMoment relevantMoment = getRelevantMoment(assertionTime);
				relevantMoment.assertions.add(sourceComponent);
				putToValueSet(whatWhenMap, sourceComponent, relevantMoment);
				putToValueSet(whoWhat, source, relevantMoment);
			}
			if (assertion.hasProperty(METAMODEL.revocationTime)) {
				Literal revocationTimeLit = assertion.getProperty(
						METAMODEL.revocationTime).getLiteral();
				Date revocationTime = ((XSDDateTime) revocationTimeLit.getValue())
				.asCalendar().getTime();
				RelevantMoment relevantMoment = getRelevantMoment(revocationTime);
				relevantMoment.revocations.add(sourceComponent);
				putToValueSet(whatWhenMap, sourceComponent, relevantMoment);
				putToValueSet(whoWhat, source, relevantMoment);
			}
		}
		
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

