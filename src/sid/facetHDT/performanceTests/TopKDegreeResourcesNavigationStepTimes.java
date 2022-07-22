package sid.facetHDT.performanceTests;

import sid.facetHDT.HDTFassade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class TopKDegreeResourcesNavigationStepTimes {
	
	public enum Direction { INCOMING, OUTGOING, BOTH }; 

	public static final String HDT_FILE_OPTION = "hdtFile"; 
	public static final String INPUT_FILE_OPTION = "inputFile"; 
	public static final String K_OPTION= "k"; 
	public static final String HELP_OPTION = "help"; 
	public static final String SAVE_MEMORY_OPTION = "saveMemory"; 
	public static final String DIRECTION_OPTION="direction"; 
	public static final Double NANO=1000000000.0; 

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(HDT_FILE_OPTION, true, "HDT Filename");
		options.addOption(K_OPTION, true, "number of top degree resources to measure time"); 
		options.addOption(INPUT_FILE_OPTION, true, "file with the degree - URI tuples to be measured"); 
		options.addOption(SAVE_MEMORY_OPTION, false, "use memory saving loading"); 
		options.addOption(DIRECTION_OPTION, true, "direction: incoming, outgoing, both");
		options.addOption(HELP_OPTION, false, "display this help"); 

		try  {
			CommandLine cmd = parser.parse( options, args);
			boolean helpAsked = cmd.hasOption(HELP_OPTION);
			boolean saveMemory = cmd.hasOption(SAVE_MEMORY_OPTION); 
			if(helpAsked) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "TopKDegreeResourcesNavigationStepTimes", options );
				System.exit(0);
			} 
			
			long maxMemory = -1; 
			String HDTFilename = cmd.getOptionValue(HDT_FILE_OPTION); 	
			Integer k = Integer.valueOf(cmd.getOptionValue(K_OPTION));
			String inputFilename = cmd.getOptionValue(INPUT_FILE_OPTION); 
			Direction dir = Direction.valueOf(cmd.getOptionValue(DIRECTION_OPTION).toUpperCase()); 
			
			MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
			MemoryUsage beforeHeapMemoryUsage = mbean.getHeapMemoryUsage();
			
			System.out.println("Loading the HDT ..."); 
			HDTFassade hdt = new HDTFassade(HDTFilename, saveMemory); 
			System.out.println("Done."); 
			
			MemoryUsage afterHeapMemoryUsage = mbean.getHeapMemoryUsage();
			if (mbean.getHeapMemoryUsage().getUsed() > maxMemory) 
				maxMemory = mbean.getHeapMemoryUsage().getUsed(); 
			long consumed = afterHeapMemoryUsage.getUsed() - 
			                beforeHeapMemoryUsage.getUsed();
			System.out.println("Total consumed Memory loading:" + bytesToMegas(consumed)+ " MB");
			
			long start = -1;  
			long end = -1;  
			
			System.out.println("Calculating the facets of the k highest degree nodes (both incoming and outgoing)... "); 
			long globalTime = 0;
			long globalTimeLabels = 0; 
			long globalTimeID = 0; 	
			
			long accumMem = 0;
			long accumMemLabels = 0; 
			long accumMemID = 0; 
			
			BufferedReader input = new BufferedReader(new FileReader(new File(inputFilename)));
				
			int processed = 0; 
			double accumDegree = 0; 
			String currentLine = "";
			StringTokenizer strTokenizer = null;
			String currentURI = null; 
			String currentDegree = null; 
			 
			while (processed != k && ((currentLine = input.readLine()) != null)) {
				strTokenizer = new StringTokenizer(currentLine); 
				currentDegree = strTokenizer.nextToken();
				currentURI = strTokenizer.nextToken(); 
				accumDegree += Double.valueOf(currentDegree); 
				System.out.println("Processing "+currentURI + " ... degree: "+currentDegree);
				System.gc();
				beforeHeapMemoryUsage = mbean.getHeapMemoryUsage(); 
				start = System.nanoTime(); 
				switch (dir) {
					case OUTGOING: 
						hdt.buildOutgoingNavigationStepsSearchingHDT(currentURI);
						break; 
					case INCOMING: 
						hdt.buildIncomingNavigationStepsSearchingHDT(currentURI);
						break; 
					case BOTH: 
						hdt.buildOutgoingNavigationStepsSearchingHDT(currentURI); 
						hdt.buildIncomingNavigationStepsSearchingHDT(currentURI); 
						break; 
				}
				end = System.nanoTime();
				afterHeapMemoryUsage = mbean.getHeapMemoryUsage();
				if (mbean.getHeapMemoryUsage().getUsed() > maxMemory) 
					maxMemory = mbean.getHeapMemoryUsage().getUsed(); 
				System.out.println("Elapsed time HDT: "+ nanoToSeconds(end-start)+" s.");
				System.out.println("Consumed delta memo: "+(bytesToMegas(afterHeapMemoryUsage.getUsed() - beforeHeapMemoryUsage.getUsed()))+ " MB"); 
				globalTime += (end-start); 
				accumMem += afterHeapMemoryUsage.getUsed() - beforeHeapMemoryUsage.getUsed(); 
			
				System.out.println("Processing "+currentURI + " ... degree: "+currentDegree);
				System.gc();
				beforeHeapMemoryUsage = mbean.getHeapMemoryUsage(); 
				start = System.nanoTime(); 
				switch (dir) {
					case OUTGOING: 
						hdt.buildOutgoingNavigationStepsSearchingLabelsHDT(currentURI);
						break; 
					case INCOMING: 
						hdt.buildIncomingNavigationStepsSearchingLabelsHDT(currentURI);
						break; 
					case BOTH: 
						hdt.buildOutgoingNavigationStepsSearchingLabelsHDT(currentURI); 
						hdt.buildIncomingNavigationStepsSearchingLabelsHDT(currentURI); 
						break; 
				}
				end = System.nanoTime();
				afterHeapMemoryUsage = mbean.getHeapMemoryUsage();
				if (mbean.getHeapMemoryUsage().getUsed() > maxMemory) 
					maxMemory = mbean.getHeapMemoryUsage().getUsed(); 
				System.out.println("Elapsed time HDT withLabels: "+nanoToSeconds(end-start)+" s.");
				System.out.println("Consumed delta memo HDT withLabels: "+bytesToMegas(afterHeapMemoryUsage.getUsed() - beforeHeapMemoryUsage.getUsed()) + " MB"); 
				globalTimeLabels += (end-start); 
				accumMemLabels += afterHeapMemoryUsage.getUsed() - beforeHeapMemoryUsage.getUsed(); 
				
				System.gc();
				beforeHeapMemoryUsage = mbean.getHeapMemoryUsage(); 
				start = System.nanoTime();  
				switch (dir) {
					case OUTGOING: 
						hdt.buildOutgoingNavigationStepsSearchingDict(currentURI);
						break; 
					case INCOMING: 
						hdt.buildIncomingNavigationStepsSearchingDict(currentURI);
						break; 
					case BOTH: 
						hdt.buildOutgoingNavigationStepsSearchingDict(currentURI); 
						hdt.buildIncomingNavigationStepsSearchingDict(currentURI); 
						break; 
				}
				end = System.nanoTime();
				afterHeapMemoryUsage = mbean.getHeapMemoryUsage();
				if (mbean.getHeapMemoryUsage().getUsed() > maxMemory) 
					maxMemory = mbean.getHeapMemoryUsage().getUsed(); 
				System.out.println("Elapsed time Dict: "+nanoToSeconds(end-start)+" s.");
				System.out.println("Consumed delta memo Dict: "+bytesToMegas(afterHeapMemoryUsage.getUsed() - beforeHeapMemoryUsage.getUsed())+ " MB"); 
				globalTimeID += (end-start);
				accumMemID += afterHeapMemoryUsage.getUsed() - beforeHeapMemoryUsage.getUsed(); 

				processed++; 
			}
			System.out.println("---------- Summary ----------"); 
			System.out.println("Saving Memory: "+saveMemory); 
			System.out.println("\tMemory inprint loading: "+bytesToMegas(consumed));
			System.out.println("DIRECTION: "+dir);  
			System.out.println("Processed "+processed+" resources, with an avg degree of "+(accumDegree/(double) processed)); 
			System.out.println("Times: "); 
			System.out.println("\tAvg Time (Only URI retrieval, no labels) HDT: "+nanoToSeconds(globalTime/(double)processed)+" s.");
			System.out.println("\tAvg Time (with Labels) HDT: "+nanoToSeconds(globalTimeLabels/(double)processed)+" s."); 
			System.out.println("\tAvg Time (Only URI retrieval, no labels) Dict: "+nanoToSeconds(globalTimeID/(double)processed)+" s.");
			System.out.println("Memory: "); 
			System.out.println("\tAvg Delta Mem (Only URI retrieval, no labels) HDT: "+bytesToMegas(accumMem/(double)processed)+" MB");
			System.out.println("\tAvg Delta Mem (with Labels) HDT: "+bytesToMegas(accumMemLabels/(double)processed)+" MB"); 
			System.out.println("\tAvg Delta Mem (Only URI retrieval, no labels) Dict: "+bytesToMegas(accumMemID/(double)processed)+" MB");
			System.out.println("\tAvg Max Memory during all tests: "+bytesToMegas(maxMemory)+" MB"); 
			
			input.close(); 
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); 
		}
	}
	
	private static double bytesToMegas (double bytes) {
		return bytes/(1024.0*1024.0); 
	}
	private static double nanoToSeconds (double nanoseconds) {
		return nanoseconds/NANO; 
	}
	
}