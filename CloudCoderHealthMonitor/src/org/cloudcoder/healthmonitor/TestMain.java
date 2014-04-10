// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.healthmonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.RootLogger;

/**
 * Just for testing.
 * 
 * @author David Hovemeyer
 */
public class TestMain {
	public static void main(String[] args) throws IOException {
		// Log to console
		ConsoleAppender ca = new ConsoleAppender();
		ca.setWriter(new OutputStreamWriter(System.out));
		ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
		RootLogger.getRootLogger().addAppender(ca);
		RootLogger.getRootLogger().setLevel(Level.DEBUG);
		
		HealthMonitorDaemon daemon = new HealthMonitorDaemon();
		
		HealthMonitorConfig config = new HealthMonitorConfig();
		config.addWebappInstance("https://cs.ycp.edu/cloudcoder");
		config.addWebappInstance("https://cloudcoder.org/demo/");
		config.addWebappInstance("http://localhost:8081/cloudcoder");
		
		// Prompt for info that we can't commit to a public git repository :-)
		Scanner keyboard = new Scanner(System.in);
		config.setReportEmailAddress(ask(keyboard, "Report email address: "));
		config.setSmtpUsername(ask(keyboard, "SMTP username: "));
		config.setSmtpPassword(ask(keyboard, "SMTP password: "));
		config.setSmtpServer(ask(keyboard, "SMTP server: "));
		config.setSmtpPort(Integer.parseInt(ask(keyboard, "SMTP port: ")));
		config.setSmtpUseTLS(true);
		
		daemon.setConfig(config);
		
		daemon.start("instance");

		System.out.println("Health monitor daemon started: type 'shutdown' to quit");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String line = reader.readLine();
			if (line == null || line.trim().toLowerCase().equals("shutdown")) {
				break;
			}
			daemon.handleCommand(line.trim());
		}
		
		System.out.println("Health monitor daemon shutting down...");
		daemon.shutdown();
		System.out.println("Done");
	}

	private static String ask(Scanner keyboard, String prompt) {
		System.out.print(prompt);
		return keyboard.nextLine();
	}
}
