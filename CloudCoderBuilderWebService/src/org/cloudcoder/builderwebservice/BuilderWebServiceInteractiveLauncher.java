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

package org.cloudcoder.builderwebservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Interactive launcher for the builder web service.
 * For development use only.
 * 
 * @author David Hovemeyer
 */
public class BuilderWebServiceInteractiveLauncher {
	public static void main(String[] args) throws IOException {
		System.out.println("Launching the BuilderWebService interactively");
		System.out.println("Type 'shutdown' to quit");
		
		CloudCoderBuilderWebServiceDaemon daemon = new CloudCoderBuilderWebServiceDaemon();

		// Assume that valid config properties can be found in ../cloudcoder.properties
		Properties config = new Properties();
		config.load(new FileReader("../cloudcoder.properties"));
		
		// Since we are running interactively, force the default keystore to be used.
		config.setProperty("cloudcoder.submitsvc.ssl.keystore", "defaultkeystore.jks");
		
		daemon.setCloudcoderProperties(config);

		// Ensure logs directory exists
		new File("logs").mkdirs();
		
		// Start the daemon.
		daemon.start("instance");

		// Handle commands.
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String command = reader.readLine();
			if (command == null) {
				break;
			}
			command = command.trim().toLowerCase();
			if (command.equals("shutdown")) {
				break;
			}
			
			daemon.handleCommand(command);
		}

		// Shut down the daemon.
		System.out.print("Shutting down...");
		System.out.flush();
		daemon.shutdown();
		System.out.println("done");
	}
}
