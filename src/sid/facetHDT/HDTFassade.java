package sid.facetHDT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.atlas.lib.CollectionUtils;
import org.rdfhdt.hdt.dictionary.Dictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.HDTManagerImpl;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleID;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.triples.Triples;

public class HDTFassade {
	
    private HDT hdt;
	
	public HDTFassade(String hdtDump) {
	   this.load(hdtDump); 
	}
	
    private void load(String hdtDump){
        try {
        	// this one requires less memory (map/mapIndexed vs load/loadIndexed)
        	//hdt = HDTManagerImpl.mapIndexedHDT(hdtDump, null);
            hdt = HDTManager.loadIndexedHDT(hdtDump);
        	System.out.println(hdt.getTriples().getNumberOfElements() + " triples in the hdt file "); 

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Searching only properties 
    public Set<String> getPropertiesSearchingHDT (String uri) throws NotFoundException{
    	HashSet<String> result = new HashSet<>();
    	searchAndAddPropertiesHDT(result, uri, TripleComponentRole.SUBJECT);
    	searchAndAddPropertiesHDT(result, uri, TripleComponentRole.OBJECT); 
    	if (result.size() == 0) throw new NotFoundException(); 
    	return result; 
    }
    
    private void searchAndAddPropertiesHDT (Set<String> result, String uri, TripleComponentRole role) {
    	if (role != TripleComponentRole.PREDICATE) { 
			try {
	    		IteratorTripleString it = null; 
	    		it = (role == TripleComponentRole.SUBJECT? hdt.search(uri, "", ""): hdt.search("", "", uri)); 
	    		while (it.hasNext()) {
					result.add(it.next().getPredicate().toString()); 
				}
	    	}
	    	catch (NotFoundException e) {
	    		
	    	}
    	}
    }
    
    public Set<String> getPropertiesSearchingDict (String uri) throws NotFoundException {

    	HashSet<Long> auxResult = new HashSet<>(); 
		Dictionary dict = hdt.getDictionary(); 
		
		long queriedID = dict.stringToId(uri, TripleComponentRole.SUBJECT);
		if (queriedID != -1) {
			if (queriedID <=dict.getNshared()) {
				// it's an URI that appears as subj and obj
				// 0 is a wildcard
				searchAndAddProperties(auxResult, new TripleID(queriedID, 0, 0));  
				searchAndAddProperties(auxResult, new TripleID(0, 0, queriedID));  
			}
			else {
				// it's just a subject 
				searchAndAddProperties(auxResult, new TripleID(queriedID, 0, 0));  
			}
		}
		else {
			queriedID = dict.stringToId(uri, TripleComponentRole.OBJECT); 
			if (queriedID == -1) throw new NotFoundException("URI: "+uri+ " not found");
			searchAndAddProperties(auxResult, new TripleID(0, 0, queriedID));  
		}
    	
    	return translateSetProperties(auxResult); 
    }

    private void searchAndAddProperties (Set<Long> result, TripleID triple) {
    	IteratorTripleID it = hdt.getTriples().search(triple);  
		while (it.hasNext()) {
			result.add(it.next().getPredicate()); 
		}
    }
    
    private Set<String> translateSetProperties(Set<Long> ids) {
    	HashSet<String> result = new HashSet<String>();
    	Dictionary dict = hdt.getDictionary(); 
    	for (long propId: ids) {
    		result.add(dict.idToString(propId, TripleComponentRole.PREDICATE).toString()); 
    	}
    	return result; 
    }
    
    
    // These methods only retrieve the URIs, not the rdfs:label ones 
    // Building outgoing facets for a given URI
    public Map<String, Set<String>> buildOutgoingFacetsSearchingHDT (String uri) throws NotFoundException {
    	Map<String, Set<String>> result = new HashMap<String, Set<String>>(); 
    	IteratorTripleString it = hdt.search(uri, "", "");
    	TripleString aux = null;
    	String auxPred = null;
    	while (it.hasNext()) {
    		aux = it.next(); 
    		auxPred = aux.getPredicate().toString(); 
    		if (!result.containsKey(auxPred)) {
    			result.put(auxPred, new HashSet<String> ());
    		}
    		result.get(auxPred).add(aux.getObject().toString()); 
    	}
    	return result; 
    }
    
    // Building incomingfacets for a given URI
    // This method is expected to be more expensive than outgoingFacets (given the underlying basic structure)
    public Map<String, Set<String>> buildIncomingFacetsSearchingHDT (String uri) throws NotFoundException {
    	Map<String, Set<String>> result = new HashMap<String, Set<String>>(); 
    	IteratorTripleString it = hdt.search("", "", uri);
    	TripleString aux = null;
    	String auxPred = null;
    	while (it.hasNext()) {
    		aux = it.next(); 
    		auxPred = aux.getPredicate().toString(); 
    		if (!result.containsKey(auxPred)) {
    			result.put(auxPred, new HashSet<String> ());
    		}
    		result.get(auxPred).add(aux.getSubject().toString()); 
    	}
    	return result; 
    }

    public Map<String, Set<String>> buildOutgoingFacetsSearchingDict (String uri) throws NotFoundException {
    	Map<Long, Set<Long>> auxResult = new HashMap<Long, Set<Long>>(); 
    	long queriedId = hdt.getDictionary().stringToId(uri, TripleComponentRole.SUBJECT); 
    	if (queriedId == -1) throw new NotFoundException(); 
    	IteratorTripleID it = hdt.getTriples().search(new TripleID(queriedId, 0, 0));
    	TripleID aux = null; 
    	long auxPred = -1; 
    	while (it.hasNext()) {
    		aux = it.next();
    		auxPred = aux.getPredicate(); 
    		if (!auxResult.containsKey(auxPred)) {
    			auxResult.put(auxPred, new HashSet<Long>()); 
    		}
    		auxResult.get(auxPred).add(aux.getObject()); 
    	}
    	return translateFacetIDs(auxResult); 
    }

    public Map<String, Set<String>> buildIncomingFacetsSearchingDict (String uri) throws NotFoundException {
    	Map<Long, Set<Long>> auxResult = new HashMap<Long, Set<Long>>(); 
    	long queriedId = hdt.getDictionary().stringToId(uri, TripleComponentRole.OBJECT); 
    	if (queriedId == -1) throw new NotFoundException(); 
    	IteratorTripleID it = hdt.getTriples().search(new TripleID(0, 0, queriedId));
    	TripleID aux = null; 
    	long auxPred = -1; 
    	while (it.hasNext()) {
    		aux = it.next();
    		auxPred = aux.getPredicate(); 
    		if (!auxResult.containsKey(auxPred)) {
    			auxResult.put(auxPred, new HashSet<Long>()); 
    		}
    		auxResult.get(auxPred).add(aux.getSubject()); 
    	}
    	return translateFacetIDs(auxResult); 
    }
    
    private Map<String, Set<String>> translateFacetIDs (Map<Long, Set<Long>> idTable) {
    	Map<String, Set<String>> result = new HashMap<String, Set<String>>();
    	String auxPred = null; 
    	for (Long idPred: idTable.keySet()) {
    		auxPred = hdt.getDictionary().idToString(idPred, TripleComponentRole.PREDICATE).toString();
    		result.put(auxPred, new HashSet<String>()); 
    		for (Long idObj: idTable.get(idPred)) {
    			result.get(auxPred).add(hdt.getDictionary().idToString(idObj, TripleComponentRole.OBJECT).toString()); 
    		}
    	}
    	return result; 
    }
    
    
    public static final String LABEL_URI = "http://www.w3.org/2000/01/rdf-schema#label"; 

    // These methods only retrieve the URIs, not the rdfs:label ones 
    // Building outgoing facets for a given URI
    public Map<String, Set<List<String>>> buildOutgoingFacetsSearchingLabelsHDT (String uri) throws NotFoundException {
    	Map<String, Set<List<String>>> result = new HashMap<String, Set<List<String>>>(); 
    	IteratorTripleString it = hdt.search(uri, "", "");
    	TripleString aux = null;
    	String auxPred = null;
    	while (it.hasNext()) {
    		aux = it.next(); 
    		auxPred = aux.getPredicate().toString(); 
    		if (!result.containsKey(auxPred)) {
    			result.put(auxPred, new HashSet<List<String>> ());
    		}
    		List<String> auxList = new ArrayList<String>(); 
    		auxList.add(aux.getObject().toString()); 
    		IteratorTripleString labelIt = hdt.search(aux.getObject(), LABEL_URI, ""); 
    		while (labelIt.hasNext()) {
    			auxList.add(labelIt.next().getObject().toString()); 
    		}
    		result.get(auxPred).add(auxList); 
    	}
    	return result; 
    }
    
    // Building incomingfacets for a given URI, retrieving the label as well
    // This method is expected to be more expensive than outgoingFacets (given the underlying basic structure)

    
    public Map<String, Set<List<String>>> buildIncomingFacetsSearchingLabelsHDT (String uri) throws NotFoundException {
    	Map<String, Set<List<String>>> result = new HashMap<String, Set<List<String>>>(); 
    	IteratorTripleString it = hdt.search("", "", uri);
    	TripleString aux = null;
    	String auxPred = null;
    	while (it.hasNext()) {
    		aux = it.next(); 
    		auxPred = aux.getPredicate().toString(); 
    		if (!result.containsKey(auxPred)) {
    			result.put(auxPred, new HashSet<List<String>> ());
    		}
    		List<String> auxList = new ArrayList<String>(); 
    		auxList.add(aux.getSubject().toString()); 
    		IteratorTripleString labelIt = hdt.search(aux.getSubject(), LABEL_URI, ""); 
    		while (labelIt.hasNext()) {
    			auxList.add(labelIt.next().getObject().toString()); 
    		}
    		result.get(auxPred).add(auxList); 
    	}
    	return result; 
    }

    
    private static Object lock = new Object(); 
    public Map<Long, Set<String>> calculateNodeDegrees() {
    	HashMap<Long, Set<String>> results = new HashMap<Long, Set<String>>(); 
		System.out.println("Processing "+(hdt.getDictionary().getNshared()+1)+"..."); 
    	LongStream.range(1, hdt.getDictionary().getNshared()+1)
    				.parallel()
    				.forEach(x -> {
    					long in = calculateInDegreeFromID(x); 
    					long out = calculateOutDegreeFromID(x);
    					long degree = in + out; 
    					String uri = hdt.getDictionary().idToString(x, TripleComponentRole.SUBJECT).toString();
    					
    					synchronized(lock) {
    						if (!results.containsKey(degree)) {
    							results.put(degree, new HashSet<String>()); 
    						}
    						results.get(degree).add(uri); 
    					}
    					
    				}); 
    	System.out.println(hdt.getDictionary().getNsubjects() + " "+hdt.getDictionary().getNshared()+" "+hdt.getDictionary().getNobjects()); 
    	System.out.println( hdt.getDictionary().idToString(hdt.getDictionary().getNsubjects(), TripleComponentRole.SUBJECT)); 
    	System.out.println( hdt.getDictionary().idToString(hdt.getDictionary().getNsubjects(), TripleComponentRole.SUBJECT)); 
    	System.out.println( hdt.getDictionary().idToString(hdt.getDictionary().getNshared()+1, TripleComponentRole.SUBJECT)); 
    	System.out.println( hdt.getDictionary().idToString(hdt.getDictionary().getNshared()+1, TripleComponentRole.OBJECT));
    	
    	LongStream.range(hdt.getDictionary().getNshared()+1, hdt.getDictionary().getNsubjects()+1)
    				.parallel()
    				.forEach(x -> {
    					long out = calculateOutDegreeFromID(x);
    					String uri = hdt.getDictionary().idToString(x, TripleComponentRole.SUBJECT).toString();
    					synchronized(lock) {
    						if (!results.containsKey(out)) {
    							results.put(out, new HashSet<String>()); 
    						}
    						results.get(out).add(uri); 
    					}
    					
    				}); 
    	LongStream.range(hdt.getDictionary().getNshared()+1, hdt.getDictionary().getNobjects()+1)
					.parallel()
					.forEach(x -> {
						long in = calculateInDegreeFromID(x);
						String uri = hdt.getDictionary().idToString(x, TripleComponentRole.OBJECT).toString();
						synchronized(lock) {
							if (!results.containsKey(in)) {
								results.put(in, new HashSet<String>()); 
							}
							results.get(in).add(uri); 
						}
						
		}); 
    	return results; 
    }
    
    public long calculateOutDegreeFromURI (String uri) throws NotFoundException{
    	long outDegree = 0;
    	try {
	    	IteratorTripleString it = hdt.search(uri, "", ""); 
	    	while (it.hasNext()) {
	    		outDegree++; 
	    		it.next(); 
	    	}
    	}
	    catch (NotFoundException e) {
	    	outDegree = 0; 
	    }
    	return outDegree; 
    }
    public long calculateInDegreeFromURI (String uri) throws NotFoundException{
    	long inDegree = 0; 
    	try {
	    	IteratorTripleString it = hdt.search("", "", uri);
	    	while (it.hasNext()) {
	    		inDegree++; 
	    		it.next(); 
	    	}
    	}
    	catch (NotFoundException e) {
    		inDegree = 0; 
    	}
    	return inDegree; 
    }
    public long calculateOutDegreeFromID (long id){
    	long outDegree = 0;
    	IteratorTripleID it = hdt.getTriples().search(new TripleID(id, 0, 0));
    	while (it.hasNext()) {
    		outDegree++; 
    		it.next(); 
    	}
    	return outDegree; 
    }
    public long calculateInDegreeFromID (long id) {
    	long inDegree = 0;
    	IteratorTripleID it = hdt.getTriples().search(new TripleID(0, 0, id));
    	while (it.hasNext()) {
    		inDegree++; 
    		it.next(); 
    	}
    	return inDegree; 
    }
    
    
}
