package sid.facetHDT;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.IteratorTripleString;


public class SchemaAproximator {
	
	private final static int MAX_NUM_CHECKS=10; 
	
	private HDT hdt;
	private Set<String> concepts; 
	private Set<String> datatypeProperties; 
	private Set<String> objectProperties; 
	
	public SchemaAproximator (HDTFassade hdt) {
		this.hdt = hdt.getHdt();
		this.concepts = new HashSet<String>(); 
		this.datatypeProperties = new HashSet<String>(); 
		this.objectProperties = new HashSet<String>(); 
		this.populateConcepts(); 
		this.populateProperties(); 
	}
	
	private void populateConcepts() {
		IteratorTripleString it = null; 
		try {
			it = hdt.search("", Utils.TYPE_URI, ""); 
			while (it.hasNext()) {
				concepts.add(it.next().getObject().toString()); 
			}
		}
		catch (NotFoundException e) {
			System.err.println("No concept assertion found"); 
		}
	}
	
	private void populateProperties() {
		// in this case, we use the fact that HDT has already 
		// separated the URIs that are properties 
		Iterator<?> propIterator = hdt.getDictionary().getPredicates().getSortedEntries(); 
		// we check whether the values of the property are URIs or literals
		String currentProperty;
		int checks;
		int objectPropertyVotes; 
		int datatypePropertyVotes; 
		IteratorTripleString it = null; 
		while (propIterator.hasNext()) {
			currentProperty = propIterator.next().toString(); 
			checks = 0;
			objectPropertyVotes = 0; 
			datatypePropertyVotes = 0; 
			try {
				it = hdt.search("", currentProperty, "");
				while (it.hasNext() && checks < MAX_NUM_CHECKS) {
					checks++; 
					try {
						URI uri = new URI(it.next().getObject().toString());
						// if it can create the URI, we consider it's a well formed resource => objProp
						objectPropertyVotes++; 
					} 
					catch (URISyntaxException e) {
						// we consider that it's something that it's not a URI 
						// TODO: check how HDT deals with blank nodes, but they should be treated with 
						// 		inner good URIs
						datatypePropertyVotes++; 
					}
					if (objectPropertyVotes>datatypePropertyVotes) {
						objectProperties.add(currentProperty); 
					}
					else {
						datatypeProperties.add(currentProperty); 
					}
				}
			}
			catch (NotFoundException e) {
				System.err.println("Ommitting "+currentProperty + " as no assertion is found"); 
			}
		}
	}
	
	public void printSchemaInformation () {
		printSet("Concepts: ", this.concepts);
		printSet("ObjectProperties: ", this.objectProperties); 
		printSet("DatatypeProperties: ", this.datatypeProperties); 
	}
	
	private void printSet(String header, Set<String> set) {
		System.out.println(header); 
		for (String item: set) {
			System.out.println("\t"+item); 
		}
	}
}
