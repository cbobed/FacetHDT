package sid.facetHDT.performanceTests;

import sid.facetHDT.HDTFassade;
import sid.facetHDT.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class SingleResourceFacetTime {
	
	public enum Direction { INCOMING, OUTGOING, BOTH }; 
	
	public static final String HDT_FILE_OPTION = "hdtFile"; 
	public static final String URI_OPTION= "uri"; 
	public static final String URIS_TO_AVOID_FILE_OPTION="uriAvoidFile"; 
	public static final String VERBOSE_OPTION="v";
	public static final String HELP_OPTION = "help"; 
	public static final Double NANO=1000000000.0; 

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(HDT_FILE_OPTION, true, "HDT Filename");
		options.addOption(URI_OPTION, true, "URI to calculate the facets from");
		options.addOption(URIS_TO_AVOID_FILE_OPTION, true, "filename with URIs of properties to be avoided");
		options.addOption(VERBOSE_OPTION, false, "display all the retrieved information"); 
		options.addOption(HELP_OPTION, false, "display this help"); 

		try  {
			CommandLine cmd = parser.parse(options, args);
			boolean helpAsked = cmd.hasOption(HELP_OPTION);
			if(helpAsked) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("SingleResourceFacetTime", options );
				System.exit(0);
			} 
			
			boolean verboseMode = cmd.hasOption(VERBOSE_OPTION); 
			String HDTFilename = cmd.getOptionValue(HDT_FILE_OPTION);
			if (cmd.hasOption(URIS_TO_AVOID_FILE_OPTION)) {
				addUrisToAvoid(Utils.URIStoAvoid, cmd.getOptionValue(URIS_TO_AVOID_FILE_OPTION)); 
			}
			String uri = cmd.getOptionValue(URI_OPTION);
			
			System.out.println("Loading the HDT ..."); 
			HDTFassade hdt = new HDTFassade(HDTFilename); 
			System.out.println("Done."); 
			
			long start = -1; 
			long end = -1; 
			long aux = -1; 
			
			System.out.println("Getting outgoing navigation steps ..."); 
			start = System.nanoTime(); 
			Map<String, Set<String>> navigationSteps = hdt.buildOutgoingNavigationStepsSearchingHDT(uri); 
			end = System.nanoTime(); 
			System.out.println("Elapsed Time: "+((end-start)/NANO)+" s."); 
			
			long globalTime = 0; 
			long globalTimeLabel = 0; 
			
			Set<String> filteredProps = new HashSet<String> (navigationSteps.keySet());
			System.out.println("#"+filteredProps.size()+" props obtained..."); 
			filteredProps.removeAll(Utils.URIStoAvoid);
			System.out.println("Calculating #"+filteredProps.size()+ "..."); 
			for (String prop: filteredProps) {
				System.out.println("---------------"); 
				System.out.println("property: "+prop+" number of URIS: "+navigationSteps.get(prop).size()); 
				start = System.nanoTime(); 
				Map<String, Set<String>> facets = hdt.buildSearchingFacetsOutgoingSearchingHDT(navigationSteps.get(prop));
				end = System.nanoTime(); 
				aux = end-start; 
				globalTime += aux; 
				System.out.println("Elapsed Time: "+(aux/NANO)+" s.");
				System.out.println("#Properties: "+facets.keySet().size()); 
				System.out.println("#Values: "+facets.keySet().stream()
															.map(x->facets.get(x).size())
															.reduce(0, (x,y) -> x+y)); 
				if (verboseMode) {
					printFacetInfo(facets, 10);
				}
				
				start = System.nanoTime(); 
				Map<String, Set<String>> facetsLabels = hdt.buildSearchingFacetsOutgoingSearchingLabelsHDT(navigationSteps.get(prop));
				end = System.nanoTime(); 
				aux = end-start; 
				globalTimeLabel += aux; 
				System.out.println("Elapsed Time with Labels : "+(aux/NANO)+" s.");
				System.out.println("#Properties: "+facetsLabels.keySet().size()); 
				System.out.println("#Values: "+facetsLabels.keySet().stream()
															.map(x->facetsLabels.get(x).size())
															.reduce(0, (x,y) -> x+y)); 
				if (verboseMode) {
					printFacetInfo(facets, 10);
				}
			}
			System.out.println("------"); 
			System.out.println("Global Elapsed Time: "+(globalTime/NANO)+" s.");
			System.out.println("Global Elapsed Labels Time: "+(globalTimeLabel/NANO)+" s.");
			
			long incomingGlobalTime = 0; 
			long incomingGlobalTimeLabel = 0; 
			
			System.out.println("Getting incoming navigation steps ..."); 
			start = System.nanoTime(); 
			navigationSteps = hdt.buildIncomingNavigationStepsSearchingHDT(uri); 
			end = System.nanoTime(); 
			System.out.println("Elapsed Time: "+((end-start)/NANO)+" s."); 
			
			filteredProps = new HashSet<String> (navigationSteps.keySet());
			System.out.println("#"+filteredProps.size()+" props obtained..."); 
			filteredProps.removeAll(Utils.URIStoAvoid);
			System.out.println("Calculating #"+filteredProps.size()+ "..."); 
			for (String prop: filteredProps) {
				System.out.println("---------------"); 
				System.out.println("property: "+prop+" number of URIS: "+navigationSteps.get(prop).size()); 
				start = System.nanoTime(); 
				Map<String, Set<String>> facets = hdt.buildSearchingFacetsOutgoingSearchingHDT(navigationSteps.get(prop));
				end = System.nanoTime(); 
				aux = end-start; 
				incomingGlobalTime += aux; 
				System.out.println("Elapsed Time: "+(aux/NANO)+" s.");
				System.out.println("#Properties: "+facets.keySet().size()); 
				System.out.println("#Values: "+facets.keySet().stream()
															.map(x->facets.get(x).size())
															.reduce(0, (x,y) -> x+y)); 
				if (verboseMode) {
					printFacetInfo(facets, 10);
				}
				
				start = System.nanoTime(); 
				Map<String, Set<String>> facetsLabels = hdt.buildSearchingFacetsOutgoingSearchingLabelsHDT(navigationSteps.get(prop));
				end = System.nanoTime(); 
				aux = end-start; 
				incomingGlobalTimeLabel += aux; 
				System.out.println("Elapsed Time with Labels : "+(aux/NANO)+" s.");
				System.out.println("#Properties: "+facetsLabels.keySet().size()); 
				System.out.println("#Values: "+facetsLabels.keySet().stream()
															.map(x->facetsLabels.get(x).size())
															.reduce(0, (x,y) -> x+y)); 
				if (verboseMode) {
					printFacetInfo(facets, 10);
				}
			}
			System.out.println("------"); 
			System.out.println("Global Elapsed Time: "+(incomingGlobalTime/NANO)+" s.");
			System.out.println("Global Elapsed Labels Time: "+(incomingGlobalTimeLabel/NANO)+" s.");
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); 
		}
	}
	
	private static void printFacetInfo (Map<String, Set<String>> fctInfo, int maxValuesPerProperty) {
		int cnt; 
		for (String key: fctInfo.keySet() ) {
			cnt = 1; 
			System.out.println("--> " +key); 
			for (String l: fctInfo.get(key)) {
				cnt++; 
				System.out.println("\t\t"+l);
				if (cnt == maxValuesPerProperty) {
					break; 
				}
			}
		}
	}
	
	private static void addUrisToAvoid(Set<String> uris, String filename) {
		try (BufferedReader input = new BufferedReader(new FileReader(new File (filename)))){
			String line = null; 
			while ((line = input.readLine()) != null) {
				uris.add(line); 
			}
		}
		catch (IOException e) {
			System.err.println("Problems loading the urisToAvoid file");
		}
	}
}
