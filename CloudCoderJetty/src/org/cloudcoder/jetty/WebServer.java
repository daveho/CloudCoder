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

package org.cloudcoder.jetty;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cloudcoder.daemon.DaemonController;

/**
 * Base class for webservers that use embedded Jetty to launch and
 * control a web application.  Contains common support code for handling
 * commands (including {@link AdminCommand}s).
 * 
 * @author David Hovemeyer
 */
public abstract class WebServer {
	private DaemonController daemonController;
	private Map<String, AdminCommand> adminCommandMap;
	
	/**
	 * Constructor.
	 * 
	 * @param daemonController the {@link DaemonController} that will start/control/shutdown
	 *                         the web server daemon
	 */
	public WebServer(DaemonController daemonController) {
		this.daemonController = daemonController;
		this.adminCommandMap = new TreeMap<String, AdminCommand>();
	}

	/**
	 * Add an {@link AdminCommand}
	 * 
	 * @param name        command name
	 * @param mainClass   command main class
	 * @param description description
	 */
	protected void addAdminCommand(String name, String mainClass, String description) {
		AdminCommand adminCommand = new AdminCommand(name, mainClass, description);
		adminCommandMap.put(adminCommand.getName(), adminCommand);
	}
	
	/**
	 * Handle command line arguments by either running an {@link AdminCommand},
	 * or delegating them to the {@link DaemonController}.
	 * 
	 * @param args command line arguments to handle
	 */
	public void handleCommand(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("Please specify a command. Use");
			System.out.println("   java -jar <jarfile> help");
			System.out.println("to show possible commands");
			return;
		}
		
		String command = args[0];
		
		if (command.equals("help") || command.equals("-h") || command.equals("--help")) {
			printHelp();
			return;
		}
		
		if (adminCommandMap.containsKey(command)) {
			// Run an administrative command
			AdminCommand adminCommand = adminCommandMap.get(command);
			runAdminCommand(adminCommand, args);
		} else {
			// Delegate the command to the DaemonController
			daemonController.exec(args);
		}
	}

	private void runAdminCommand(AdminCommand adminCommand, String[] args) throws Exception {
		// Collect command line arguments to send to the administrative program
		List<String> cmdLineArgs = new LinkedList<String>(Arrays.asList(args));
		cmdLineArgs.remove(0);
		
		// Run the administrative program.
		NestedJarClassLoader.runMain(this.getClass(), adminCommand.getMainClass(), cmdLineArgs);
	}

	private void printHelp() {
		System.out.println("Usage: java -jar <jarfile> <command> [command arguments...]");
		System.out.println("Web application commands:");
		System.out.println("   start               start the web application");
		System.out.println("   shutdown            shut down the web application");
		System.out.println("Admin commands:");
		for (Map.Entry<String, AdminCommand> entry : adminCommandMap.entrySet()) {
			AdminCommand adminCommand = entry.getValue();
			System.out.print("   ");
			System.out.print(adminCommand.getName());
			System.out.print("                    ".substring(0, 20 - adminCommand.getName().length()));
			System.out.println(adminCommand.getDescription().replace("\n", "\n                       "));
		}
	}
}
