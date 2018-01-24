// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2017, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2018, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.server.persist.util.SchemaUtil;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemAuthorship;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.StartedQuiz;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;

/**
 * Create the webapp database, using the metadata information
 * specified by the model classes.
 * 
 * @author David Hovemeyer
 */
public class CreateWebappDatabase {
	static final boolean DEBUG = false;
	
	/**
	 * Array with all of the tables needed to run the webapp.
	 */
	public static final ModelObjectSchema<?>[] TABLES = {
		Change.SCHEMA,
		ConfigurationSetting.SCHEMA,
		Course.SCHEMA,
		CourseRegistration.SCHEMA,
		Event.SCHEMA,
		Problem.SCHEMA,
		SubmissionReceipt.SCHEMA,
		Term.SCHEMA,
		TestCase.SCHEMA,
		TestResult.SCHEMA,
		User.SCHEMA,
		Quiz.SCHEMA,
		Module.SCHEMA,
		StartedQuiz.SCHEMA,
	};
	
	private static class Props {

		public String ccUserName;
		public String ccPassword;
		public String ccFirstname;
		public String ccLastname;
		public String ccEmail;
		public String ccWebsite;
		public String ccInstitutionName;
		public String ccRepoUrl;
		
		public List<String> termNames = new ArrayList<String>();

		public Props() {
			termNames.addAll(Arrays.asList("Winter", "Spring", "Summer", "Summer 1", "Summer 2", "Fall"));
		}
	}
	
	public static void main(String[] args) throws Exception {
		ConfigurationUtil.configureLog4j();
		
		try {
			Props props = null;
			
			for (String arg : args) {
				if (arg.startsWith("--props=")) {
					// Properties specified by bootstrap.pl: allows non-interactive
					// creation of the database
					props = new Props();
					arg = arg.substring("--props=".length());
					String[] arr = arg.split(",");
					for (String prop : arr) {
						int eq = prop.indexOf('=');
						String name = prop.substring(0, eq);
						String val = prop.substring(eq + 1);
						
						if (name.equals("ccUser")) {
							props.ccUserName = val;
						} else if (name.equals("ccPassword")) {
							props.ccPassword = val;
						} else if (name.equals("ccFirstName")) {
							props.ccFirstname = val;
						} else if (name.equals("ccLastName")) {
							props.ccLastname = val;
						} else if (name.equals("ccEmail")) {
							props.ccEmail = val;
						} else if (name.equals("ccWebsite")) {
							props.ccWebsite = val;
						} else if (name.equals("ccInstitutionName")) {
							props.ccInstitutionName = val;
						} else if (name.equals("ccRepoUrl")) {
							props.ccRepoUrl = val;
						}
					}
				} else {
					throw new IllegalArgumentException("Unknown option: " + arg);
				}
			}
			
			if (props != null) {
				// Non-interactive configuration (probably from bootstrap.pl)
				doCreateWebappDatabase(props);
			} else {
				// Interactive configuration
				createWebappDatabase();
			}
		} catch (SQLException e) {
			// Handle SQLException by printing an error message:
			// these are likely to be meaningful to the user
			// (for example, can't create the database because
			// it already exists.)
		    e.printStackTrace(System.err);
			System.err.println("Database error: " + e.getMessage());
		}
	}

	private static void createWebappDatabase() throws ClassNotFoundException,
			FileNotFoundException, IOException, SQLException {
		System.out.println("Please enter some information needed to configure the Cloudcoder");
		System.out.println("database.  (Hit enter to accept a default value, if there is one.)");
		
		Scanner keyboard = new Scanner(System.in);
		
		Props props = new Props();
		
		System.out.println("\n  >> Note: the user account you create will have superuser privileges <<\n");
		props.ccUserName = ConfigurationUtil.ask(keyboard, "Enter a username for your CloudCoder account: ");
		props.ccPassword = ConfigurationUtil.ask(keyboard, "Enter a password for your CloudCoder account");
		props.ccFirstname = ConfigurationUtil.ask(keyboard, "What is your first name?");
		props.ccLastname= ConfigurationUtil.ask(keyboard, "What is your last name?");
		props.ccEmail= ConfigurationUtil.ask(keyboard, "What is your email address?");
		props.ccWebsite = ConfigurationUtil.ask(keyboard, "What is your website URL?");
		props.ccInstitutionName = ConfigurationUtil.ask(keyboard, "What is your institution name (e.g, 'Unseen University')?");
		props.ccRepoUrl = ConfigurationUtil.ask(keyboard, "Enter the URL of the exercise repository", "https://cloudcoder.org/repo");
		
		choseTerms(keyboard, props);
		
		doCreateWebappDatabase(props);
	}

