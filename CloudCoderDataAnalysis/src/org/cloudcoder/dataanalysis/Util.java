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

import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.cloudcoder.app.server.persist.JDBCDatabaseConfig;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;

/**
 * Data analysis utility methods.
 * 
 * @author David Hovemeyer
 */
public class Util {
	public static void readDatabaseProperties(Scanner keyboard, Properties config) {
		config.setProperty("cloudcoder.db.databaseName", Util.ask(keyboard, "Database name: "));
		config.setProperty("cloudcoder.db.user", Util.ask(keyboard, "Database username: "));
		config.setProperty("cloudcoder.db.passwd", Util.ask(keyboard, "Database password: "));
		config.setProperty("cloudcoder.db.host", Util.ask(keyboard, "Database hostname: "));
		config.setProperty("cloudcoder.db.portStr", Util.ask(keyboard, "Database port string (e.g., ':8889' for MAMP): "));
	}

	public static void connectToDatabase(Properties config) {
		JDBCDatabaseConfig.createFromProperties(config);
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
		console.setThreshold(Level.INFO);
		console.activateOptions();
		//add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);
	}

	public static void loadEmbeddedConfig(Properties config, ClassLoader classLoader) {
		Properties embeddedConfig =
				org.cloudcoder.daemon.Util.loadPropertiesFromResource(classLoader, "cloudcoder.properties");
		config.putAll(embeddedConfig);
	}

	/**
	 * Prompt user for {@link SnapshotSelectionCriteria}.
	 * 
	 * @param keyboard the scanner reading from the keyboard
	 * @return the {@link SnapshotSelectionCriteria}
	 */
	public static SnapshotSelectionCriteria getSnapshotSelectionCriteria(Scanner keyboard) {
		SnapshotSelectionCriteria criteria = new SnapshotSelectionCriteria();
		criteria.setCourseId(Integer.parseInt(ask(keyboard, "Course id (-1 for all): ")));
		criteria.setProblemId(Integer.parseInt(ask(keyboard, "Problem id (-1 for all): ")));
		criteria.setUserId(Integer.parseInt(ask(keyboard, "User id (-1 for all): ")));
		return criteria;
	}

	public static void configureCriteriaAndDatabase(Scanner keyboard, IAnalyzeSnapshots t, String[] args) {
		boolean interactive = false;
		for (String arg : args) {
			if (arg.equals("--interactiveConfig")) {
				interactive = true;
			} else {
				throw new IllegalArgumentException("Unknown option: " + arg);
			}
		}
		
		configureLogging();
		
		SnapshotSelectionCriteria criteria = getSnapshotSelectionCriteria(keyboard);
	
		t.setCriteria(criteria);
	
		Properties config = new Properties();
		if (interactive) {
			readDatabaseProperties(keyboard, config);
		} else {
			loadEmbeddedConfig(config, TimeToSolve.class.getClassLoader());
		}
		Util.connectToDatabase(config);
		
		t.setConfig(config);
	}

}
