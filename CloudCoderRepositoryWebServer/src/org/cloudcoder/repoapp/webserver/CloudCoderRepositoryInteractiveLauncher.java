// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.repoapp.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Run the CloudCoderRepository webapp interactively, for development only. 
 *
 * @author David Hovemeyer
 */
public class CloudCoderRepositoryInteractiveLauncher {
	public static void main(String[] args) throws IOException {
		System.out.println("Launching the CloudCoderRepository webapp interactively");
		System.out.println("Type 'shutdown' to quit");
		
		CloudCoderRepositoryDaemon daemon = new CloudCoderRepositoryDaemon();
		
		// Run the webapp out of its Eclipse project (which is assumed to
		// be a sibling of this Eclipse project.)
		daemon.setWebappUrl("file:../CloudCoderRepository/war");
		
		// Setting empty config properties should result in default values
		// being used, which should be appropriate for development.
		daemon.setConfigProperties(new Properties());
		
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
