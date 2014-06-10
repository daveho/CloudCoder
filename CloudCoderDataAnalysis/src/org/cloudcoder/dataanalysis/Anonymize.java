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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.cloudcoder.app.server.persist.CreateWebappDatabase;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.IDatabase;
import org.cloudcoder.app.server.persist.JDBCDatabaseConfig;
import org.cloudcoder.app.server.persist.PasswordUtil;
import org.cloudcoder.app.server.persist.SchemaVersionChecker;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.User;

import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVWriteProc;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Anonymize a CloudCoder database.
 * Saves a file mapping the anonymized user accounts to the original
 * user identity information.
 * 
 * @author David Hovemeyer
 */
public class Anonymize {
	private static class Anonymization {
		int userId;
		String anonUsername;
		String genPassword;
		String realUsername;
		String realFirstname;
		String realLastname;
		String realEmail;
		String realWebsite;
		
		Anonymization(int userId, String anonUsername, String genPassword, String realUsername, String realFirstname, String realLastname, String realEmail, String realWebsite) {
			this.userId = userId;
			this.anonUsername = anonUsername;
			this.genPassword = genPassword;
			this.realUsername = realUsername;
			this.realFirstname = realFirstname;
			this.realLastname = realLastname;
			this.realEmail = realEmail;
			this.realWebsite = realWebsite;
		}
	}

	public static void main(String[] args) throws IOException {
		configureLogging();
		
		Scanner keyboard = new Scanner(System.in);
		
		connectToDatabase(keyboard);
		System.out.println("Checking schema...");
		checkSchema();
		
		String identityFile = ask(keyboard, "Filename for mapping anon usernames to real identities: ");
		
		String genPasswd = ask(keyboard, "Password to use for all accounts: ");
		
		String dbName = JDBCDatabaseConfig.getInstance().getConfigProperties().getDatabaseName();
		System.out.println("================================================================================");
		System.out.println("You are about to destructively anonymize the database " + dbName);
		System.out.println("Are you sure you want to do this?  There is no going back if you say yes!");
		System.out.println("================================================================================");
		String ans = ask(keyboard, "Anonymize database " + dbName + " (yes/no)? ");
		if (!ans.toLowerCase().equals("yes")) {
			System.out.println("Not anonymizing.  Bye!");
			System.exit(0);
		}
		
		// File for mapping anon identities to real identities
		PrintWriter pw = new PrintWriter(new FileWriter(identityFile));
		
		IDatabase db = Database.getInstance();
		// Execute!
		System.out.print("Anonymizing...");
		System.out.flush();
		final List<Anonymization> anonymizationList = new ArrayList<Anonymize.Anonymization>();
		anonymizeUserData(db, anonymizationList, genPasswd);
		System.out.println("done");
		
		// Save anonymized identities
		saveAnonymizedIdentities(pw, anonymizationList);
	}

	private static void configureLogging() {
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

	private static void connectToDatabase(Scanner keyboard) {
		final String dbName = ask(keyboard, "Database name: ");
		final String dbUser = ask(keyboard, "Database username: ");
		final String dbPasswd = ask(keyboard, "Database password: ");
		final String dbHost = ask(keyboard, "Database hostname: ");
		final String dbPortStr = ask(keyboard, "Database port string (e.g., ':8889' for MAMP): ");
		
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

	private static String ask(Scanner keyboard, String prompt) {
		System.out.print(prompt);
		return keyboard.nextLine();
	}
	
	private static final class SchemaCheckReporter implements SchemaVersionChecker.Reporter {
		private int numErrors = 0;
		
		public int getNumErrors() {
			return numErrors;
		}
		
		@Override
		public void reportTableWrongVersion(ModelObjectSchema<?> schema, int dbTableSchemaVersion) {
			System.out.println(
					"Table " + schema.getDbTableName() +
					" has incorrect version (found " + dbTableSchemaVersion +
					", expected " + schema.getVersion() + ")");
			numErrors++;
		}

		@Override
		public void reportMissingSchemaVersion(ModelObjectSchema<?> schema) {
			System.out.println("Could not find schema version for table " + schema.getDbTableName());
			numErrors++;
		}

		@Override
		public void reportGeneralError(String error) {
			System.out.println("Database error: " + error);
			numErrors++;
		}
	}

	private static void checkSchema() {
		SchemaVersionChecker checker = new SchemaVersionChecker();
		SchemaCheckReporter reporter = new SchemaCheckReporter();
		checker.check(Arrays.asList(CreateWebappDatabase.TABLES), reporter);
		if (reporter.getNumErrors() > 0) {
			System.out.println("Did not verify that schema is up to date: exiting");
			System.exit(1);
		}
	}

	private static void anonymizeUserData(IDatabase db, final List<Anonymization> anonymizationList, final String genPasswd) {
		AbstractDatabaseRunnableNoAuthException<Boolean> txn = new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			@Override
			public Boolean run(Connection conn) throws SQLException {
				// Get all users
				PreparedStatement getUsers = prepareStatement(conn, "select * from cc_users");
				ResultSet resultSet = executeQuery(getUsers);
				while (resultSet.next()) {
					User user = new User();
					DBUtil.loadModelObjectFields(user, User.SCHEMA, resultSet);
					
					Anonymization a = new Anonymization(
							user.getId(), "x", "x", user.getUsername(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getWebsite());
					anonymizationList.add(a);
				}
				System.out.print("[" + anonymizationList.size() + " users]");
				
				// Generate fake usernames and change each user to have
				// the same password
				for (Anonymization a : anonymizationList) {
					a.anonUsername = String.format("u%05d", a.userId);
					a.genPassword = genPasswd;
				}
				
				// Anonymize!
				PreparedStatement update = prepareStatement(
						conn,
						"update cc_users " +
						"   set username = ?, password_hash = ?, firstname = ?, lastname = ?, email = ?, website = ? " +
						" where id = ?" 
				);
				int numBatched = 0;
				for (Anonymization a : anonymizationList) {
					update.setString(1, a.anonUsername);
					String passwordHash = PasswordUtil.hashPassword(a.genPassword);
					update.setString(2, passwordHash);
					update.setString(3, a.anonUsername);
					update.setString(4, a.anonUsername);
					update.setString(5, a.anonUsername + "@anon.edu");
					update.setString(6, "x");
					update.setInt(7, a.userId);
					
					update.addBatch();
					
					numBatched++;
					
					if (numBatched >= 20) {
						update.executeBatch();
						numBatched = 0;
						System.out.print(".");
						System.out.flush();
					}
				}
				
				if (numBatched > 0) {
					update.executeBatch();
					System.out.print(".");
					System.out.flush();
				}
				
				return true;
			}
			
			@Override
			public String getDescription() {
				return " anonymizing user information";
			}
		};
		
		db.databaseRun(txn);
	}

	private static void saveAnonymizedIdentities(PrintWriter pw, final List<Anonymization> anonymizationList) {
		CSV csv = CSV
				.separator(',')  // delimiter of fields
				.quote('"')      // quote character
				.create();       // new instance is immutable
		csv.write(pw, new CSVWriteProc() {
			@Override
			public void process(CSVWriter w) {
				w.writeNext("id", "anonUsername", "genPassword", "realUsername", "realFirstname", "realLastname", "realEmail", "realWebsite");
				for (Anonymization a : anonymizationList) {
					w.writeNext(String.valueOf(a.userId), a.anonUsername, a.genPassword, a.realUsername, a.realFirstname, a.realLastname, a.realEmail, a.realWebsite);
				}
			}
		});
		pw.close();
	}
}
