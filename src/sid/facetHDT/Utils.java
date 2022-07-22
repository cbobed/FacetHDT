package sid.facetHDT;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utils {
	
    public static final String LABEL_URI = "http://www.w3.org/2000/01/rdf-schema#label"; 
	public static final String TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"; 
	
	public static Set<String> URIStoAvoid = new HashSet<String>(Arrays.asList(TYPE_URI)); 
}
