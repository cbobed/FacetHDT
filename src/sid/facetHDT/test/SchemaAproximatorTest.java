package sid.facetHDT.test;

import sid.facetHDT.HDTFassade;
import sid.facetHDT.SchemaAproximator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class SchemaAproximatorTest {
	
	public static final String HDT_FILE_OPTION = "hdtFile"; 
	public static final String HELP_OPTION = "help"; 
	public static final Double NANO=1000000000.0; 

	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(HDT_FILE_OPTION, true, "HDT Filename");
		options.addOption(HELP_OPTION, false, "display this help"); 

		try  {
			CommandLine cmd = parser.parse( options, args);
			boolean helpAsked = cmd.hasOption(HELP_OPTION);
			if(helpAsked) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "SchemaAproximatorTest", options );
				System.exit(0);
			} 
			
			String HDTFilename = cmd.getOptionValue(HDT_FILE_OPTION); 	
			
			System.out.println("Loading the HDT ..."); 
			HDTFassade hdt = new HDTFassade(HDTFilename); 
			System.out.println("Done."); 
			
			long start = -1; 
			long end = -1; 
			
			System.out.println("Calculating the schema"); 
			start = System.nanoTime();
			SchemaAproximator sAprox = new SchemaAproximator(hdt); 
			end = System.nanoTime(); 
			System.out.println("Elapsed Time: "+((end-start)/NANO)+" s."); 
			
			sAprox.printSchemaInformation();			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); 
		}
	}
}
