package sid.facetHDT.performanceTests;

import sid.facetHDT.HDTFassade;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
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

public class TopKDegreeExtractor {
	public enum Direction { INCOMING, OUTGOING, BOTH }; 
	
	public static final String HDT_FILE_OPTION = "hdtFile"; 
	public static final String K_OPTION= "k"; 
	public static final String OUTPUT_FILE_OPTION="outputFile"; 
	public static final String DIRECTION_OPTION = "direction"; 
	public static final String HELP_OPTION = "help"; 
	public static final Double NANO=1000000000.0; 

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(HDT_FILE_OPTION, true, "HDT Filename");
		options.addOption(K_OPTION, true, "number of top degree resources to extract"); 
		options.addOption(OUTPUT_FILE_OPTION, true, "file to store the highest degree resources"); 
		options.addOption(DIRECTION_OPTION, true, "direction of the degree"); 
		options.addOption(HELP_OPTION, false, "display this help"); 

		try  {
			CommandLine cmd = parser.parse( options, args);
			boolean helpAsked = cmd.hasOption(HELP_OPTION);
			if(helpAsked) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "TopKDegreeExtractor", options );
				System.exit(0);
			} 
			
			String HDTFilename = cmd.getOptionValue(HDT_FILE_OPTION); 	
			Integer k = Integer.valueOf(cmd.getOptionValue(K_OPTION));
			String outputFilename = cmd.getOptionValue(OUTPUT_FILE_OPTION); 
			Direction dir = Direction.valueOf(cmd.getOptionValue(DIRECTION_OPTION).toUpperCase()); 
			
			System.out.println("Loading the HDT ..."); 
			HDTFassade hdt = new HDTFassade(HDTFilename); 
			System.out.println("Done."); 
			
			System.out.println("Analyzing the degrees of the graph ... "); 
			long start = System.nanoTime(); 
			Map<Long, Set<String>> degrees = null; 
			switch (dir) { 
				case BOTH: 
					degrees = hdt.calculateNodeDegrees();
					break; 
				case OUTGOING: 
					degrees = hdt.calculateNodeOutDegrees(); 
					break; 
				case INCOMING: 
					degrees = hdt.calculateNodeInDegrees(); 
					break; 
			}
			long end = System.nanoTime(); 
			System.out.println("Elapsed time: "+((end-start)/NANO)+"s");
			
			List<Long> listDegrees = new ArrayList<Long>(degrees.keySet()); 
			Collections.sort(listDegrees);
			
			PrintWriter out = new PrintWriter(new File(outputFilename)); 
			
			long processed = 0 ; 
			int currentDegreeIdx = listDegrees.size()-1;
			int currentIdx = 0; 
			while (processed != k && currentDegreeIdx>=1) {
				List<String> auxList = new ArrayList<> (degrees.get(listDegrees.get(currentDegreeIdx)));
				currentIdx = 0; 
				while (processed != k && currentIdx <auxList.size()) {
					
					URI auxURI = null; 
					try {
						auxURI = new URI (auxList.get(currentIdx)); 
					}
					catch (URISyntaxException e) {
						auxURI = null; 
					}
					if (auxURI != null && auxList.get(currentIdx).contains("/resource/") ) {
						out.println(listDegrees.get(currentDegreeIdx) + " " + auxList.get(currentIdx)); 
						processed++; 
					}	
					currentIdx++; 
				}
				currentDegreeIdx--; 
			} 
			out.flush(); 
			out.close(); 
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); 
		}
	}
	
}

