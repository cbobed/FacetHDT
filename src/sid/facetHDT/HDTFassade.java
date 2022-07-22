package sid.facetHDT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


public class HDTFassade {
	
	private HDT hdt;
	
	public HDTFassade(String hdtDump) {
	   this.load(hdtDump); 
	}
	
	public HDTFassade(String hdtDump, boolean saveMemory) {
	   this.load(hdtDump, saveMemory); 
	}
	
    private void load(String hdtDump){
    	load(hdtDump, false); 
    }
	
    private void load(String hdtDump, boolean saveMemory){
        try {

            if (saveMemory) {
            	// this one requires less memory (map/mapIndexed vs load/loadIndexed)
            	hdt = HDTManagerImpl.mapIndexedHDT(hdtDump, null); 
            }
            else {
                hdt = HDTManager.loadIndexedHDT(hdtDump);
            }
            
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
    
    public Set<String> getIncomingPropertiesSearchingHDT (String uri) throws NotFoundException{
    	HashSet<String> result = new HashSet<>();
    	searchAndAddPropertiesHDT(result, uri, TripleComponentRole.OBJECT); 
    	if (result.size() == 0) throw new NotFoundException(); 
    	return result; 
    }

    public Set<String> getOutgoingPropertiesSearchingHDT (String uri) throws NotFoundException{
    	HashSet<String> result = new HashSet<>();
    	searchAndAddPropertiesHDT(result, uri, TripleComponentRole.SUBJECT); 
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
    public Map<String, Set<String>> buildOutgoingNavigationStepsSearchingHDT (String uri) {
    	Map<String, Set<String>> result = new HashMap<String, Set<String>>(); 
    	try {
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
    	}
    	catch (NotFoundException e) {
    		System.err.println(uri + " not found as subject"); 
    	}
    	return result; 
    }
    
    // Building incomingfacets for a given URI
    // This method is expected to be more expensive than outgoingFacets (given the underlying basic structure)
    public Map<String, Set<String>> buildIncomingNavigationStepsSearchingHDT (String uri) {
    	Map<String, Set<String>> result = new HashMap<String, Set<String>>(); 
    	try {
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
    	}
    	catch (NotFoundException e) {
    		System.err.println(uri +" not found as object"); 
    	}
    	return result; 
    }
 


    // These methods only retrieve the URIs, not the rdfs:label ones 
    // Building outgoing facets for a given URI
    public Map<String, Set<List<String>>> buildOutgoingNavigationStepsSearchingLabelsHDT (String uri) {
 
    	Map<String, Set<List<String>>> result = null;  
		Map<String, Set<String>> intermediateResults = buildOutgoingNavigationStepsSearchingHDT(uri); 
		result = obtainAllLabelsAsList(intermediateResults); 
	
    	return result; 
    }
    
  
    // Building incomingfacets for a given URI, retrieving the label as well
    // This method is expected to be more expensive than outgoingFacets (given the underlying basic structure)

    public Map<String, Set<List<String>>> buildIncomingNavigationStepsSearchingLabelsHDT (String uri) {
    	Map<String, Set<List<String>>> result = new HashMap<String, Set<List<String>>>(); 
    	Map<String, Set<String>> intermediateResults = buildIncomingNavigationStepsSearchingHDT(uri); 
    	result = obtainAllLabelsAsList(intermediateResults); 
    	return result; 
    }
  
    // Methods for retrieving the properties to populate the facets of the supernodes 
    // given a set of uris (ideally they will be the ones in a facet) 
    // This first version is without labels 
    public Map<String, Set<String>> buildSearchingFacetsOutgoingSearchingHDT (Set<String> uris) {
    	// first we are going to work with uris and literals to avoid duplicates in the sets
    	// afterwards, we translate the labels appropriately
    	Map<String, Set<String>> intermediateResults = new HashMap<String, Set<String>>();
    	Map<String, Set<String>> auxCurrentResults = null; 
    	// this could be innerly parallelized easily using ConcurrentHashMap + parallelStream 
    	for (String currentURI: uris) {
    		auxCurrentResults = buildIncomingNavigationStepsSearchingHDT(currentURI); 
    		mergeResults(intermediateResults, auxCurrentResults); 
    	}
    	return intermediateResults; 
    }
    
    public Map<String, Set<String>> buildSearchingFacetsOutgoingSearchingLabelsHDT (Set<String> uris) {
    	Map<String, Set<String>> intermediateResults = buildSearchingFacetsOutgoingSearchingHDT(uris);
    	Map<String, Set<String>> result = obtainAllLabelsForFacets(intermediateResults); 
    	return result; 
    	 
    }
    
    private void mergeResults (Map<String, Set<String>> mainMap, Map<String, Set<String>> addedMap) {
    	for (String key: addedMap.keySet()) {
    		if (!mainMap.containsKey(key)) {
    			mainMap.put(key, new HashSet<String>()); 
    		}
    		mainMap.get(key).addAll(addedMap.get(key)); 
    	}
    }
    
    
    private Map<String, Set<List<String>>> obtainAllLabelsAsList (Map<String, Set<String>> rawUris) {
	  
    	Map<String, Set<List<String>>> result = new HashMap<String, Set<List<String>>>(); 
    	for (String propertyKey: rawUris.keySet()) {
    		result.put(propertyKey, new HashSet<List<String>>()); 
    		for (String valueToTranslate: rawUris.get(propertyKey)) {
    			List<String> auxList = new ArrayList<String>(); 
    			auxList.add(valueToTranslate);
    			try {
    				//URI u = new URI(valueToTranslate); // to check that the object it is a URI
    	    		IteratorTripleString labelIt = hdt.search(valueToTranslate, Utils.LABEL_URI, ""); 
    	    		while (labelIt.hasNext()) {
    	    			auxList.add(labelIt.next().getObject().toString()); 
    	    		}
        		}
        		catch (NotFoundException e) {
        			System.err.println(valueToTranslate + " :: Label not found"); 
        		}
    			result.get(propertyKey).add(auxList); 
    		}
    	}
    	return result; 
 
    }
    
    private Map<String, Set<String>> obtainAllLabelsForFacets (Map<String, Set<String>> rawUris) {
    	
    	Map<String, Set<String>> result = new HashMap<String, Set<String>>(); 
    	for (String propertyKey: rawUris.keySet()) {
    		result.put(propertyKey, new HashSet<String>()); 
    		for (String valueToTranslate: rawUris.get(propertyKey)) {
    			// This is a test, so we add all the values 
    			// here we should discriminate whether propertyKey is an ObjectProperty or a DatatypeProperty (attribute) 
    			// to check whether it makes sense or not to try to translate the values 
    			result.get(propertyKey).add(valueToTranslate); 
    			try {
    	    		IteratorTripleString labelIt = hdt.search(valueToTranslate, Utils.LABEL_URI, "");
    	    		while (labelIt.hasNext()) {
    	    			result.get(propertyKey).add(labelIt.next().getObject().toString()); 
    	    		}
        		}
        		catch (NotFoundException e) {
        			System.err.println(valueToTranslate + " :: Label not found"); 
        		} 
    		}
    	}
    	return result; 
 
    }
    
    // getter to build other classes on top of the same HDT
    public HDT getHdt() {
		return hdt;
	}

    
    @Deprecated
    public Map<String, Set<String>> buildOutgoingNavigationStepsSearchingDict (String uri) {
    	Map<Long, Set<Long>> auxResult = new HashMap<Long, Set<Long>>(); 
    	long queriedId = hdt.getDictionary().stringToId(uri, TripleComponentRole.SUBJECT); 
    	if (queriedId >= 1) {
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
    	}
	    	return translateFacetIDs(auxResult); 
    }
    @Deprecated
    public Map<String, Set<String>> buildIncomingNavigationStepsSearchingDict (String uri) {
    	Map<Long, Set<Long>> auxResult = new HashMap<Long, Set<Long>>(); 
    	long queriedId = hdt.getDictionary().stringToId(uri, TripleComponentRole.OBJECT); 
    	if (queriedId >= 1) {
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
    	}
    	return translateFacetIDs(auxResult); 
    }
    @Deprecated
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
    
    @Deprecated
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

    
}
