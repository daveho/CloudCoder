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

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.IContainsEvent;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of IDatabase using JDBC.
 * 
 * @author David Hovemeyer
 */
public class JDBCDatabase implements IDatabase {
	// Constants for table names
	private static final String TEST_RESULTS = "cc_test_results";
	private static final String TEST_CASES = "cc_test_cases";
	private static final String SUBMISSION_RECEIPTS = "cc_submission_receipts";
	private static final String TERMS = "cc_terms";
	private static final String EVENTS = "cc_events";
	private static final String CHANGES = "cc_changes";
	private static final String COURSE_REGISTRATIONS = "cc_course_registrations";
	private static final String COURSES = "cc_courses";
	private static final String PROBLEMS = "cc_problems";
	private static final String USERS = "cc_users";
	private static final String CONFIGURATION_SETTINGS = "cc_configuration_settings";

	private static final Logger logger=LoggerFactory.getLogger(JDBCDatabase.class);
	
	private String jdbcUrl;
	
	public JDBCDatabase() {
		JDBCDatabaseConfig config = JDBCDatabaseConfig.getInstance();
		jdbcUrl = "jdbc:mysql://" +
				config.getDbHost() + config.getDbPortStr() +
				"/" +
				config.getDbDatabaseName() +
				"?user=" +
				config.getDbUser() +
				"&password=" + config.getDbPasswd();
	}
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			throw new IllegalStateException("Could not load mysql jdbc driver", e);
		}
	}
	
	private static class InUseConnection {
		Connection conn;
		int refCount;
	}
	
	/*
	 * Need to consider how to do connection management better.
	 * For now, just use something simple that works.
	 */

	private ThreadLocal<InUseConnection> threadLocalConnection = new ThreadLocal<InUseConnection>();
	
	private Connection getConnection() throws SQLException {
		InUseConnection c = threadLocalConnection.get();
		if (c == null) {
			c = new InUseConnection();
			c.conn = DriverManager.getConnection(jdbcUrl);
			c.refCount = 0;
			threadLocalConnection.set(c);
		}
		c.refCount++;
		return c.conn;
	}
	
	private void releaseConnection() throws SQLException {
		InUseConnection c = threadLocalConnection.get();
		c.refCount--;
		if (c.refCount == 0) {
			c.conn.close();
			threadLocalConnection.set(null);
		}
	}
	
	@Override
	public ConfigurationSetting getConfigurationSetting(final ConfigurationSettingName name) {
		return databaseRun(new AbstractDatabaseRunnable<ConfigurationSetting>() {
			@Override
			public ConfigurationSetting run(Connection conn)
					throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select s.* from " + CONFIGURATION_SETTINGS + " as s where s.name = ?");
				stmt.setString(1, name.toString());
				ResultSet resultSet = executeQuery(stmt);
				if (!resultSet.next()) {
					return null;
				}
				ConfigurationSetting configurationSetting = new ConfigurationSetting();
				load(configurationSetting, resultSet, 1);
				return configurationSetting;
			}
			@Override
			public String getDescription() {
				return "retrieving configuration setting";
			}
		});
	}
	
	@Override
	public User authenticateUser(final String userName, final String password) {
		return databaseRun(new AbstractDatabaseRunnable<User>() {
			@Override
			public User run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select * from " + USERS + " where username = ?");
				stmt.setString(1, userName);
				
				ResultSet resultSet = executeQuery(stmt);
				if (!resultSet.next()) {
					return null;
				}
				
				User user = new User();
				load(user, resultSet, 1);
				
				// Check password
				String encryptedPassword = HashPassword.computeHash(password, user.getSalt());
				
				logger.debug("Password check: " + encryptedPassword + ", " + user.getPasswordMD5());
				
				if (!encryptedPassword.equals(user.getPasswordMD5())) {
					// Password does not match
					return null;
				}
				
				// Authenticated!
				return user;
			}

			@Override
			public String getDescription() {
				return "retrieving user";
			}
		});
	}
	
	@Override
	public Problem getProblem(final User user, final int problemId) {
		return databaseRun(new AbstractDatabaseRunnable<Problem>() {
			@Override
			public Problem run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select " + PROBLEMS + ".* from " + PROBLEMS + ", " + COURSES + ", " + COURSE_REGISTRATIONS + " " +
						" where " + PROBLEMS + ".problem_id = ? " +
						"   and " + COURSES + ".id = " + PROBLEMS + ".course_id " +
						"   and " + COURSE_REGISTRATIONS + ".course_id = " + COURSES + ".id " +
						"   and " + COURSE_REGISTRATIONS + ".user_id = ?"
				);
				stmt.setInt(1, problemId);
				stmt.setInt(2, user.getId());
				
				ResultSet resultSet = executeQuery(stmt);
				
				if (!resultSet.next()) {
					// no such problem, or user is not authorized to see this problem
					return null;
				}
				
				Problem problem = new Problem();
				load(problem, resultSet, 1);
				return problem;
			}
			
			@Override
			public String getDescription() {
				return "retrieving problem";
			}
		});
	}
	
	@Override
	public Change getMostRecentChange(final User user, final int problemId) {
		return databaseRun(new AbstractDatabaseRunnable<Change>() {
			@Override
			public Change run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select c.* from " + CHANGES + " as c, " + EVENTS + " as e " +
						" where c.event_id = e.id " +
						"   and e.id = (select max(ee.id) from " + CHANGES + " as cc, " + EVENTS + " as ee " +
						"                where cc.event_id = ee.id " +
						"                  and ee.problem_id = ? " +
						"                  and ee.user_id = ?)"
				);
				stmt.setInt(1, problemId);
				stmt.setInt(2, user.getId());
				
				ResultSet resultSet = executeQuery(stmt);
				if (!resultSet.next()) {
					return null;
				}
				
				Change change = new Change();
				load(change, resultSet, 1);
				return change;
			}
			public String getDescription() {
				return "retrieving latest code change";
			}
		});
	}
	
	@Override
	public Change getMostRecentFullTextChange(final User user, final int problemId) {
		return databaseRun(new AbstractDatabaseRunnable<Change>() {
			@Override
			public Change run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select c.* from " + CHANGES + " as c, " + EVENTS + " as e " +
						" where c.event_id = e.id " +
						"   and e.id = (select max(ee.id) from " + CHANGES + " as cc, " + EVENTS + " as ee " +
						"                where cc.event_id = ee.id " +
						"                  and ee.problem_id = ? " +
						"                  and ee.user_id = ? " +
						"                  and cc.type = ?)"
				);
				stmt.setInt(1, problemId);
				stmt.setInt(2, user.getId());
				stmt.setInt(3, ChangeType.FULL_TEXT.ordinal());

				ResultSet resultSet = executeQuery(stmt);
				if (!resultSet.next()) {
					return null;
				}
				Change change = new Change();
				load(change, resultSet, 1);
				return change;
			}
			@Override
			public String getDescription() {
				return " retrieving most recent full text change";
			}
		});
	}
	
	@Override
	public List<Change> getAllChangesNewerThan(final User user, final int problemId, final int baseRev) {
		return databaseRun(new AbstractDatabaseRunnable<List<Change>>() {
			@Override
			public List<Change> run(Connection conn) throws SQLException {
				List<Change> result = new ArrayList<Change>();
				
				PreparedStatement stmt = prepareStatement(
						conn,
						"select c.* from " + CHANGES + " as c, " + EVENTS + " as e " +
						" where c.event_id = e.id " +
						"   and e.id > ? " +
						"   and e.user_id = ? " +
						"   and e.problem_id = ? " +
						" order by e.id asc"
				);
				stmt.setInt(1, baseRev);
				stmt.setInt(2, user.getId());
				stmt.setInt(3, problemId);
				
				ResultSet resultSet = executeQuery(stmt);
				while (resultSet.next()) {
					Change change = new Change();
					load(change, resultSet, 1);
					result.add(change);
				}
				
				return result;
			}
			@Override
			public String getDescription() {
				return " retrieving most recent text changes";
			}
		});
	}
	
	@Override
	public List<? extends Object[]> getCoursesForUser(final User user) {
		return databaseRun(new AbstractDatabaseRunnable<List<? extends Object[]>>() {
			@Override
			public List<? extends Object[]> run(Connection conn) throws SQLException {
				List<Object[]> result = new ArrayList<Object[]>();

				PreparedStatement stmt = prepareStatement(
						conn,
						"select c.*, t.*, r.* from " + COURSES + " as c, " + TERMS + " as t, " + COURSE_REGISTRATIONS + " as r " +
						" where c.id = r.course_id " + 
						"   and c.term_id = t.id " +
						"   and r.user_id = ? " +
						" order by c.year desc, t.seq desc"
				);
				stmt.setInt(1, user.getId());
				
				ResultSet resultSet = executeQuery(stmt);
				
				while (resultSet.next()) {
					Course course = new Course();
					load(course, resultSet, 1);
					Term term = new Term();
					load(term, resultSet, Course.NUM_FIELDS + 1);
					CourseRegistration reg = new CourseRegistration();
					load(reg, resultSet, Course.NUM_FIELDS + Term.NUM_FIELDS + 1);
					result.add(new Object[]{course, term, reg});
				}
				
				return result;
			}
			@Override
			public String getDescription() {
				return " retrieving courses for user";
			}
		});
	}

	@Override
	public List<Problem> getProblemsInCourse(final User user, final Course course) {
		return databaseRun(new AbstractDatabaseRunnable<List<Problem>>() {
			@Override
			public List<Problem> run(Connection conn) throws SQLException {
				return doGetProblemsInCourse(user, course, conn, this);
			}
			@Override
			public String getDescription() {
				return "retrieving problems for course";
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#getProblemAndSubscriptionReceiptsInCourse(org.cloudcoder.app.shared.model.User, org.cloudcoder.app.shared.model.Course)
	 */
	@Override
	public List<ProblemAndSubmissionReceipt> getProblemAndSubscriptionReceiptsInCourse(
			final User user, final Course course) {
		return databaseRun(new AbstractDatabaseRunnable<List<ProblemAndSubmissionReceipt>>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public List<ProblemAndSubmissionReceipt> run(Connection conn)
					throws SQLException {
				// Get all problems for this user/course
				List<Problem> problemList = doGetProblemsInCourse(user, course, conn, this);
				
				// Get all submission receipts for this user/course.
				// Note that we join on course_registrations in order to ensure 
				// that user is authorized to get information about the course.
				PreparedStatement stmt = prepareStatement(
						conn,
						"select r.*, e.* from " + SUBMISSION_RECEIPTS + " as r, " + PROBLEMS + " as p, " + EVENTS + " as e, " + COURSE_REGISTRATIONS + " as cr " +
						" where cr.user_id = ?" +
						"   and cr.course_id = ? " +
						"   and p.course_id = cr.course_id " +
						"   and e.problem_id = p.problem_id " +
						"   and e.user_id = cr.user_id " +
						"   and r.event_id = e.id "
				);
				stmt.setInt(1, user.getId());
				stmt.setInt(2, course.getId());
				
				// Map of problem ids to most recent submission receipt for problem
				Map<Integer, SubmissionReceipt> problemIdToMostRecentSubmissionReceiptMap = new HashMap<Integer, SubmissionReceipt>();
				ResultSet resultSet = executeQuery(stmt);
				while (resultSet.next()) {
					SubmissionReceipt submissionReceipt = new SubmissionReceipt();
					load(submissionReceipt, resultSet, 1);
					load(submissionReceipt.getEvent(), resultSet, SubmissionReceipt.NUM_FIELDS + 1);
					SubmissionReceipt current = problemIdToMostRecentSubmissionReceiptMap.get(submissionReceipt.getEvent().getProblemId());
					if (current == null || submissionReceipt.getEventId() > current.getEventId()) {
						problemIdToMostRecentSubmissionReceiptMap.put(submissionReceipt.getEvent().getProblemId(), submissionReceipt);
					}
				}
				
				// Match up problems and corresponding submission receipts.
				List<ProblemAndSubmissionReceipt> result = new ArrayList<ProblemAndSubmissionReceipt>();
				for (Problem problem : problemList) {
					SubmissionReceipt receipt = problemIdToMostRecentSubmissionReceiptMap.get(problem.getProblemId());
					ProblemAndSubmissionReceipt problemAndSubscriptionReceipt = new ProblemAndSubmissionReceipt(problem, receipt);
					result.add(problemAndSubscriptionReceipt);
				}
				
				return result;
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "retrieving problems and subscription receipts for course";
			}
		});
	}
	
	@Override
	public void storeChanges(final Change[] changeList) {
		databaseRun(new AbstractDatabaseRunnable<Boolean>() {
			@Override
			public Boolean run(Connection conn) throws SQLException {
				// Store Events
				storeEvents(changeList, conn, this);
				
				// Store Changes
				PreparedStatement insertChange = prepareStatement(
						conn,
						"insert into " + CHANGES + " values (NULL, ?, ?, ?, ?, ?, ?, ?)"
				);
				for (Change change : changeList) {
					storeNoId(change, insertChange, 1);
					insertChange.addBatch();
				}
				insertChange.executeBatch();
				
				return true;
			}
			@Override
			public String getDescription() {
				return "storing text changes";
			}
		});
	}
	
	@Override
	public List<TestCase> getTestCasesForProblem(final int problemId) {
		return databaseRun(new AbstractDatabaseRunnable<List<TestCase>>() {
			@Override
			public List<TestCase> run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select * from " + TEST_CASES + " where problem_id = ?");
				stmt.setInt(1, problemId);
				
				List<TestCase> result = new ArrayList<TestCase>();
				
				ResultSet resultSet = executeQuery(stmt);
				while (resultSet.next()) {
					TestCase testCase = new TestCase();
					load(testCase, resultSet, 1);
					result.add(testCase);
				}
				return result;
			}
			@Override
			public String getDescription() {
				return "getting test cases for problem";
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#insertSubmissionReceipt(org.cloudcoder.app.shared.model.SubmissionReceipt)
	 */
	@Override
	public void insertSubmissionReceipt(final SubmissionReceipt receipt, final TestResult[] testResultList_) {
		databaseRun(new AbstractDatabaseRunnable<Boolean>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public Boolean run(Connection conn) throws SQLException {
				doInsertSubmissionReceipt(receipt, testResultList_, conn, this);
				return true;
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "storing submission receipt";
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#addSubmissionReceiptIfNecessary(org.cloudcoder.app.shared.model.User, org.cloudcoder.app.shared.model.Problem)
	 */
	@Override
	public void getOrAddLatestSubmissionReceipt(final User user, final Problem problem) {
		databaseRun(new AbstractDatabaseRunnable<SubmissionReceipt>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public SubmissionReceipt run(Connection conn) throws SQLException {
				// Get most recent submission receipt for user/problem
				PreparedStatement stmt = prepareStatement(
						conn,
						"select r.*, e.* from " + SUBMISSION_RECEIPTS + " as r, " + EVENTS + " as e " +
						" where r.event_id = e.id " +
						"   and e.id = (select max(ee.id) from " + SUBMISSION_RECEIPTS + " as rr, " + EVENTS + " as ee " +
						"                where rr.event_id = ee.id " +
						"                  and ee.problem_id = ? " +
						"                  and ee.user_id = ?)");
				stmt.setInt(1, problem.getProblemId());
				stmt.setInt(2, user.getId());
				
				ResultSet resultSet = executeQuery(stmt);
				if (resultSet.next()) {
					SubmissionReceipt submissionReceipt = new SubmissionReceipt();
					load(submissionReceipt, resultSet, 1);
					load(submissionReceipt.getEvent(), resultSet, SubmissionReceipt.NUM_FIELDS + 1);
					return submissionReceipt;
				}
				
				// There is no submission receipt in the database yet, so add one
				// with status STARTED
				SubmissionStatus status = SubmissionStatus.STARTED;
				SubmissionReceipt receipt = SubmissionReceipt.create(user, problem, status, -1, 0, 0);
				doInsertSubmissionReceipt(receipt, new TestResult[0], conn, this);
				return receipt;
				
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "adding initial submission receipt if necessary";
			}
		});
	}
	
	public void addProblem(final Problem problem) {
		databaseRun(new AbstractDatabaseRunnable<Boolean>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public Boolean run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"insert into " + PROBLEMS + " values (NULL, ?, ?, ?, ?, ?, ?, ?, ?)",
						PreparedStatement.RETURN_GENERATED_KEYS
				);
				
				storeNoId(problem, stmt, 1);
				
				stmt.executeUpdate();
				
				ResultSet generatedKey = getGeneratedKeys(stmt);
				if (!generatedKey.next()) {
					throw new SQLException("Could not get generated key for inserted problem");
				}
				problem.setProblemId(generatedKey.getInt(1));
				
				return true;
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "adding Problem";
			}
		});
	}
	
	public void addTestCases(final Problem problem, final List<TestCase> testCaseList) {
		databaseRun(new AbstractDatabaseRunnable<Boolean>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public Boolean run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"insert into " + TEST_CASES + " values (NULL, ?, ?, ?, ?, ?)",
						PreparedStatement.RETURN_GENERATED_KEYS
				);
				
				for (TestCase testCase : testCaseList) {
					testCase.setProblemId(problem.getProblemId());
					storeNoId(testCase, stmt, 1);
					stmt.addBatch();
				}
				
				stmt.executeBatch();
				
				ResultSet generatedKeys = getGeneratedKeys(stmt);
				int count = 0;
				while (generatedKeys.next()) {
					testCaseList.get(count).setId(generatedKeys.getInt(1));
					count++;
				}
				if (count != testCaseList.size()) {
					throw new SQLException("wrong number of generated keys for inserted test cases");
				}
				
				return true;
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "adding test case";
			}
		});
	}

	private<E> E databaseRun(DatabaseRunnable<E> databaseRunnable) {
		try {
			Connection conn = null;
			boolean committed = false;
			try {
				conn = getConnection();
				conn.setAutoCommit(false);
				// FIXME: should retry if deadlock is detected
				E result = databaseRunnable.run(conn);
				conn.commit();
				committed = true;
				return result;
			} finally {
				if (conn != null) {
					if (!committed) {
						conn.rollback();
					}
					databaseRunnable.cleanup();
					conn.setAutoCommit(true);
					releaseConnection();
				}
			}
		} catch (SQLException e) {
			throw new PersistenceException("SQLException", e);
		}
	}

	/**
	 * Store the Event objects embedded in the given IContainsEvent objects.
	 * 
	 * @param containsEventList list of IContainsEvent objects
	 * @param conn              database connection
	 * @param dbRunnable        an AbstractDatabaseRunnable that is managing statements and result sets
	 * @throws SQLException
	 */
	private void storeEvents(final IContainsEvent[] containsEventList, Connection conn, AbstractDatabaseRunnable<?> dbRunnable)
			throws SQLException {
		PreparedStatement insertEvent = dbRunnable.prepareStatement(
				conn,
				"insert into " + EVENTS + " values (NULL, ?, ?, ?, ?)", 
				Statement.RETURN_GENERATED_KEYS
		);
		for (IContainsEvent change : containsEventList) {
			storeNoId(change.getEvent(), insertEvent, 1);
			insertEvent.addBatch();
		}
		insertEvent.executeBatch();
		
		// Get the generated ids of the newly inserted Events
		ResultSet genKeys = dbRunnable.getGeneratedKeys(insertEvent);
		int count = 0;
		while (genKeys.next()) {
			int id = genKeys.getInt(1);
			containsEventList[count].getEvent().setId(id);
			containsEventList[count].setEventId(id);
			count++;
		}
		if (count != containsEventList.length) {
			throw new SQLException("Did not get all generated keys for inserted events");
		}
	}

	private void doInsertSubmissionReceipt(
			final SubmissionReceipt receipt,
			final TestResult[] testResultList_,
			Connection conn,
			AbstractDatabaseRunnable<?> dbRunnable)
			throws SQLException {
		// Get TestResults (ensuring that the array is non-null
		TestResult[] testResultList = testResultList_ != null ? testResultList_ : new TestResult[0];
		
		// Store the underlying Event
		storeEvents(new SubmissionReceipt[]{receipt}, conn, dbRunnable);
		
		// Set the SubmissionReceipt's event id to match the event we just inserted
		receipt.setEventId(receipt.getEvent().getId());
		
		// Insert the SubmissionReceipt
		PreparedStatement stmt = dbRunnable.prepareStatement(
				conn,
				"insert into " + SUBMISSION_RECEIPTS + " values (NULL, ?, ?, ?, ?, ?)",
				PreparedStatement.RETURN_GENERATED_KEYS
		);
		storeNoId(receipt, stmt, 1);
		stmt.execute();
		
		// Get the generated key for this submission receipt
		ResultSet genKey = dbRunnable.getGeneratedKeys(stmt);
		if (!genKey.next()) {
			throw new SQLException("Could not get generated key for submission receipt");
		}
		receipt.setId(genKey.getInt(1));
		
		// Store the TestResults
		for (TestResult testResult : testResultList) {
			testResult.setSubmissionReceiptId(receipt.getId());
		}
		PreparedStatement insertTestResults = dbRunnable.prepareStatement(
				conn,
				"insert into " + TEST_RESULTS + " values (NULL, ?, ?, ?, ?, ?)",
				PreparedStatement.RETURN_GENERATED_KEYS
		);
		for (TestResult testResult : testResultList) {
			storeNoId(testResult, insertTestResults, 1);
			insertTestResults.addBatch();
		}
		insertTestResults.executeBatch();
		
		// Get generated TestResult ids
		ResultSet generatedIds = dbRunnable.getGeneratedKeys(insertTestResults);
		int count = 0;
		while (generatedIds.next()) {
			testResultList[count].setId(generatedIds.getInt(1));
			count++;
		}
		if (count != testResultList.length) {
			throw new SQLException("Wrong number of generated ids for test results");
		}
	}
	
	/**
	 * Get all problems for user/course.
	 * 
	 * @param user    a User
	 * @param course  a Course
	 * @param conn    the Connection
	 * @param dbRunnable the AbstractDatabaseRunnable
	 * @return List of Problems for user/course
	 * @throws SQLException 
	 */
	protected List<Problem> doGetProblemsInCourse(User user, Course course,
			Connection conn,
			AbstractDatabaseRunnable<?> dbRunnable) throws SQLException {
		
		//
		// Note that we have to join on course registrations to ensure
		// that we return courses that the user is actually registered for.
		//
		PreparedStatement stmt = dbRunnable.prepareStatement(
				conn,
				"select p.* from " + PROBLEMS + " as p, " + COURSES + " as c, " + COURSE_REGISTRATIONS + " as r " +
				" where p.course_id = c.id " +
				"   and r.course_id = c.id " +
				"   and r.user_id = ? " +
				"   and c.id = ?"
		);
		stmt.setInt(1, user.getId());
		stmt.setInt(2, course.getId());
		
		ResultSet resultSet = dbRunnable.executeQuery(stmt);
		
		List<Problem> resultList = new ArrayList<Problem>();
		while (resultSet.next()) {
			Problem problem = new Problem();
			load(problem, resultSet, 1);
			resultList.add(problem);
		}
		
		return resultList;
	}

	private void load(ConfigurationSetting configurationSetting, ResultSet resultSet, int index) throws SQLException {
		configurationSetting.setName(resultSet.getString(index++));
		configurationSetting.setValue(resultSet.getString(index++));
	}

	private void load(User user, ResultSet resultSet, int index) throws SQLException {
		user.setId(resultSet.getInt(index++));
		user.setUserName(resultSet.getString(index++));
		user.setPasswordMD5(resultSet.getString(index++));
		user.setSalt(resultSet.getString(index++));
	}

	protected void load(Problem problem, ResultSet resultSet, int index) throws SQLException {
		problem.setProblemId(resultSet.getInt(index++));
		problem.setCourseId(resultSet.getInt(index++));
		problem.setProblemType(resultSet.getInt(index++));
		problem.setTestName(resultSet.getString(index++));
		problem.setBriefDescription(resultSet.getString(index++));
		problem.setDescription(resultSet.getString(index++));
		problem.setWhenAssigned(resultSet.getLong(index++));
		problem.setWhenDue(resultSet.getLong(index++));
		problem.setSkeleton(resultSet.getString(index++));
	}

	protected void load(Change change, ResultSet resultSet, int index) throws SQLException {
		change.setId(resultSet.getInt(index++));
		change.setEventId(resultSet.getInt(index++));
		change.setType(resultSet.getInt(index++));
		change.setStartRow(resultSet.getInt(index++));
		change.setEndRow(resultSet.getInt(index++));
		change.setStartColumn(resultSet.getInt(index++));
		change.setEndColumn(resultSet.getInt(index++));
		change.setText(resultSet.getString(index++));
	}

	protected void load(Course course, ResultSet resultSet, int index) throws SQLException {
		course.setId(resultSet.getInt(index++));
		course.setName(resultSet.getString(index++));
		course.setTitle(resultSet.getString(index++));
		course.setUrl(resultSet.getString(index++));
		course.setTermId(resultSet.getInt(index++));
		course.setYear(resultSet.getInt(index++));
	}

	protected void load(Term term, ResultSet resultSet, int index) throws SQLException {
		term.setId(resultSet.getInt(index++));
		term.setName(resultSet.getString(index++));
		term.setSeq(resultSet.getInt(index++));
	}

	protected void load(TestCase testCase, ResultSet resultSet, int index) throws SQLException {
		testCase.setId(resultSet.getInt(index++));
		testCase.setProblemId(resultSet.getInt(index++));
		testCase.setTestCaseName(resultSet.getString(index++));
		testCase.setInput(resultSet.getString(index++));
		testCase.setOutput(resultSet.getString(index++));
		testCase.setSecret(resultSet.getBoolean(index++));
	}
	
	protected void load(SubmissionReceipt submissionReceipt, ResultSet resultSet, int index) throws SQLException {
		submissionReceipt.setId(resultSet.getInt(index++));
		submissionReceipt.setEventId(resultSet.getInt(index++));
		submissionReceipt.setLastEditEventId(resultSet.getInt(index++));
		submissionReceipt.setStatus(resultSet.getInt(index++));
		submissionReceipt.setNumTestsAttempted(submissionReceipt.getNumTestsAttempted());
		submissionReceipt.setNumTestsPassed(submissionReceipt.getNumTestsPassed());
	}
	
	protected void load(Event event, ResultSet resultSet, int index) throws SQLException {
		event.setId(resultSet.getInt(index++));
		event.setUserId(resultSet.getInt(index++));
		event.setProblemId(resultSet.getInt(index++));
		event.setType(resultSet.getInt(index++));
		event.setTimestamp(resultSet.getLong(index++));
	}
	
	protected void load(CourseRegistration reg, ResultSet resultSet, int index) throws SQLException {
		reg.setId(resultSet.getInt(index++));
		reg.setCourseId(resultSet.getInt(index++));
		reg.setUserId(resultSet.getInt(index++));
		reg.setRegistrationType(resultSet.getInt(index++));
		reg.setSection(resultSet.getInt(index++));
	}

	protected void storeNoId(Event event, PreparedStatement stmt, int index) throws SQLException {
		stmt.setInt(index++, event.getUserId());
		stmt.setInt(index++, event.getProblemId());
		stmt.setInt(index++, event.getType());
		stmt.setLong(index++, event.getTimestamp());
	}

	protected void storeNoId(Change change, PreparedStatement stmt, int index) throws SQLException {
		stmt.setInt(index++, change.getEventId());
		stmt.setInt(index++, change.getType().ordinal());
		stmt.setInt(index++, change.getStartRow());
		stmt.setInt(index++, change.getEndRow());
		stmt.setInt(index++, change.getStartColumn());
		stmt.setInt(index++, change.getEndColumn());
		stmt.setString(index++, change.getText());
	}

	protected void storeNoId(SubmissionReceipt receipt, PreparedStatement stmt, int index) throws SQLException {
		stmt.setInt(index++, receipt.getEventId());
		stmt.setLong(index++, receipt.getLastEditEventId());
		stmt.setInt(index++, receipt.getStatus().ordinal());
		stmt.setInt(index++, receipt.getNumTestsAttempted());
		stmt.setInt(index++, receipt.getNumTestsPassed());
	}

	protected void storeNoId(TestResult testResult, PreparedStatement stmt, int index) throws SQLException {
		stmt.setInt(index++, testResult.getSubmissionReceiptId());
		stmt.setInt(index++, testResult.getOutcome().ordinal());
		stmt.setString(index++, testResult.getMessage());
		stmt.setString(index++, testResult.getStdout());
		stmt.setString(index++, testResult.getStderr());
	}
	
	protected void storeNoId(Problem problem, PreparedStatement stmt, int index) throws SQLException {
		stmt.setInt(index++, problem.getCourseId());
		stmt.setInt(index++, problem.getProblemType().ordinal());
		stmt.setString(index++, problem.getTestName());
		stmt.setString(index++, problem.getBriefDescription());
		stmt.setString(index++, problem.getDescription());
		stmt.setLong(index++, problem.getWhenAssigned());
		stmt.setLong(index++, problem.getWhenDue());
		stmt.setString(index++, problem.getSkeleton());
	}

	protected void storeNoId(TestCase testCase, PreparedStatement stmt, int index) throws SQLException {
		stmt.setInt(index++, testCase.getProblemId());
		stmt.setString(index++, testCase.getTestCaseName());
		stmt.setString(index++, testCase.getInput());
		stmt.setString(index++, testCase.getOutput());
		stmt.setBoolean(index++, testCase.isSecret());
	}
}
