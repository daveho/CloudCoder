package org.cloudcoder.app.loadtester;

import java.util.HashMap;
import java.util.Map;

public class Options {
	private String[] args;
	private String command;
	private Map<String, String> optMap;
	
	public Options(String[] args) {
		this.args = args;
		this.optMap = new HashMap<String, String>();
		
		// Set some defaults
		optMap.put("repeatCount", "1");
	}
	
	public String getCommand() {
		return command;
	}
	
	public void parse() {
		if (args.length < 1) {
			throw new IllegalArgumentException("Please specify a command");
		}
		command = args[0];
		for (int i = 1; i < args.length; i++) {
			String opt = args[i];
			int eq = opt.indexOf('=');
			if (eq < 0) {
				throw new IllegalArgumentException("Invalid option: " + opt);
			}
			String key = opt.substring(0, eq);
			String val = opt.substring(eq+1);
			optMap.put(key, val);
		}
	}

	public boolean hasOption(String key) {
		return optMap.containsKey(key);
	}
	
	public String getOptVal(String key) {
		String val = optMap.get(key);
		if (val == null) {
			throw new IllegalArgumentException("Missing value for option " + key);
		}
		return val;
	}
	
	public int getOptValAsInt(String key) {
		return Integer.parseInt(getOptVal(key));
	}

	public void usage() {
		System.out.println("Usage: java -jar cloudcoderLoadTester.jar <command> [options]");
		System.out.println("Commands:");
		System.out.println("  captureAllEditSequences problemId=<problem id>");
		System.out.println("  execute hostConfig=<host config name> mix=<mix name> [numThreads=<n>] [repeatCount=<n>]");
		System.out.println("  createTestUsers");
	}
}