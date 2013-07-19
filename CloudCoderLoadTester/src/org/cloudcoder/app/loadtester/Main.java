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
		} else if (command.equals("execute")) {
			String hostConfigName = opts.getOptVal("hostConfig");
			HostConfig hostConfig = HostConfigDatabase.forName(hostConfigName);
			String mixName = opts.getOptVal("mix");
			Mix mix = MixDatabase.forName(mixName);
			int numThreads = mix.size();
			if (opts.hasOption("numThreads")) {
				numThreads = opts.getOptValAsInt("numThreads");
			}
			int repeatCount = opts.getOptValAsInt("repeatCount");
			
			LoadTester loadTester = new LoadTester();
			loadTester.setHostConfig(hostConfig);
			loadTester.setMix(mix);
			loadTester.setNumThreads(numThreads);
			loadTester.setRepeatCount(repeatCount);
			
			loadTester.execute();
		} else if (command.equals("createTestUsers")) {
			CreateTestUsers.createTestUserAccounts();
		} else {
			System.out.println("Unknown command: " + command);
			opts.usage();
			System.exit(1);
		}
	}
}
