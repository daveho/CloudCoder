package org.cloudcoder.app.loadtester;

/**
 * Entry point for load tester.
 * 
 * @author David Hovemeyer
 */
public class Main {
	public static void main(String[] args) throws Exception {
		Options opts = new Options(args);
		
		try {
			opts.parse();
		} catch (IllegalArgumentException e) {
			System.out.println("Error: " + e.getMessage());
			opts.usage();
			System.exit(1);
		}
		
		String command = opts.getCommand();
		if (command.equals("captureAllEditSequences")) {
			int problemId = opts.getOptValAsInt("problemId");
			String outputDir = opts.getOptVal("outputDir");
			CaptureAllEditSequencesForProblem.execute(problemId, outputDir);
		}
	}
}
