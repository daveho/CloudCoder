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

package org.cloudcoder.dataanalysis;

import java.util.Scanner;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.cloudcoder.app.server.persist.JDBCDatabaseConfig;

/**
 * Data analysis utility methods.
 * 
 * @author David Hovemeyer
 */
public class Util {

	public static void connectToDatabase(Scanner keyboard) {
		final String dbName = Util.ask(keyboard, "Database name: ");
		final String dbUser = Util.ask(keyboard, "Database username: ");
		final String dbPasswd = Util.ask(keyboard, "Database password: ");
		final String dbHost = Util.ask(keyboard, "Database hostname: ");
		final String dbPortStr = Util.ask(keyboard, "Database port string (e.g., ':8889' for MAMP): ");
		
		JDBCDatabaseConfig.ConfigProperties config = new JDBCDatabaseConfig.ConfigProperties() {
			
			@Override
			public String getUser() {
				return dbUser;
			}
			
			@Override
			public String getPortStr() {
				return dbPortStr;
			}
			
			@Override
			public String getPasswd() {
				return dbPasswd;
			}
			
			@Override
			public String getHost() {
				return dbHost;
			}
			
			@Override
			public String getDatabaseName() {
				return dbName;
			}
		};
		
		JDBCDatabaseConfig.create(config);
	}

	public static String ask(Scanner keyboard, String prompt) {
		System.out.print(prompt);
		return keyboard.nextLine();
	}

	public static void configureLogging() {
		// From: http://stackoverflow.com/questions/8965946/configuring-log4j-loggers-programmatically
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.FATAL);
		console.activateOptions();
		//add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
	}

}
