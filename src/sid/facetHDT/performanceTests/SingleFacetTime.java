package sid.facetHDT.performanceTests;

import sid.facetHDT.HDTFassade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class SingleFacetTime {
	
	public enum Direction { INCOMING, OUTGOING, BOTH }; 
	
	public static final String HDT_FILE_OPTION = "hdtFile"; 
	public static final String URI_OPTION= "uri"; 
	public static final String DIRECTION_OPTION="direction"; 
	public static final String HELP_OPTION = "help"; 
	public static final Double NANO=1000000000.0; 

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(HDT_FILE_OPTION, true, "HDT Filename");
		options.addOption(URI_OPTION, true, "URI to calculate the facets from");
		options.addOption(DIRECTION_OPTION, true, "direction: incoming, outgoing, both"); 
		options.addOption(HELP_OPTION, false, "display this help"); 

		try  {
			CommandLine cmd = parser.parse( options, args);
			boolean helpAsked = cmd.hasOption(HELP_OPTION);
			if(helpAsked) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "SingleFacetTime", options );
				System.exit(0);
			} 
			
			String HDTFilename = cmd.getOptionValue(HDT_FILE_OPTION); 	
			String uri = cmd.getOptionValue(URI_OPTION);
			Direction dir = Direction.valueOf(cmd.getOptionValue(DIRECTION_OPTION).toUpperCase()); 
			
			System.out.println("Loading the HDT ..."); 
			HDTFassade hdt = new HDTFassade(HDTFilename); 
			System.out.println("Done."); 
			
			long start = -1; 
			long end = -1; 
			
			System.out.println("Calculating the degrees of the given URI ... "); 
			long inDegree = -1; 
			long outDegree = -1; 
			switch (dir) {
				case INCOMING: 
					start = System.nanoTime(); 
					inDegree = hdt.calculateInDegreeFromURI(uri); 
					end = System.nanoTime(); 
					System.out.println("InDegree: "+inDegree); 
					break; 
				case OUTGOING: 
					start = System.nanoTime();
					outDegree = hdt.calculateOutDegreeFromURI(uri); 
					end = System.nanoTime(); 
					System.out.println("OutDegree: "+outDegree);
					
					break; 
				case BOTH: 
					start = System.nanoTime();
					inDegree = hdt.calculateInDegreeFromURI(uri); 
					outDegree = hdt.calculateOutDegreeFromURI(uri); 
					end = System.nanoTime(); 
					System.out.println("InDegree: "+inDegree); 
					System.out.println("OutDegree: "+outDegree); 
					break; 
			}
			
			System.out.println("Elapsed Time: "+((end-start)/NANO)+" s."); 
			
			System.out.println("Calculating "+dir+" facets of the given URI ...  ");
			Map<String, Set<String>> inFacetsHDT = null;
			Map<String, Set<String>> inFacetsDict = null; 
			Map<String, Set<String>> outFacetsHDT = null;
			Map<String, Set<String>> outFacetsDict = null; 
			Map<String, Set<List<String>>> inFacetsLabelsHDT = null;
			Map<String, Set<List<String>>> outFacetsLabelsHDT = null;
			switch (dir) {
				case INCOMING: 
					start = System.nanoTime(); 
					inFacetsHDT = hdt.buildIncomingFacetsSearchingHDT(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeHDT: "+((end-start)/NANO)+" s."); 
					start = System.nanoTime(); 
					inFacetsLabelsHDT = hdt.buildIncomingFacetsSearchingLabelsHDT(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeHDT Labels: "+((end-start)/NANO)+" s."); 
					start = System.nanoTime(); 
					inFacetsDict = hdt.buildIncomingFacetsSearchingDict(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeHDT: "+((end-start)/NANO)+" s."); 
					break; 
				case OUTGOING: 
					start = System.nanoTime(); 
					outFacetsHDT = hdt.buildOutgoingFacetsSearchingHDT(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeHDT: "+((end-start)/NANO)+" s."); 
					start = System.nanoTime(); 
					outFacetsLabelsHDT = hdt.buildOutgoingFacetsSearchingLabelsHDT(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeHDT Labels: "+((end-start)/NANO)+" s."); 
					start = System.nanoTime(); 
					outFacetsDict = hdt.buildOutgoingFacetsSearchingDict(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeHDT: "+((end-start)/NANO)+" s."); 
					
					break; 
				case BOTH: 
					start = System.nanoTime(); 
					inFacetsHDT = hdt.buildIncomingFacetsSearchingHDT(uri);
					outFacetsHDT = hdt.buildOutgoingFacetsSearchingHDT(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeHDT: "+((end-start)/NANO)+" s."); 
					start = System.nanoTime(); 
					inFacetsLabelsHDT = hdt.buildIncomingFacetsSearchingLabelsHDT(uri);
					outFacetsLabelsHDT = hdt.buildOutgoingFacetsSearchingLabelsHDT(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeHDT Labels: "+((end-start)/NANO)+" s."); 
					start = System.nanoTime(); 
					inFacetsDict = hdt.buildIncomingFacetsSearchingDict(uri);
					outFacetsDict = hdt.buildOutgoingFacetsSearchingDict(uri); 
					end = System.nanoTime(); 
					System.out.println("Elapsed TimeDict: "+((end-start)/NANO)+" s.");
					break; 
			}
			
			if (inFacetsHDT != null && inFacetsDict != null) {
				System.out.println("In :: NumFacets: "+inFacetsHDT.keySet().size()+" --- "+inFacetsDict.keySet().size());
				long sum = 0;
				long labels = 0; 
				for (String key: inFacetsHDT.keySet() ) {
					sum += inFacetsHDT.get(key).size(); 
				}
				System.out.println("In :: HDT: "+ sum);
				sum = 0; 
				labels = 0; 
				for (String key: inFacetsLabelsHDT.keySet() ) {
					sum += inFacetsLabelsHDT.get(key).size();
					for (List<String> l: inFacetsLabelsHDT.get(key)) {
						labels += l.size(); 
					}
				}
				System.out.println("In :: HDT Labels Values: "+ sum);
				System.out.println("In :: HDT Labels Lists: "+ labels);
				
				sum = 0; 
				for (String key: inFacetsDict.keySet() ) {
					sum += inFacetsDict.get(key).size(); 
				}
				System.out.println("In :: Dict: "+ sum); 
			}
			if (outFacetsHDT != null && outFacetsDict != null) {
				System.out.println("Out :: NumFacets: "+outFacetsHDT.keySet().size()+" --- "+outFacetsDict.keySet().size());
				long sum = 0; 
				long labels = 0; 
				for (String key: outFacetsHDT.keySet() ) {
					sum += outFacetsHDT.get(key).size(); 
				}
				System.out.println("Out :: HDT: "+ sum); 
				sum = 0; 
				labels = 0; 
				for (String key: outFacetsLabelsHDT.keySet() ) {
					sum += outFacetsLabelsHDT.get(key).size();
					for (List<String> l: outFacetsLabelsHDT.get(key)) {
						labels+=outFacetsHDT.get(key).size(); 
					}
				}
				System.out.println("Out :: HDT Labels Values: "+ sum);
				System.out.println("Out :: HDT Labels Lists: "+ labels);
				sum = 0; 
				for (String key: outFacetsDict.keySet() ) {
					sum += outFacetsDict.get(key).size(); 
				}
				System.out.println("Out :: Dict: "+ sum); 
			}

			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); 
		}
	}
	
}
