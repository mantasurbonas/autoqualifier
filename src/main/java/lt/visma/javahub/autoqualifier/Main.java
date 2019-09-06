package lt.visma.javahub.autoqualifier;

import java.io.File;
import java.util.Arrays;

public class Main {

	private static final String SOURCEROOT_KEY = "-sourceroot";
	private static final String MODE_KEY = "-mode";

	public static void main(String []s) throws Exception {
		if (s.length != 4) {
			printUsage(s);
			return;
		}
		
		String modeKey = s[0];
		String mode = s[1];
		String sourceKey = s[2];
		String sourcePath = s[3];

		if (!modeKey.equalsIgnoreCase(MODE_KEY) || !sourceKey.equalsIgnoreCase(SOURCEROOT_KEY)) {
			printUsage(s);
			return;
		}
		
		File file = new File(sourcePath);
		if (!file.exists()) {
			System.out.println("folder does not exist: "+sourcePath);
			return;
		}
		
		if (!file.isDirectory()) {
			System.out.println("path is not a directory: "+sourcePath);
			return;
		}
			
		System.out.println("Autoqualifying Spring beans in "+mode+" mode within directory ["+file.getAbsolutePath()+"]");
		
		new Qualifier()
				.setMode(mode)
				.reviewSources(sourcePath)
				.executeActions();
	}

	private static void printUsage(String [] actualParams) {
		System.err.println("Usage: java -jar autoqualifier.jar "
							+ MODE_KEY + " <log|warnUnnamed|warnNamed|errorUnnamed|errorNamed|nameByClass|nameRandomly|unname> "
							+ SOURCEROOT_KEY+ " </full/path/to/source/root>");
		
		System.err.println("Your parameters: "+Arrays.asList(actualParams));
	}
	
}