	private static void choseTerms(Scanner keyboard, Props props) {
		System.out.println("\nHere are the default academic terms that CloudCoder will use:");
		for (String termName : props.termNames) {
			System.out.println("  " + termName);
		}
		String ans = ConfigurationUtil.ask(keyboard, "\nUse these? (yes/no, answer no to define your own terms) ");
		if (ans.toLowerCase().equals("yes")) {
			return;
		}
		boolean done = false;
		while (!done) {
			int numTerms = Integer.parseInt(ConfigurationUtil.ask(keyboard, "How many terms? "));
			List<String> termNames = new ArrayList<String>();
			System.out.println("Please enter the terms in chronological order:");
			for (int i = 0; i < numTerms; i++) {
				String termName = ConfigurationUtil.ask(keyboard, "Name of term " + (i+1) + ": ");
				termNames.add(termName);
			}
			String ans2 = ConfigurationUtil.ask(keyboard, "Are these terms correct? (yes/no) ");
			if (ans2.toLowerCase().equals("yes")) {
				props.termNames.clear();
				props.termNames.addAll(termNames);
				done = true;
			}
		}
	}

	private static void doCreateWebappDatabase(Props props)
			throws ClassNotFoundException, IOException, SQLException {
		Class.forName(JDBCDatabase.JDBC_DRIVER_CLASS);

		Properties config = DBUtil.getConfigProperties();
		
		Connection conn = DBUtil.connectToDatabaseServer(config, "cloudcoder.db");
		
		String dbName = config.getProperty("cloudcoder.db.databaseName");
		if (dbName == null) {
			throw new IllegalStateException("configuration properties do not define cloudcoder.db.databaseName");
		}
		
		System.out.println("Creating database");
		DBUtil.createDatabase(conn, dbName);
		
		conn.close();
		
		// Reconnect to the newly-created database
		conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
		
		// Create schema version table
		System.out.println("Creating schema version table...");
		SchemaUtil.createSchemaVersionTableIfNeeded(conn, TABLES);
		
		// Create tables and indexes
		for (ModelObjectSchema<?> schema : TABLES) {
			createTable(conn, schema);
		}
		
		// Create initial database contents
		
		// Set institution name (and any other configuration settings)
		System.out.println("Adding configuration settings...");

		DBUtil.storeConfigurationSetting(conn, ConfigurationSettingName.PUB_TEXT_INSTITUTION, props.ccInstitutionName);
		DBUtil.storeConfigurationSetting(conn, ConfigurationSettingName.PUB_REPOSITORY_URL, props.ccRepoUrl);
		
		// Terms
		System.out.println("Creating terms...");
		int count = 0;
		Term firstTerm = null;
		for (String termName : props.termNames) {
			Term t = CreateWebappDatabase.storeTerm(conn, termName, count++);
			if (firstTerm == null) {
				firstTerm = t;
			}
		}
		
		// Create an initial demo course
		System.out.println("Creating demo course...");
		int courseId = CreateSampleData.createDemoCourse(conn, firstTerm);
		
		// Create an initial user
		System.out.println("Creating initial user...");
		int userId = ConfigurationUtil.createOrUpdateUser(conn, 
				props.ccUserName, 
				props.ccFirstname,
				props.ccLastname,
				props.ccEmail,
				props.ccPassword,
				props.ccWebsite);
		
		// Make the initial user a superuser
		User initialUser = new User();
		initialUser.setId(userId);
		DBUtil.loadModelObject(conn, initialUser);
		initialUser.setSuperuser(true);
		DBUtil.updateModelObject(conn, initialUser);
		
		// Register the user as an instructor in the demo course
		System.out.println("Registering initial user for demo course...");
		ConfigurationUtil.registerUser(conn, userId, courseId, CourseRegistrationType.INSTRUCTOR, 101);
		
		// Add sample exercises
		System.out.println("Adding sample exercises to demo course...");
		List<ProblemAndTestCaseList> sampleExercises = CreateSampleData.getSampleExercises();
		for (ProblemAndTestCaseList ex : sampleExercises) {
			System.out.println("  " + ex.getProblem().getTestname());
			storeExercise(ex, courseId, conn);
		}

		conn.close();
		
		System.out.println("Success!");
	}

	private static void storeExercise(ProblemAndTestCaseList ex, int courseId, Connection conn) throws SQLException {
		ex.getProblem().setCourseId(courseId);
		
		// All of the sample problems are imported from the repository
		ex.getProblem().setProblemAuthorship(ProblemAuthorship.IMPORTED);
		
		DBUtil.storeModelObject(conn, ex.getProblem());
		int problemId = ex.getProblem().getProblemId();
		for (TestCase tc : ex.getTestCaseData()) {
			tc.setProblemId(problemId);
			DBUtil.storeModelObject(conn, tc);
		}
	}

	private static<E> void createTable(Connection conn, ModelObjectSchema<E> schema) throws SQLException {
		System.out.println("Creating table " + schema.getDbTableName());
		DBUtil.createTable(conn, schema);
	}

	private static Term storeTerm(Connection conn, String name, int seq) throws SQLException {
		Term term = new Term();
		term.setName(name);
		term.setSeq(seq);

		DBUtil.storeModelObject(conn, term);

		return term;
	}
}
