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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.CreateWebappDatabase;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.JDBCDatabaseConfig;
import org.cloudcoder.app.server.persist.SchemaVersionChecker;
import org.cloudcoder.app.shared.model.Anonymization;
import org.cloudcoder.app.shared.model.ModelObjectSchema;

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
	public static void main(String[] args) throws IOException {
		Util.configureLogging();
		
		Properties config = new Properties();
		
		Scanner keyboard = new Scanner(System.in);
		
		Util.readDatabaseProperties(keyboard, config);
		
		Util.connectToDatabase(config);
		System.out.println("Checking schema...");
		checkSchema();
		
		String identityFile = Util.ask(keyboard, "Filename for mapping anon usernames to real identities: ");
		
		String genPasswd = Util.ask(keyboard, "Password to use for all accounts: ");
		
		String dbName = JDBCDatabaseConfig.getInstance().getConfigProperties().getDatabaseName();
		System.out.println("================================================================================");
		System.out.println("You are about to destructively anonymize the database " + dbName);
		System.out.println("Are you sure you want to do this?  There is no going back if you say yes!");
		System.out.println("================================================================================");
		String ans = Util.ask(keyboard, "Anonymize database " + dbName + " (yes/no)? ");
		if (!ans.toLowerCase().equals("yes")) {
			System.out.println("Not anonymizing.  Bye!");
			System.exit(0);
		}
		
		// File for mapping anon identities to real identities
		PrintWriter pw = new PrintWriter(new FileWriter(identityFile));
		
		// Execute!
		System.out.print("Anonymizing...");
		System.out.flush();
		List<Anonymization> anonymizationList = Database.getInstance().anonymizeUserData(
				genPasswd,
				new Runnable(){
					@Override
					public void run() {
						System.out.print(".");
						System.out.flush();
					}
				}
		);
		System.out.println("done");
		
		// Save anonymized identities
		saveAnonymizedIdentities(pw, anonymizationList);
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
					w.writeNext(
							String.valueOf(a.getUserId()),
							a.getAnonUsername(),
							a.getGenPassword(),
							a.getRealUsername(),
							a.getRealFirstname(),
							a.getRealLastname(),
							a.getRealEmail(),
							a.getRealWebsite());
				}
			}
		});
		pw.close();
	}
}
