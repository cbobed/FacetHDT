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

public class TopKDegreeTimes {
	public static final String HDT_FILE_OPTION = "hdtFile"; 
	public static final String K_OPTION= "k"; 
	public static final String HELP_OPTION = "help"; 
	public static final Double NANO=1000000000.0; 

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(HDT_FILE_OPTION, true, "HDT Filename");
		options.addOption(K_OPTION, true, "number of top degree resources to measure time"); 
		options.addOption(HELP_OPTION, false, "display this help"); 

		try  {
			CommandLine cmd = parser.parse( options, args);
			boolean helpAsked = cmd.hasOption(HELP_OPTION);
			if(helpAsked) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "CompressionRatioCalculator", options );
				System.exit(0);
			} 
			
			String HDTFilename = cmd.getOptionValue(HDT_FILE_OPTION); 	
			Integer k = Integer.valueOf(cmd.getOptionValue(K_OPTION));
			
			System.out.println("Loading the HDT ..."); 
			HDTFassade hdt = new HDTFassade(HDTFilename); 
			System.out.println("Done."); 
			
			System.out.println("Analyzing the degrees of the graph ... "); 
			long start = System.nanoTime(); 
			Map<Long, Set<String>> degrees = hdt.calculateNodeDegrees(); 
			long end = System.nanoTime(); 
			System.out.println("Elapsed time: "+((end-start)/NANO)+"s");
			
			
			System.out.println("Calculating the facets of the k highest degree nodes (both incoming and outgoing)... "); 
			long globalTime = 0;
			long globalTimeID = 0; 
			List<Long> listDegrees = new ArrayList<Long>(degrees.keySet()); 
			Collections.sort(listDegrees);
			long processed = 0 ; 
			int currentDegreeIdx = listDegrees.size()-1;
			int currentIdx = 0; 
			while (processed != k && currentDegreeIdx>=1) {
				List<String> auxList = new ArrayList<> (degrees.get(listDegrees.get(currentDegreeIdx)));
				currentIdx = 0; 
				while (processed != k && currentIdx <auxList.size()) {
					System.out.println("Processing "+auxList.get(currentIdx) + " ... degree: "+listDegrees.get(currentDegreeIdx)); 
					start = System.nanoTime(); 
					hdt.buildOutgoingFacetsSearchingHDT(auxList.get(currentIdx)); 
					hdt.buildIncomingFacetsSearchingHDT(auxList.get(currentIdx)); 
					end = System.nanoTime();
					System.out.println("Elapsed time HDT: "+((end-start)/NANO)+"s");
					globalTime += (end-start); 
					start = System.nanoTime();  
					hdt.buildOutgoingFacetsSearchingDict(auxList.get(currentIdx)); 
					hdt.buildIncomingFacetsSearchingDict(auxList.get(currentIdx)); 
					end = System.nanoTime();
					System.out.println("Elapsed time Dict: "+((end-start)/NANO)+"s");
					globalTimeID += (end-start);
					
					currentIdx++; 
					processed++; 
				}
				currentDegreeIdx--; 
			}
			System.out.println("Avg Time (Only URI retrieval, no labels) HDT: "+(globalTime/(double)k)); 
			System.out.println("Avg Time (Only URI retrieval, no labels) Dict: "+(globalTimeID/(double)k)); 
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); 
		}
	}
	
}
