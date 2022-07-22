package sid.facetHDT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;

import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.IteratorTripleID;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdt.triples.TripleID;

public class DegreesCalculator {
	
	 private HDT hdt;
	
	public DegreesCalculator (HDTFassade hdt) {
		this.hdt = hdt.getHdt(); 
	}
	
	private static Object lock = new Object(); 
    public Map<Long, Set<String>> calculateNodeDegrees() {
    	HashMap<Long, Set<String>> results = new HashMap<Long, Set<String>>(); 
		System.out.println("Processing "+(hdt.getDictionary().getNshared()+1)+"..."); 
    	LongStream.range(1, hdt.getDictionary().getNshared()+1)
    				.parallel()
    				.forEach(x -> {
    					long inDegree = calculateInDegreeFromID(x); 
    					long outDegree = calculateOutDegreeFromID(x);
    					long degree = inDegree + outDegree; 
    					String uri = hdt.getDictionary().idToString(x, TripleComponentRole.SUBJECT).toString();
    					
    					synchronized(lock) {
    						if (!results.containsKey(degree)) {
    							results.put(degree, new HashSet<String>()); 
    						}
    						results.get(degree).add(uri); 
    					}
    					
    				}); 
    	
    	LongStream.range(hdt.getDictionary().getNshared()+1, hdt.getDictionary().getNsubjects()+1)
    				.parallel()
    				.forEach(x -> {
    					long outDegree = calculateOutDegreeFromID(x);
    					String uri = hdt.getDictionary().idToString(x, TripleComponentRole.SUBJECT).toString();
    					synchronized(lock) {
    						if (!results.containsKey(outDegree)) {
    							results.put(outDegree, new HashSet<String>()); 
    						}
    						results.get(outDegree).add(uri); 
    					}
    					
    				}); 
    	LongStream.range(hdt.getDictionary().getNshared()+1, hdt.getDictionary().getNobjects()+1)
					.parallel()
					.forEach(x -> {
						long inDegree = calculateInDegreeFromID(x);
						String uri = hdt.getDictionary().idToString(x, TripleComponentRole.OBJECT).toString();
						synchronized(lock) {
							if (!results.containsKey(inDegree)) {
								results.put(inDegree, new HashSet<String>()); 
							}
							results.get(inDegree).add(uri); 
						}
						
		}); 
    	return results; 
    }
    
    public Map<Long, Set<String>> calculateNodeOutDegrees() {
    	HashMap<Long, Set<String>> results = new HashMap<Long, Set<String>>(); 
		System.out.println("Processing "+(hdt.getDictionary().getNsubjects()+1)+"..."); 
    	LongStream.range(1, hdt.getDictionary().getNsubjects()+1)
    				.parallel()
    				.forEach(x -> {
    					long outDegree = calculateOutDegreeFromID(x);
    					String uri = hdt.getDictionary().idToString(x, TripleComponentRole.SUBJECT).toString();
    					synchronized(lock) {
    						if (!results.containsKey(outDegree)) {
    							results.put(outDegree, new HashSet<String>()); 
    						}
    						results.get(outDegree).add(uri); 
    					}
    					
    				}); 
    	return results; 
    }
    
    public Map<Long, Set<String>> calculateNodeInDegrees() {
    	HashMap<Long, Set<String>> results = new HashMap<Long, Set<String>>(); 
		System.out.println("Processing "+(hdt.getDictionary().getNshared()+1)+"..."); 
    	LongStream.range(1, hdt.getDictionary().getNshared()+1)
    				.parallel()
    				.forEach(x -> {
    					long inDegree = calculateInDegreeFromID(x);  
    					String uri = hdt.getDictionary().idToString(x, TripleComponentRole.SUBJECT).toString();
    					
    					synchronized(lock) {
    						if (!results.containsKey(inDegree)) {
    							results.put(inDegree, new HashSet<String>()); 
    						}
    						results.get(inDegree).add(uri); 
    					}
    					
    				}); 

    	LongStream.range(hdt.getDictionary().getNshared()+1, hdt.getDictionary().getNobjects()+1)
					.parallel()
					.forEach(x -> {
						long inDegree = calculateInDegreeFromID(x);
						String uri = hdt.getDictionary().idToString(x, TripleComponentRole.OBJECT).toString();
						synchronized(lock) {
							if (!results.containsKey(inDegree)) {
								results.put(inDegree, new HashSet<String>()); 
							}
							results.get(inDegree).add(uri); 
						}
						
		}); 
    	return results; 
    }
    
    
    public long calculateOutDegreeFromURI (String uri) {
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
    public long calculateInDegreeFromURI (String uri) {
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
