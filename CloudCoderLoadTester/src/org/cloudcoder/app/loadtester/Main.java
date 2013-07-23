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

import java.io.IOException;

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
			doCaptureEditSequences(opts);
		} else if (command.equals("execute")) {
			doExecute(opts);
		} else if (command.equals("createTestUsers")) {
			HostConfig hostConfig = opts.hasOption("hostConfig")
					? getHostConfig(opts.getOptVal("hostConfig")) : HostConfigDatabase.forName("default");
			CreateTestUsers.createTestUserAccounts(hostConfig);
		} else {
			System.out.println("Unknown command: " + command);
			opts.usage();
			System.exit(1);
		}
	}

	private static void doCaptureEditSequences(Options opts) throws IOException {
		int problemId = opts.getOptValAsInt("problemId");
		String outputDir = opts.getOptVal("outputDir");
		CaptureAllEditSequencesForProblem.execute(problemId, outputDir);
	}

	private static void doExecute(Options opts) {
		HostConfig hostConfig = getHostConfig(opts.getOptVal("hostConfig"));

		String mixName = opts.getOptVal("mix");
		Mix mix = MixDatabase.forName(mixName);
		int numThreads = mix.size();
		if (opts.hasOption("numThreads")) {
			numThreads = opts.getOptValAsInt("numThreads");
		}
		int repeatCount = opts.getOptValAsInt("repeatCount");
		
		long maxPause = CompressEditSequence.DEFAULT_MAX_PAUSE_TIME_MS;
		if (opts.hasOption("maxPause")) {
			maxPause = opts.getOptValAsInt("maxPause");
		}
		
		LoadTester loadTester = new LoadTester();
		loadTester.setHostConfig(hostConfig);
		loadTester.setMix(mix);
		loadTester.setNumThreads(numThreads);
		loadTester.setRepeatCount(repeatCount);
		loadTester.setMaxPause(maxPause);
		
		loadTester.execute();
	}

	private static HostConfig getHostConfig(String hostConfigName) {
		HostConfig hostConfig;
		if (hostConfigName.indexOf(",") >= 0) {
			// Host config is specified in the form
			//    protocol,hostname,port,contextPath
			String[] fields = hostConfigName.split(",");
			hostConfig = new HostConfig(fields[0], fields[1], Integer.parseInt(fields[2]), fields[3]);
		} else {
			// Host config is one of the named configs in HostConfigDatabase
			hostConfig = HostConfigDatabase.forName(hostConfigName);
		}
		return hostConfig;
	}
}
