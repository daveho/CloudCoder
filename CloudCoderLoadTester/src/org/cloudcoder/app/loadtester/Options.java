// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.loadtester;

import java.util.HashMap;
import java.util.Map;

/**
 * Commands and command options for the {@link Main} driver.
 * All options are assumed to be in the form keyName=value.
 * 
 * @author David Hovemeyer
 */
public class Options {
	private String[] args;
	private String command;
	private Map<String, String> optMap;
	
	/**
	 * Constructor.
	 * 
	 * @param args command line arguments
	 */
	public Options(String[] args) {
		this.args = args;
		this.optMap = new HashMap<String, String>();
		
		// Set some defaults
		optMap.put("repeatCount", "1");
	}
	
	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * Parse the command and options (if any).
	 */
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

	/**
	 * Determine if the given option was specified.
	 * 
	 * @param key the option name
	 * @return true if the option was specified, false otherwise
	 */
	public boolean hasOption(String key) {
		return optMap.containsKey(key);
	}
	
	/**
	 * Get a string-valued option.
	 * 
	 * @param key option name
	 * @return value of the option
	 */
	public String getOptVal(String key) {
		String val = optMap.get(key);
		if (val == null) {
			throw new IllegalArgumentException("Missing value for option " + key);
		}
		return val;
	}

	/**
	 * Get an integer-valued option.
	 * 
	 * @param key option name
	 * @return value of the option
	 */
	public int getOptValAsInt(String key) {
		return Integer.parseInt(getOptVal(key));
	}

	/**
	 * Print usage.
	 */
	public void usage() {
		System.out.println("Usage: java -jar cloudcoderLoadTester.jar <command> [options]");
		System.out.println("Commands:");
		System.out.println("  captureAllEditSequences problemId=<problem id>");
		System.out.println("  execute hostConfig=<host config name> mix=<mix name> [numThreads=<n>] [repeatCount=<n>] [maxPause=<ms>]");
		System.out.println("  createTestUsers [hostConfig=<host config name>]");
	}
}