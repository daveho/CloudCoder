// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudcoder.app.server.persist.txn.AddUserToCourse;
import org.cloudcoder.app.server.persist.txn.AuthenticateUser;
import org.cloudcoder.app.server.persist.txn.EditUser;
import org.cloudcoder.app.server.persist.txn.EditUserGivenUserData;
import org.cloudcoder.app.server.persist.txn.GetAllChangesNewerThan;
import org.cloudcoder.app.server.persist.txn.GetChangeGivenChangeEventId;
import org.cloudcoder.app.server.persist.txn.GetConfigurationSetting;
import org.cloudcoder.app.server.persist.txn.GetCoursesForUser;
import org.cloudcoder.app.server.persist.txn.GetMostRecentChangeForUserAndProblem;
import org.cloudcoder.app.server.persist.txn.GetMostRecentFullTextChange;
import org.cloudcoder.app.server.persist.txn.GetOrAddLatestSubmissionReceipt;
import org.cloudcoder.app.server.persist.txn.GetProblemAndSubscriptionReceiptsForUserInCourse;
import org.cloudcoder.app.server.persist.txn.GetProblemForProblemId;
import org.cloudcoder.app.server.persist.txn.GetProblemForUser;
import org.cloudcoder.app.server.persist.txn.GetProblemsInCourse;
import org.cloudcoder.app.server.persist.txn.GetTestCasesForProblem;
import org.cloudcoder.app.server.persist.txn.GetTestCasesForProblemCheckAuth;
import org.cloudcoder.app.server.persist.txn.GetUserGivenId;
import org.cloudcoder.app.server.persist.txn.GetUserWithoutAuthentication;
import org.cloudcoder.app.server.persist.txn.GetUsersInCourse;
import org.cloudcoder.app.server.persist.txn.InsertUsersFromInputStream;
import org.cloudcoder.app.server.persist.txn.Queries;
import org.cloudcoder.app.server.persist.txn.StoreChanges;
import org.cloudcoder.app.server.persist.txn.StoreSubmissionReceipt;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.server.persist.util.DatabaseRunnable;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.EditedUser;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.IModelObject;
import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemAuthorship;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.ProblemSummary;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.RepoProblemSearchCriteria;
import org.cloudcoder.app.shared.model.RepoProblemSearchResult;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.RepoTestCase;
import org.cloudcoder.app.shared.model.StartedQuiz;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.UserAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.UserRegistrationRequest;
import org.cloudcoder.app.shared.model.UserRegistrationRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of IDatabase using JDBC.
 * 
 * @author David Hovemeyer
 */
public class JDBCDatabase implements IDatabase {
	static final Logger logger=LoggerFactory.getLogger(JDBCDatabase.class);

	private String jdbcUrl;
	
	public JDBCDatabase() {
		JDBCDatabaseConfig.ConfigProperties config = JDBCDatabaseConfig.getInstance().getConfigProperties();
		jdbcUrl = "jdbc:mysql://" +
				config.getHost() + config.getPortStr() +
				"/" +
				config.getDatabaseName() +
				"?user=" +
				config.getUser() +
				"&password=" + config.getPasswd();
		logger.info("Database URL: "+jdbcUrl);
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
		return databaseRun(new GetConfigurationSetting(name));
	}
	
	public User getUserGivenId(final int userId) {
		return databaseRun(new GetUserGivenId(userId));
	}
	
	@Override
	public User authenticateUser(final String userName, final String password) {
		return databaseRun(new AuthenticateUser(userName, password));
	}
	
	@Override
    public List<User> getUsersInCourse(final int courseId, final int sectionNumber) {
		return databaseRun(new GetUsersInCourse(sectionNumber, courseId));
	}

	@Override
	public User getUserWithoutAuthentication(final String userName) {
		return databaseRun(new GetUserWithoutAuthentication(userName));
	}
	
	@Override
	public Pair<Problem, Quiz> getProblem(final User user, final int problemId) {
		return databaseRun(new GetProblemForUser(problemId, user));
	}

	@Override
	public Problem getProblem(final int problemId) {
		return databaseRun(new GetProblemForProblemId(problemId));
	}
	
	@Override
	public Change getMostRecentChange(final User user, final int problemId) {
		return databaseRun(new GetMostRecentChangeForUserAndProblem(problemId, user));
	}
	
	@Override
	public Change getMostRecentFullTextChange(final User user, final int problemId) {
		return databaseRun(new GetMostRecentFullTextChange(problemId, user));
	}
	
	@Override
	public Change getChange(final int changeEventId) {
		return databaseRun(new GetChangeGivenChangeEventId(changeEventId));
	}
	
	@Override
	public List<Change> getAllChangesNewerThan(final User user, final int problemId, final int baseRev) {
		return databaseRun(new GetAllChangesNewerThan(problemId, user, baseRev));
	}
	
	@Override
	public List<? extends Object[]> getCoursesForUser(final User user) {
		return databaseRun(new GetCoursesForUser(user));
	}

	@Override
	public ProblemList getProblemsInCourse(final User user, final Course course) {
		return databaseRun(new GetProblemsInCourse(course, user));
	}

	@Override
	public List<ProblemAndSubmissionReceipt> getProblemAndSubscriptionReceiptsInCourse(
			final User requestingUser, final Course course, final User forUser, final Module module) {
		return databaseRun(new GetProblemAndSubscriptionReceiptsForUserInCourse(course, forUser,
				module, requestingUser));
	}
	
	@Override
	public void storeChanges(final Change[] changeList) {
		databaseRun(new StoreChanges(changeList));
	}
	
	@Override
	public List<TestCase> getTestCasesForProblem(final int problemId) {
		return databaseRun(new GetTestCasesForProblem(problemId));
	}

	@Override
	public TestCase[] getTestCasesForProblem(final User authenticatedUser, final boolean requireInstructor, final int problemId) {
		return databaseRun(new GetTestCasesForProblemCheckAuth(authenticatedUser,
				requireInstructor, problemId));
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#insertSubmissionReceipt(org.cloudcoder.app.shared.model.SubmissionReceipt)
	 */
	@Override
	public void insertSubmissionReceipt(final SubmissionReceipt receipt, final TestResult[] testResultList_) {
		databaseRun(new StoreSubmissionReceipt(testResultList_, receipt));
	}
	
	public void insertUsersFromInputStream(final InputStream in, final Course course) {
		try {
			databaseRunAuth(new InsertUsersFromInputStream(in, course));
		} catch (CloudCoderAuthenticationException e) {
			// TODO proper error handling
			throw new RuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#addSubmissionReceiptIfNecessary(org.cloudcoder.app.shared.model.User, org.cloudcoder.app.shared.model.Problem)
	 */
	@Override
	public void getOrAddLatestSubmissionReceipt(final User user, final Problem problem) {
		databaseRun(new GetOrAddLatestSubmissionReceipt(user, problem));
	}
	
	@Override
	public void editUser(final User user)
	{
	    databaseRun(new EditUser(user));
	}
	
	@Override
    public void editUser(final int userId, final String username, final String firstname, 
        final String lastname, final String email, final String passwd)
    {
	    databaseRun(new EditUserGivenUserData(username, lastname, email, passwd, userId,
				firstname));    
    }

    @Override
    public void editRegistrationType(int userId, int courseId,
        CourseRegistrationType type)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Implement this method");
    }

    @Override
	public void addUserToCourse(final User authenticatedUser, final int courseId, final EditedUser editedUser) throws CloudCoderAuthenticationException {
    	databaseRunAuth(new AddUserToCourse(courseId, authenticatedUser, editedUser));
    }

    @Override
	public void addProblem(final Problem problem) {
		databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public Boolean run(Connection conn) throws SQLException {
				return doInsertProblem(problem, conn, (AbstractDatabaseRunnableNoAuthException<?>) this);
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
	
	@Override
	public void addTestCases(final Problem problem, final List<TestCase> testCaseList) {
		databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public Boolean run(Connection conn) throws SQLException {
				AbstractDatabaseRunnableNoAuthException<?> databaseRunnable = this;
				
				return doInsertTestCases(problem, testCaseList, conn,
						databaseRunnable);
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
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#createProblemSummary(org.cloudcoder.app.shared.model.Problem)
	 */
	@Override
	public ProblemSummary createProblemSummary(final Problem problem) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<ProblemSummary>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public ProblemSummary run(Connection conn) throws SQLException {
				// Determine how many students (non-instructor users) are in this course
				int numStudentsInCourse = doCountStudentsInCourse(problem, conn, this);
				
				// Get all SubmissionReceipts
				PreparedStatement stmt = prepareStatement(
						conn,
						"select sr.*, e.* " +
						"  from " + SubmissionReceipt.SCHEMA.getDbTableName() + " as sr, " + Event.SCHEMA.getDbTableName() + " as e " +
						" where sr.event_id = e.id " +
						"   and e.problem_id = ?");
				stmt.setInt(1, problem.getProblemId());
				
				// Keep track of "best" submissions from each student.
				HashMap<Integer, SubmissionReceipt> bestSubmissions = new HashMap<Integer, SubmissionReceipt>();
				
				ResultSet resultSet = executeQuery(stmt);
				while (resultSet.next()) {
					SubmissionReceipt receipt = Queries.loadSubmissionReceiptAndEvent(resultSet);
					
					SubmissionReceipt prevBest = bestSubmissions.get(receipt.getEvent().getUserId());
					SubmissionStatus curStatus = receipt.getStatus();
					//SubmissionStatus prevStatus = prevBest.getStatus();
					if (prevBest == null
							|| curStatus == SubmissionStatus.TESTS_PASSED && prevBest.getStatus() != SubmissionStatus.TESTS_PASSED
							|| receipt.getNumTestsPassed() > prevBest.getNumTestsPassed()) {
						// New receipt is better than the previous receipt
						bestSubmissions.put(receipt.getEvent().getUserId(), receipt);
					}
				}
				
				// Aggregate the data
				int started = 0;
				int anyPassed = 0;
				int allPassed = 0;
				for (SubmissionReceipt r : bestSubmissions.values()) {
					if (r.getStatus() == SubmissionStatus.TESTS_PASSED) {
						started++;
						allPassed++;
						anyPassed++;
					} else if (r.getNumTestsPassed() > 0) {
						started++;
						anyPassed++;
					} else {
						started++;
					}
				}
				
				// Create the ProblemSummary
				ProblemSummary problemSummary = new ProblemSummary();
				problemSummary.setProblem(problem);
				problemSummary.setNumStudents(numStudentsInCourse);
				problemSummary.setNumStarted(started);
				problemSummary.setNumPassedAtLeastOneTest(anyPassed);
				problemSummary.setNumCompleted(allPassed);
				
				return problemSummary;
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "get problem summary for problem";
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#getSubmissionReceipt(int)
	 */
	@Override
	public SubmissionReceipt getSubmissionReceipt(final int submissionReceiptId) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<SubmissionReceipt>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public SubmissionReceipt run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select sr.*, e.* " +
						"  from " + SubmissionReceipt.SCHEMA.getDbTableName() + " as sr, " +
						"       " + Event.SCHEMA.getDbTableName() + " as e " +
						" where sr.event_id = e.id " +
						"   and e.event_id = ?");
				stmt.setInt(1, submissionReceiptId);
				
				ResultSet resultSet = executeQuery(stmt);
				
				if (resultSet.next()) {
					SubmissionReceipt receipt = Queries.loadSubmissionReceiptAndEvent(resultSet);
					return receipt;
				}
				return null;
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "getting submission receipt";
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#insertTestResults(org.cloudcoder.app.shared.model.TestResult[], int)
	 */
	@Override
	public void replaceTestResults(final TestResult[] testResults, final int submissionReceiptId) {
		databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public Boolean run(Connection conn) throws SQLException {
				// Delete old test results (if any)
				PreparedStatement delTestResults = prepareStatement(
						conn,
						"delete from " + TestResult.SCHEMA.getDbTableName() + " where submission_receipt_event_id = ?");
				delTestResults.setInt(1, submissionReceiptId);
				delTestResults.executeUpdate();
				
				// Insert new test results
				Queries.doInsertTestResults(testResults, submissionReceiptId, conn, this);
				
				return true;
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "store test results";
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.server.persist.IDatabase#updateSubmissionReceipt(org.cloudcoder.app.shared.model.SubmissionReceipt)
	 */
	@Override
	public void updateSubmissionReceipt(final SubmissionReceipt receipt) {
		databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#run(java.sql.Connection)
			 */
			@Override
			public Boolean run(Connection conn) throws SQLException {
				// Only update the fields that we expect might have changed
				// following a retest.
				PreparedStatement stmt = prepareStatement(
						conn,
						"update " + SubmissionReceipt.SCHEMA.getDbTableName() + 
						"  set status = ?, num_tests_attempted = ?, num_tests_passed = ?");
				stmt.setInt(1, receipt.getStatus().ordinal());
				stmt.setInt(2, receipt.getNumTestsAttempted());
				stmt.setInt(3, receipt.getNumTestsPassed());
				
				stmt.executeUpdate();
				
				return true;
			}
			/* (non-Javadoc)
			 * @see org.cloudcoder.app.server.persist.DatabaseRunnable#getDescription()
			 */
			@Override
			public String getDescription() {
				return "update submission receipt";
			}
		});
	}
	
	@Override
	public ProblemAndTestCaseList storeProblemAndTestCaseList(
			final ProblemAndTestCaseList problemAndTestCaseList, final Course course, final User user)
			throws CloudCoderAuthenticationException {
		return databaseRunAuth(new AbstractDatabaseRunnable<ProblemAndTestCaseList>() {
			@Override
			public ProblemAndTestCaseList run(Connection conn)
					throws SQLException, CloudCoderAuthenticationException {
				// Ensure problem and course id match.
				if (!problemAndTestCaseList.getProblem().getCourseId().equals((Integer) course.getId())) {
					throw new CloudCoderAuthenticationException("Problem does not match course");
				}
				
				// Check that user is registered as an instructor in the course.
				boolean isInstructor = false;
				List<? extends Object[]> courses = Queries.doGetCoursesForUser(user, conn, this);
				for (Object[] tuple : courses) {
					CourseRegistration courseReg = (CourseRegistration) tuple[2];
					if (courseReg.getCourseId() == course.getId()
							&& courseReg.getRegistrationType().ordinal() >= CourseRegistrationType.INSTRUCTOR.ordinal()) {
							isInstructor = true;
						break;
					}
				}
				if (!isInstructor) {
					throw new CloudCoderAuthenticationException("not instructor in course");
				}
				
				// If the problem id is not set, then insert the problem.
				// Otherwise, update the existing problem.
				if (problemAndTestCaseList.getProblem().getProblemId() == null
						|| problemAndTestCaseList.getProblem().getProblemId() < 0) {
					// Insert problem and test cases
					doInsertProblem(problemAndTestCaseList.getProblem(), conn, this);
					doInsertTestCases(
							problemAndTestCaseList.getProblem(),
							problemAndTestCaseList.getTestCaseData(),
							conn,
							this);
				} else {
					// Update problem and test cases
					doUpdateProblem(problemAndTestCaseList.getProblem(), conn, this);
					
					// We can achieve the effect of updating the test cases by deleting
					// and then reinserting
					doDeleteTestCases(problemAndTestCaseList.getProblem().getProblemId(), conn, this);
					doInsertTestCases(
							problemAndTestCaseList.getProblem(),
							problemAndTestCaseList.getTestCaseData(),
							conn,
							this);
				}
				
				// Success!
				return problemAndTestCaseList;
			}

			@Override
			public String getDescription() {
				return " storing problem and test cases";
			}
		});
	}
	
	@Override
	public RepoProblemAndTestCaseList getRepoProblemAndTestCaseList(final String hash) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<RepoProblemAndTestCaseList>() {
			@Override
			public RepoProblemAndTestCaseList run(Connection conn) throws SQLException {
				// Query to find the RepoProblem
				PreparedStatement findRepoProblem = prepareStatement(
						conn,
						"select * from " + RepoProblem.SCHEMA.getDbTableName() + " as rp " +
						" where rp." + RepoProblem.HASH.getName() + " = ?");
				findRepoProblem.setString(1, hash);
				
				ResultSet repoProblemRs = executeQuery(findRepoProblem);
				if (!repoProblemRs.next()) {
					return null;
				}
				
				RepoProblem repoProblem = new RepoProblem();
				load(repoProblem, repoProblemRs, 1);
				
				RepoProblemAndTestCaseList result = new RepoProblemAndTestCaseList();
				result.setProblem(repoProblem);
				
				// Find all RepoTestCases associated with the RepoProblem
				doFindRepoTestCases(repoProblem, result, conn, this);
				
				return result;
			}

			@Override
			public String getDescription() {
				return " retrieving problem and test cases from the repository";
			}
		});
	}

	@Override
	public void storeRepoProblemAndTestCaseList(final RepoProblemAndTestCaseList exercise, final User user) {
		databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			@Override
			public Boolean run(Connection conn) throws SQLException {
				// Compute hash
				exercise.computeHash();
				
				// Set user id
				exercise.getProblem().setUserId(user.getId());

				// Store the RepoProblem
				DBUtil.storeModelObject(conn, exercise.getProblem());
				
				// Insert RepoTestCases (setting repo problem id of each)
				String insertRepoTestCaseSql = DBUtil.createInsertStatement(RepoTestCase.SCHEMA);
				PreparedStatement stmt = prepareStatement(conn, insertRepoTestCaseSql, PreparedStatement.RETURN_GENERATED_KEYS);
				for (RepoTestCase repoTestCase : exercise.getTestCaseData()) {
					repoTestCase.setRepoProblemId(exercise.getProblem().getId());
					DBUtil.bindModelObjectValuesForInsert(repoTestCase, RepoTestCase.SCHEMA, stmt);
					stmt.addBatch();
				}
				stmt.executeBatch();

				// Get generated unique ids of RepoTestCase objects
				ResultSet genKeys = getGeneratedKeys(stmt);
				DBUtil.getModelObjectUniqueIds(exercise.getTestCaseData(), RepoTestCase.SCHEMA, genKeys);
				
				// Add a tag indicating the programming language
				Language language = exercise.getProblem().getProblemType().getLanguage();
				RepoProblemTag tag = new RepoProblemTag();
				tag.setName(language.getTagName());
				tag.setRepoProblemId(exercise.getProblem().getId());
				tag.setUserId(user.getId());
				doAddRepoProblemTag(conn, tag, this);
				
				return true;
			}
			@Override
			public String getDescription() {
				return " storing exercise in repository database";
			}
		});
	}
	
	@Override
	public List<RepoProblemSearchResult> searchRepositoryExercises(final RepoProblemSearchCriteria searchCriteria) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<List<RepoProblemSearchResult>>() {
			@Override
			public List<RepoProblemSearchResult> run(Connection conn) throws SQLException {
				RepoProblemSearch search = new RepoProblemSearch();
				search.setSearchCriteria(searchCriteria);
				search.execute(conn, this);
				return search.getSearchResultList();
			}
			@Override
			public String getDescription() {
				return " searching for exercises in the exercise repository";
			}
		});
	}
	
	@Override
	public CourseRegistrationList findCourseRegistrations(final User user, final Course course) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<CourseRegistrationList>() {
			@Override
			public CourseRegistrationList run(Connection conn) throws SQLException {
				int userId = user.getId();
				int courseId = course.getId();
				return Queries.doGetCourseRegistrations(conn, courseId, userId, this);
			}
			@Override
			public String getDescription() {
				return " finding course registration for user/course";
			}
		});
	}
	
	@Override
	public CourseRegistrationList findCourseRegistrations(final User user, final int courseId) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<CourseRegistrationList>() {
			@Override
			public CourseRegistrationList run(Connection conn) throws SQLException {
				int userId = user.getId();
				return Queries.doGetCourseRegistrations(conn, courseId, userId, this);
			}
			@Override
			public String getDescription() {
				return " finding course registration for user/course";
			}
		});
	}
	
	@Override
	public List<UserAndSubmissionReceipt> getBestSubmissionReceipts(
			final Course unused, final int section, final Problem problem) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<List<UserAndSubmissionReceipt>>() {
			@Override
			public List<UserAndSubmissionReceipt> run(Connection conn)
					throws SQLException {
				return doGetBestSubmissionReceipts(conn, problem, section, this);
			}
			@Override
			public String getDescription() {
				return " getting best submission receipts for problem/course";
			}
		});
	}

	@Override
	public List<UserAndSubmissionReceipt> getBestSubmissionReceipts(final Problem problem, final int section, final User authenticatedUser) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<List<UserAndSubmissionReceipt>>() {
			@Override
			public List<UserAndSubmissionReceipt> run(Connection conn) throws SQLException {
				CourseRegistrationList regList = Queries.doGetCourseRegistrations(conn, problem.getCourseId(), authenticatedUser.getId(), this);
				if (!regList.isInstructor()) {
					// user is not an instructor
					return new ArrayList<UserAndSubmissionReceipt>();
				}

				return doGetBestSubmissionReceipts(conn, problem, section, this);
			}
			@Override
			public String getDescription() {
				return " getting best submission receipts for problem";
			}
		});
	}
	
	@Override
	public boolean deleteProblem(final User user, final Course course, final Problem problem)
			throws CloudCoderAuthenticationException {
		return databaseRunAuth(new AbstractDatabaseRunnable<Boolean>() {
			@Override
			public Boolean run(Connection conn) throws SQLException, CloudCoderAuthenticationException {
				// verify that the user is an instructor in the course
				CourseRegistrationList courseReg = Queries.doGetCourseRegistrations(conn, course.getId(), user.getId(), this);
				if (!courseReg.isInstructor()) {
					throw new CloudCoderAuthenticationException("Only instructor can delete a problem");
				}
				
				// Delete the problem
				// Note that we do NOT delete the problem from the database.
				// Instead, we just set the deleted flag to true, which prevents the
				// problem from coming up in future searches.  Because lots
				// of information is linked to a problem, and serious database
				// corruption could occur if a problem id were reused, this
				// is a much safer approach than physical deletion.
				PreparedStatement stmt = prepareStatement(
						conn,
						"update " + Problem.SCHEMA.getDbTableName() +
						"   set " + Problem.DELETED.getName() + " = 1 " +
						" where " + Problem.PROBLEM_ID.getName() + " = ?");
				stmt.setInt(1, problem.getProblemId());
				
				stmt.executeUpdate();
				
				return true;
			}
			@Override
			public String getDescription() {
				return " deleting problem";
			}
		});
	}
	
	/**
	 * Check whether at least one of the {@link CourseRegistration} objects
	 * is an instructor registration.
	 * 
	 * @param courseReg list of {@link CourseRegistration} objects
	 * @return true if at least one {@link CourseRegistration} is an instructor
	 *         registration
	 */
	protected boolean isInstructor(List<CourseRegistration> courseReg) {
		for (CourseRegistration reg : courseReg) {
			if (reg.getRegistrationType().ordinal() >= CourseRegistrationType.INSTRUCTOR.ordinal()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public OperationResult addUserRegistrationRequest(final UserRegistrationRequest request) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<OperationResult>() {
			@Override
			public OperationResult run(Connection conn) throws SQLException {
				// Make sure that username is not already taken
				User existing = Queries.getUser(conn, request.getUsername(), this);
				if (existing != null) {
					return new OperationResult(false, "Username " + request.getUsername() + " is already in use");
				}
				
				// Insert the request
				DBUtil.storeModelObject(conn, request, UserRegistrationRequest.SCHEMA);
				
				return new OperationResult(true, "Successfully added registration request to database");
			}
			@Override
			public String getDescription() {
				return " adding user registration request";
			}
		});
	}
	
	@Override
	public UserRegistrationRequest findUserRegistrationRequest(final String secret) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<UserRegistrationRequest>() {
			@Override
			public UserRegistrationRequest run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select * from " + UserRegistrationRequest.SCHEMA.getDbTableName() + " as urr " +
						" where urr." + UserRegistrationRequest.SECRET.getName() + " = ?"
				);
				stmt.setString(1, secret);
				
				ResultSet resultSet = executeQuery(stmt);
				if (!resultSet.next()) {
					return null;
				}
				UserRegistrationRequest request = new UserRegistrationRequest();
				Queries.loadGeneric(request, resultSet, 1, UserRegistrationRequest.SCHEMA);
				return request;
			}
			@Override
			public String getDescription() {
				return " finding user registration request";
			}
		});
	}
	
	@Override
	public OperationResult completeRegistration(final UserRegistrationRequest request) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<OperationResult>() {
			@Override
			public OperationResult run(Connection conn) throws SQLException {
				// Copy information from request into a User object
				User user = new User();
				for (ModelObjectField<? super User, ?> field : User.SCHEMA.getFieldList()) {
					Object val = field.get(request);
					field.setUntyped(user, val);
				}
				
				// Attempt to insert the User
				try {
					DBUtil.storeModelObject(conn, user);
				} catch (SQLException e) {
					// Check to see if it was a duplicate key error, which would mean
					// an account with the same username or password has already been
					// created.
					if (e.getSQLState().equals("23000")) {
						throw new SQLException("A user account with the same username or email address has already been created.");
					} else {
						throw e;
					}
				}
				
				// Successfully added User - now set request status to CONFIRMED
				request.setStatus(UserRegistrationRequestStatus.CONFIRMED);
				DBUtil.updateModelObject(conn, request, UserRegistrationRequest.SCHEMA);
				
				// Success!
				return new OperationResult(true, "User account " + user.getUsername() + " created successfully!");
			}
			@Override
			public String getDescription() {
				return " completing user registration request";
			}
		});
	}
	
	@Override
	public List<RepoProblemTag> getProblemTags(final int repoProblemId) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<List<RepoProblemTag>>() {
			@Override
			public List<RepoProblemTag> run(Connection conn) throws SQLException {
				// Order the tags by decreasing order of popularity
				// and (secondarily) ascending name order.
				// Return at most 8 tags.
				PreparedStatement stmt = prepareStatement(
						conn,
						"select rpt.*, count(rpt." + RepoProblemTag.NAME.getName() + ") as count " +
						"  from " + RepoProblemTag.SCHEMA.getDbTableName() + " as rpt " +
						" where rpt." + RepoProblemTag.REPO_PROBLEM_ID.getName() + " = ? " +
						" group by rpt." + RepoProblemTag.NAME.getName() + " " +
						" order by count desc, rpt." + RepoProblemTag.NAME.getName() + " asc " +
						" limit 8"
						);
				stmt.setInt(1, repoProblemId);
				
				ResultSet resultSet = executeQuery(stmt);
				List<RepoProblemTag> result = new ArrayList<RepoProblemTag>();
				while (resultSet.next()) {
					RepoProblemTag tag = new RepoProblemTag();
					
					Queries.loadGeneric(tag, resultSet, 1, RepoProblemTag.SCHEMA);
					
					// Because these tags are aggregated from (potentially) multiple
					// records in the table, we set the user id to 0, so there is no
					// confusion over whether the tag is linked to a specific user.
					tag.setUserId(0);
					
					// Set the count (number of users who added this tag to this problem)
					tag.setCount(resultSet.getInt(RepoProblemTag.SCHEMA.getNumFields() + 1));
					
					result.add(tag);
				}
				
				return result;
			}
			@Override
			public String getDescription() {
				return " getting tags for repository exercise"; 
			}
		});
	}
	
	@Override
	public boolean addRepoProblemTag(final RepoProblemTag repoProblemTag) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			@Override
			public Boolean run(Connection conn) throws SQLException {
				return doAddRepoProblemTag(conn, repoProblemTag, this);
			}
			
			@Override
			public String getDescription() {
				return " adding tag to repository exercise";
			}
		});
	}
	
	@Override
	public List<String> suggestTagNames(final String term) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<List<String>>() {
			@Override
			public List<String> run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select distinct " + RepoProblemTag.NAME.getName() +
						"  from " + RepoProblemTag.SCHEMA.getDbTableName() +
						" where "+ RepoProblemTag.NAME.getName() + " like ? " +
						" order by "+ RepoProblemTag.NAME.getName() + " asc"
				);
				stmt.setString(1, term + "%");
				
				List<String> result = new ArrayList<String>();
				ResultSet resultSet = executeQuery(stmt);
				while (resultSet.next()) {
					result.add(resultSet.getString(1));
				}
				
				return result;
			}
			@Override
			public String getDescription() {
				return " suggesting tag names";
			}
		});
	}
	
	@Override
	public Quiz startQuiz(final User user, final Problem problem, final int section) throws CloudCoderAuthenticationException {
		return databaseRunAuth(new AbstractDatabaseRunnable<Quiz>() {
			@Override
			public Quiz run(Connection conn) throws SQLException, CloudCoderAuthenticationException {
				// Find the user's course registration in the course/section
				PreparedStatement findReg = prepareStatement(
						conn,
						"select cr.* from cc_course_registrations as cr " +
						" where cr.user_id = ? " +
						"   and cr.course_id = ? " +
						"   and cr.section = ? " +
						"   and cr.registration_type >= ?"
				);
				findReg.setInt(1, user.getId());
				findReg.setInt(2, problem.getCourseId());
				findReg.setInt(3, section);
				findReg.setInt(4, CourseRegistrationType.INSTRUCTOR.ordinal());
				
				ResultSet resultSet = executeQuery(findReg);
				
				if (!resultSet.next()) {
					throw new CloudCoderAuthenticationException("User is not an instructor in given course/section");
				}
				
				// Delete previous quiz for this problem/section (if any)
				PreparedStatement delOldQuiz = prepareStatement(
						conn,
						"delete from cc_quizzes where problem_id = ? and section = ?"
				);
				delOldQuiz.setInt(1, problem.getProblemId());
				delOldQuiz.setInt(2, section);
				delOldQuiz.executeUpdate();
				
				// Create the quiz record
				Quiz quiz = new Quiz();
				quiz.setProblemId(problem.getProblemId());
				quiz.setCourseId(problem.getCourseId());
				quiz.setSection(section);
				quiz.setStartTime(System.currentTimeMillis());
				quiz.setEndTime(0L);
				PreparedStatement insertQuiz = prepareStatement(
						conn,
						"insert into cc_quizzes values (" +
						DBUtil.getInsertPlaceholdersNoId(Quiz.SCHEMA) +
						")",
						PreparedStatement.RETURN_GENERATED_KEYS
				);
				DBUtil.bindModelObjectValuesForInsert(quiz, Quiz.SCHEMA, insertQuiz);
				
				insertQuiz.executeUpdate();
				
				ResultSet generatedKey = getGeneratedKeys(insertQuiz);
				if (!generatedKey.next()) {
					throw new SQLException("Could not get generated key for inserted Quiz");
				}
				quiz.setId(generatedKey.getInt(1));
				
				return quiz;
			}
			@Override
			public String getDescription() {
				return " starting quiz";
			}
		});
	}
	
	@Override
	public Quiz findCurrentQuiz(final User user, final Problem problem) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<Quiz>() {
			@Override
			public Quiz run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select q.* from cc_quizzes as q, cc_course_registrations as cr " +
						" where cr.user_id = ? " +
						"   and cr.course_id = ? " +
						"   and cr.registration_type >= ? " +
						"   and q.course_id = cr.course_id " +
						"   and q.section = cr.section " +
						"   and q.problem_id = ? " +
						"   and q.start_time <= ? " +
						"   and (q.end_time >= ? or q.end_time = 0)"
				);
				stmt.setInt(1, user.getId());
				stmt.setInt(2, problem.getCourseId());
				stmt.setInt(3, CourseRegistrationType.INSTRUCTOR.ordinal());
				stmt.setInt(4, problem.getProblemId());
				long currentTime = System.currentTimeMillis();
				stmt.setLong(5, currentTime);
				stmt.setLong(6, currentTime);
				
				ResultSet resultSet = executeQuery(stmt);
				if (!resultSet.next()) {
					return null;
				}
				
				Quiz quiz = new Quiz();
				DBUtil.loadModelObjectFields(quiz, Quiz.SCHEMA, resultSet);
				return quiz;
			}
			@Override
			public String getDescription() {
				return " finding current quiz for problem";
			}
		});
	}
	
	@Override
	public Boolean endQuiz(final User user, final Quiz quiz) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			@Override
			public Boolean run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"update cc_quizzes as q" +
						"  join cc_course_registrations as cr on  cr.course_id = q.course_id" +
						"                                     and cr.section = q.section" +
						"                                     and cr.user_id = ?" +
						"                                     and q.problem_id = ?" +
						"                                     and q.section = ?" +
						"                                     and q.course_id = ?" +
						" set end_time = ?"
				);
				stmt.setInt(1, user.getId());
				stmt.setInt(2, quiz.getProblemId());
				stmt.setInt(3, quiz.getSection());
				stmt.setInt(4, quiz.getCourseId());
				long currentTime = System.currentTimeMillis();
				stmt.setLong(5, currentTime);
				
				int updateCount = stmt.executeUpdate();
				return updateCount > 0;
			}
			@Override
			public String getDescription() {
				return " ending quiz";
			}
		});
	}
	
	@Override
	public <E extends IModelObject<E>> boolean reloadModelObject(final E obj) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
			@Override
			public Boolean run(Connection conn) throws SQLException {
				ModelObjectField<? super E, ?> idField = obj.getSchema().getUniqueIdField();
				PreparedStatement stmt = prepareStatement(
						conn,
						"select * from " + obj.getSchema().getDbTableName() + " where " + idField.getName() + " = ?"
				);
				stmt.setObject(1, idField.get(obj));
				
				ResultSet resultSet = executeQuery(stmt);
				if (!resultSet.next()) {
					return false;
				}
				
				DBUtil.loadModelObjectFields(obj, obj.getSchema(), resultSet);
				return true;
			}
			@Override
			public String getDescription() {
				return " reloading model object";
			}
		});
	}
	
	@Override
	public Module[] getModulesForCourse(final User user, final Course course) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<Module[]>() {
			@Override
			public Module[] run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select m.* from cc_modules as m " +
						" where m.id in " +
						"   (select p.module_id from cc_problems as p, cc_course_registrations as cr " +
						"     where p.course_id = cr.course_id " +
						"       and cr.course_id = ? " +
						"       and cr.user_id = ?) " +
						" order by m.name"
				);
				stmt.setInt(1, course.getId());
				stmt.setInt(2, user.getId());
				
				ResultSet resultSet = executeQuery(stmt);
				List<Module> result = new ArrayList<Module>();
				while (resultSet.next()) {
					Module module = new Module();
					DBUtil.loadModelObjectFields(module, Module.SCHEMA, resultSet);
					result.add(module);
				}
				
				return result.toArray(new Module[result.size()]);
			}
			@Override
			public String getDescription() {
				return " getting modules in course";
			}
		});
	}
	
	@Override
	public Module setModule(final User user, final Problem problem, final String moduleName) throws CloudCoderAuthenticationException {
		return databaseRunAuth(new AbstractDatabaseRunnable<Module>() {
			@Override
			public Module run(Connection conn) throws SQLException, CloudCoderAuthenticationException {
				// Verify that user is an instructor in the course
				// (throwing CloudCoderAuthenticationException if not)
				PreparedStatement verifyInstructorStmt = prepareStatement(
						conn,
						"select cr.id from cc_course_registrations as cr, cc_problems as p " +
						" where cr.user_id = ? " +
						"   and p.problem_id = ? " +
						"   and cr.course_id = p.course_id " +
						"   and cr.registration_type >= ?"
				);
				verifyInstructorStmt.setInt(1, user.getId());
				verifyInstructorStmt.setInt(2, problem.getProblemId());
				verifyInstructorStmt.setInt(3, CourseRegistrationType.INSTRUCTOR.ordinal());
				
				ResultSet verifyInstructorResultSet = executeQuery(verifyInstructorStmt);
				if (!verifyInstructorResultSet.next()) {
					logger.info(
							"Attempt by user {} to set module for problem {} without instructor permission",
							user.getId(),
							problem.getProblemId());
					throw new CloudCoderAuthenticationException("Only an instructor can set the module for an exercise");
				}
				
				// See if the module exists already
				PreparedStatement findExisting = prepareStatement(
						conn,
						"select m.* from cc_modules as m where m.name = ?"
				);
				findExisting.setString(1, moduleName);
				
				Module module = new Module();
				ResultSet findExistingResultSet = executeQuery(findExisting);
				if (findExistingResultSet.next()) {
					// Use existing module
					DBUtil.loadModelObjectFields(module, Module.SCHEMA, findExistingResultSet);
				} else {
					// Module doesn't exist, so add it
					module.setName(moduleName);
					DBUtil.storeModelObject(conn, module);
				}
				
				// Update the problem to use the new module
				problem.setModuleId(module.getId());
				DBUtil.updateModelObject(conn, problem, Problem.SCHEMA);
				
				return module;
			}
			@Override
			public String getDescription() {
				return " setting module for problem";
			}
		});
	}
	
	@Override
	public StartedQuiz startOrContinueQuiz(final User user, final Quiz quiz) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<StartedQuiz>() {
			@Override
			public StartedQuiz run(Connection conn) throws SQLException {
				PreparedStatement query = prepareStatement(
						conn,
						"select sq.* from cc_started_quizzes as sq, cc_quizzes as q " +
						" where sq.user_id = ? " +
						"   and sq.quiz_id = ? " +
						"   and q.id = sq.quiz_id " +
						"   and q.start_time <= ? " +
						"   and (q.end_time = 0 or q.end_time > ?)"
				);
				query.setInt(1, user.getId());
				query.setInt(2, quiz.getId());
				long currentTime = System.currentTimeMillis();
				query.setLong(3, currentTime);
				query.setLong(4, currentTime);
				
				StartedQuiz startedQuiz = new StartedQuiz();
				ResultSet queryResult = executeQuery(query);
				if (queryResult.next()) {
					// Found the StartedQuiz
					DBUtil.loadModelObjectFields(startedQuiz, StartedQuiz.SCHEMA, queryResult);
				} else {
					// StartedQuiz doesn't exist yet, so create it
					startedQuiz.setQuizId(quiz.getId());
					startedQuiz.setUserId(user.getId());
					startedQuiz.setStartTime(currentTime);
					DBUtil.storeModelObject(conn, startedQuiz);
				}
				
				return startedQuiz;
			}
			
			@Override
			public String getDescription() {
				return " checking for started quiz";
			}
		});
	}
	
	@Override
	public StartedQuiz findUnfinishedQuiz(final User user) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<StartedQuiz>() {
			@Override
			public StartedQuiz run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select sq.* from cc_started_quizzes as sq, cc_quizzes as q " +
						" where sq.quiz_id = q.id " +
						"   and sq.user_id = ? " +
						"   and q.start_time < ? " +
						"   and (q.end_time = 0 or q.end_time > ?)"
				);
				stmt.setInt(1, user.getId());
				
				long currentTime = System.currentTimeMillis();
				stmt.setLong(2, currentTime);
				stmt.setLong(3, currentTime);
				
				ResultSet resultSet = executeQuery(stmt);
				StartedQuiz result = null;
				if (resultSet.next()) {
					result = new StartedQuiz();
					DBUtil.loadModelObjectFields(result, StartedQuiz.SCHEMA, resultSet);
					return result;
				}
				
				return result;
			}
			@Override
			public String getDescription() {
				return " finding unfinished quiz for user";
			}
		});
	}
	
	@Override
	public Integer[] getSectionsForCourse(final Course course, final User authenticatedUser) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<Integer[]>() {
			@Override
			public Integer[] run(Connection conn) throws SQLException {
				// Make sure user is an instructor in the course
				CourseRegistrationList regList = Queries.doGetCourseRegistrations(conn, course.getId(), authenticatedUser.getId(), this);
				if (!regList.isInstructor()) {
					return new Integer[0];
				}
				
				PreparedStatement stmt = prepareStatement(
						conn,
						"select distinct cr.section from cc_course_registrations as cr " +
						" where cr.course_id = ? " +
						" order by cr.section asc"
				);
				stmt.setInt(1, course.getId());
				
				List<Integer> result = new ArrayList<Integer>();
				ResultSet resultSet = executeQuery(stmt);
				while (resultSet.next()) {
					result.add(resultSet.getInt(1));
				}
				
				return result.toArray(new Integer[result.size()]);
			}
			@Override
			public String getDescription() {
				return " getting sections for course";
			}
		});
	}
	
	@Override
	public SubmissionReceipt[] getAllSubmissionReceiptsForUser(final Problem problem, final User user) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<SubmissionReceipt[]>() {
			@Override
			public SubmissionReceipt[] run(Connection conn) throws SQLException {
				PreparedStatement stmt = prepareStatement(
						conn,
						"select sr.*, e.* from cc_submission_receipts as sr, cc_events as e " +
						"  where sr.event_id = e.id " +
						"    and e.user_id = ? " +
						"    and e.problem_id = ? " +
						" order by e.timestamp asc"
				);
				stmt.setInt(1, user.getId());
				stmt.setInt(2, problem.getProblemId());
				
				ArrayList<SubmissionReceipt> result = new ArrayList<SubmissionReceipt>();
				
				ResultSet resultSet = executeQuery(stmt);
				while (resultSet.next()) {
					int index = 1;
					SubmissionReceipt receipt = new SubmissionReceipt();
					index = DBUtil.loadModelObjectFields(receipt, SubmissionReceipt.SCHEMA, resultSet, index);
					Event event = new Event();
					index = DBUtil.loadModelObjectFields(event, Event.SCHEMA, resultSet, index);
					
					receipt.setEvent(event);
					
					result.add(receipt);
				}
				
				return result.toArray(new SubmissionReceipt[result.size()]);
			}
			@Override
			public String getDescription() {
				return " getting subscription receipts for user";
			}
		});
	}
	
	@Override
	public ProblemText getSubmissionText(final User authenticatedUser, final User submitter, final Problem problem, final SubmissionReceipt receipt) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<ProblemText>() {
			@Override
			public ProblemText run(Connection conn) throws SQLException {
				// Check authenticated user's course registrations
				CourseRegistrationList regList =
						Queries.doGetCourseRegistrations(conn, problem.getCourseId(), authenticatedUser.getId(), this);

				// Note that the queries require that either
				//   (1) the authenticated user is the submitter of the change event
				//       specified in the submission receipt as the last edit, or
				//   (2) the authenticated user is an instructor in the course
				
				// There are two cases:
				//   - the normal case where we know the event if of the full-text
				//     change containing the submission text
				//   - the case where the submission receipt is the initial one
				//     where the status is SubmissionStatus.STARTED, in which case
				//     we look for the user's first full-text submission
				//     (which the client webapp will typically create using the
				//     problem's skeleton code)

				ProblemText result;
				
				if (receipt.getLastEditEventId() < 0) {
					// Don't know the submission receipt: look for the first full-text change
					// for the user/problem
					PreparedStatement stmt = prepareStatement(
							conn,
							"select oc.text from cc_changes as oc " +
							" where oc.event_id = " +
							"   (select min(e.id) from cc_changes as c, cc_events as e " +
							"     where c.event_id = e.id " +
							"       and e.user_id = ? " +
							"       and e.problem_id = ? " +
							"       and c.type = ? " +
							"       and (e.user_id = ? or ? = 1))"
					);
					stmt.setInt(1, submitter.getId());
					stmt.setInt(2, problem.getProblemId());
					stmt.setInt(3, ChangeType.FULL_TEXT.ordinal());
					stmt.setInt(4, authenticatedUser.getId());
					stmt.setInt(5, regList.isInstructor() ? 1 : 0);
					
					ResultSet resultSet = executeQuery(stmt);
					if (resultSet.next()) {
						result = new ProblemText(resultSet.getString(1), false);
					} else {
						result = new ProblemText("", false);
					}
				} else {
					// We have the event id of the full-text change, so just
					// find it.
					PreparedStatement stmt = prepareStatement(
							conn,
							"select c.text from cc_changes as c, cc_events as e " +
							" where c.event_id = e.id " +
							"   and c.event_id = ? " +
							"   and (e.user_id = ? or ? = 1) " +
							"   and e.problem_id = ?"
					);
					stmt.setInt(1, receipt.getLastEditEventId());
					stmt.setInt(2, authenticatedUser.getId());
					stmt.setInt(3, regList.isInstructor() ? 1 : 0);
					stmt.setInt(4, problem.getProblemId());
					
					ResultSet resultSet = executeQuery(stmt);
					if (resultSet.next()) {
						// Got it
						result = new ProblemText(resultSet.getString(1), false);
					} else {
						// No such edit event, or user is not authorized to see it
						result = new ProblemText("", false);
					}
				}
				
				return result;
			}
			@Override
			public String getDescription() {
				return " getting submission text";
			}
		});
	}
	
	@Override
	public NamedTestResult[] getTestResultsForSubmission(final User authenticatedUser, final Problem problem, final SubmissionReceipt receipt) {
		return databaseRun(new AbstractDatabaseRunnableNoAuthException<NamedTestResult[]>() {
			@Override
			public NamedTestResult[] run(Connection conn) throws SQLException {
				
				CourseRegistrationList regList = Queries.doGetCourseRegistrations(conn, problem.getCourseId(), authenticatedUser.getId(), this);
				
				// Get all test results
				PreparedStatement stmt = prepareStatement(
						conn,
						"select tr.*,  e.* from cc_test_results as tr, cc_submission_receipts as sr, cc_events as e " +
						" where e.id = ?" +
						"   and tr.submission_receipt_event_id = e.id " +
						"   and sr.event_id = e.id " +
						"   and (e.user_id = ? or ? = 1) " +
						"   and e.problem_id = ? " +
						" order by tr.id asc"
				);
				stmt.setInt(1, receipt.getEventId());
				stmt.setInt(2, authenticatedUser.getId());
				stmt.setInt(3, regList.isInstructor() ? 1 : 0);
				stmt.setInt(4, problem.getProblemId());
				
				// Get the test results (which we are assumed as stored in order by id)
				List<TestResult> testResults = new ArrayList<TestResult>();
				ResultSet resultSet = executeQuery(stmt);
				while (resultSet.next()) {
					TestResult testResult = new TestResult();
					DBUtil.loadModelObjectFields(testResult, TestResult.SCHEMA, resultSet);
					testResults.add(testResult);
				}
				
				// Get the test cases (so we can find out the test case names)
				List<TestCase> testCases = Queries.doGetTestCasesForProblem(conn, problem.getProblemId(), this);
				
				// Build the list of NamedTestResults
				NamedTestResult[] results = new NamedTestResult[testResults.size()];
				for (int i = 0; i < results.length; i++) {
					String testCaseName = (i < testCases.size() ? testCases.get(i).getTestCaseName() : ("t" + i));
					NamedTestResult namedTestResult = new NamedTestResult(testCaseName, testResults.get(i));
					results[i] = namedTestResult;
				}

				return results;
			}
			@Override
			public String getDescription() {
				return " getting test results for submission";
			}
		});
	}

	/**
	 * Run a database transaction and return the result.
	 * This method is for transactions that extend {@link AbstractDatabaseRunnableNoAuthException}
	 * and thus are guaranteed not to throw {@link CloudCoderAuthenticationException}.
	 * 
	 * @param databaseRunnable the transaction to run
	 * @return the result
	 */
	public<E> E databaseRun(AbstractDatabaseRunnableNoAuthException<E> databaseRunnable) {
		try {
			return doDatabaseRun(databaseRunnable);
		} catch (CloudCoderAuthenticationException e) {
			// The fact that the method takes an
			// AbstractDatabaseRunnableNoAuthException guarantees that the transaction
			// won't throw NetcoderAuthenticationException.
			throw new IllegalStateException("Can't happen", e);
		}
	}

	/**
	 * Run a database transaction and return the result.
	 * This method is for transactions that check the authenticity of provided
	 * user credentials and may throw {@link CloudCoderAuthenticationException}.
	 * 
	 * @param databaseRunnable the transaction to run
	 * @return the result
	 */
	public<E> E databaseRunAuth(AbstractDatabaseRunnable<E> databaseRunnable) throws CloudCoderAuthenticationException {
		return doDatabaseRun(databaseRunnable);
	}

	private<E> E doDatabaseRun(DatabaseRunnable<E> databaseRunnable) throws CloudCoderAuthenticationException {
		// Give the DatabaseRunnable access to the logger
		databaseRunnable.setLogger(logger);
		
		int attempts = 0;
		
		boolean successfulCommit = false;
		E result = null;

		// Attempt the transaction until a maximum number of attempts is reached.
		while (!successfulCommit && attempts < 20) {
			attempts++;
			
			Connection conn;
			boolean origAutocommit;
			
			// Attempt to get a connection
			try {
				conn = getConnection();
				origAutocommit = conn.getAutoCommit();
			} catch (SQLException e) {
				throw new PersistenceException("SQLException", e);
			}
			
			// Attempt the execute the transaction.
			// If the transaction is not successful (throws SQLException),
			// determine if it is because of a deadlock or other recoverable
			// error, and if so, retry.
			try {
				conn.setAutoCommit(false);
				result = databaseRunnable.run(conn);
				conn.commit();
				successfulCommit = true; // Hooray!
			} catch (SQLException e) {
				String sqlState = e.getSQLState();
				if (sqlState != null && (sqlState.equals("40001") || sqlState.equals("41000") || sqlState.equals("23000"))) {
					// Deadlock detected: retry transaction
					// NOTE: I have also included duplicate key errors (23000).
					// There is evidence that a MySQL bug can cause spurious
					// duplicate key errors on auto increment fields, which I *think*
					// I have actually seen in CloudCoder error logs.
					//   See: http://www.softwareprojects.com/resources/programming/t-mysql-innodb-deadlocks-and-duplicate-key-errors-12-1970.html
					// Workaround is to retry the transaction.
					logger.info("MySQL deadlock detected (sqlState=" + sqlState + ")", e);
				} else {
					// Some other kind of transaction failure.
					logger.error("Transaction failed with SQLException", e);
					throw new PersistenceException("SQLException", e);
				}
			} finally {
				// If the transaction didn't succeed, roll back
				if (!successfulCommit) {
					try {
						conn.rollback();
					} catch (SQLException ex) {
						throw new PersistenceException("SQLException (on rollback)", ex);
					}
				}
				
				// Restore the original autocommit value and release the connection.
				try {
					conn.setAutoCommit(origAutocommit);
					releaseConnection();
				} catch (SQLException e) {
					throw new PersistenceException("SQLException (releasing connection)", e);
				}
			}
		}
		
		// If the transaction was never executed successfully, throw a PersistenceException
		if (!successfulCommit) {
			throw new PersistenceException("Could not complete transaction (gave up after " + attempts + " attempts)");
		}
		
		// Success!
		return result;
	}
	
	/**
	 * Count students in course for given {@link Problem}.
	 * 
	 * @param problem  the {@link Problem}
	 * @param conn     the database connection
	 * @param abstractDatabaseRunnable the {@link AbstractDatabaseRunnable}
	 * @return number of students in the course
	 * @throws SQLException 
	 */
	protected int doCountStudentsInCourse(Problem problem, Connection conn,
			AbstractDatabaseRunnableNoAuthException<ProblemSummary> abstractDatabaseRunnable) throws SQLException {
		PreparedStatement stmt = abstractDatabaseRunnable.prepareStatement(
				conn,
				"select count(*) from " +
				Course.SCHEMA.getDbTableName() + " as c, " +
				CourseRegistration.SCHEMA.getDbTableName() + " as cr " +
				" where c.id = ? " +
				"   and cr.course_id = c.id "
				);
		stmt.setInt(1, problem.getCourseId());
		
		ResultSet resultSet = abstractDatabaseRunnable.executeQuery(stmt);
		if (!resultSet.next()) {
			return -1;
		}
		
		return resultSet.getInt(1);
	}
	
	private Boolean doInsertProblem(
			final Problem problem,
			Connection conn,
			AbstractDatabaseRunnable<?> databaseRunnable) throws SQLException {
		PreparedStatement stmt = databaseRunnable.prepareStatement(
				conn,
				"insert into " + Problem.SCHEMA.getDbTableName() +
				" values (" +
				DBUtil.getInsertPlaceholdersNoId(Problem.SCHEMA) +
				")",
				PreparedStatement.RETURN_GENERATED_KEYS
		);
		
		storeNoId(problem, stmt, 1);
		
		stmt.executeUpdate();
		
		ResultSet generatedKey = databaseRunnable.getGeneratedKeys(stmt);
		if (!generatedKey.next()) {
			throw new SQLException("Could not get generated key for inserted problem");
		}
		problem.setProblemId(generatedKey.getInt(1));
		
		return true;
	}

	private Boolean doInsertTestCases(final Problem problem,
			final List<TestCase> testCaseList, Connection conn,
			AbstractDatabaseRunnable<?> databaseRunnable) throws SQLException {
		PreparedStatement stmt = databaseRunnable.prepareStatement(
				conn,
				"insert into " + TestCase.SCHEMA.getDbTableName() + " values (NULL, ?, ?, ?, ?, ?)",
				PreparedStatement.RETURN_GENERATED_KEYS
		);
		
		for (TestCase testCase : testCaseList) {
			testCase.setProblemId(problem.getProblemId());
			storeNoId(testCase, stmt, 1);
			stmt.addBatch();
		}
		
		stmt.executeBatch();
		
		ResultSet generatedKeys = databaseRunnable.getGeneratedKeys(stmt);
		int count = 0;
		while (generatedKeys.next()) {
			testCaseList.get(count).setTestCaseId(generatedKeys.getInt(1));
			count++;
		}
		if (count != testCaseList.size()) {
			throw new SQLException("wrong number of generated keys for inserted test cases");
		}
		
		return true;
	}

	private Boolean doUpdateProblem(
			final Problem problem,
			Connection conn,
			AbstractDatabaseRunnable<?> databaseRunnable) throws SQLException {
		
		// Special case: if the authorship status of the original version was
		// IMPORTED, then we need to change it to IMPORTED_AND_MODIFIED and
		// set the parent hash field.  (In theory, loading the current
		// version of the problem from the database isn't necessary,
		// but it doesn't hurt to be paranoid, and authorship status is
		// important to track precisely.)
		Problem orig = new Problem();
		PreparedStatement fetchOrig = databaseRunnable.prepareStatement(
				conn,
				"select * from " + Problem.SCHEMA.getDbTableName() + " where " + Problem.PROBLEM_ID.getName() + " = ?");
		fetchOrig.setInt(1, problem.getProblemId());
		ResultSet origRS = databaseRunnable.executeQuery(fetchOrig);
		if (!origRS.next()) {
			throw new SQLException("Can't update problem " + problem.getProblemId() + " because it doesn't exist");
		}
		Queries.loadGeneric(orig, origRS, 1, Problem.SCHEMA);
		
		if (orig.getProblemAuthorship() == ProblemAuthorship.IMPORTED) {
			// FIXME: get the hash of the imported problem from the database
			// Probably, could just use parent_hash: for IMPORTED problems,
			// it's the actual hash, but for IMPORTED_AND_MODIFIED problems,
			// it's the parent hash.  Actually, that would be nice because
			// then we don't have to do anything with the hash.
			problem.setProblemAuthorship(ProblemAuthorship.IMPORTED_AND_MODIFIED);
		}
		
		PreparedStatement update = databaseRunnable.prepareStatement(
				conn,
				"update " + Problem.SCHEMA.getDbTableName() +
				" set " + DBUtil.getUpdatePlaceholdersNoId(Problem.SCHEMA) +
				" where problem_id = ?"
				);
		int index = storeNoId(problem, update, 1);
		update.setInt(index, problem.getProblemId());
		
		int rowCount = update.executeUpdate();
		if (rowCount != 1) {
			throw new SQLException("Could not update problem (no such problem in database?)");
		}
		
		return true;
	}
	
	protected void doDeleteTestCases(
			Integer problemId,
			Connection conn,
			AbstractDatabaseRunnable<ProblemAndTestCaseList> abstractDatabaseRunnable) throws SQLException {
		PreparedStatement deleteStmt = abstractDatabaseRunnable.prepareStatement(
				conn,
				"delete from " + TestCase.SCHEMA.getDbTableName() + " where problem_id = ?");
		deleteStmt.setInt(1, problemId);
		
		deleteStmt.executeUpdate();
	}
	
	protected void doFindRepoTestCases(
			RepoProblem repoProblem,
			RepoProblemAndTestCaseList exercise,
			Connection conn,
			AbstractDatabaseRunnable<?> dbRunnable)
			throws SQLException {
		PreparedStatement findRepoTestCases = dbRunnable.prepareStatement(
				conn,
				"select * from " + RepoTestCase.SCHEMA.getDbTableName() + " as rtc " +
				" where rtc." + RepoTestCase.REPO_PROBLEM_ID.getName() + " = ?");
		findRepoTestCases.setInt(1, repoProblem.getId());
		
		ResultSet repoTestCaseRs = dbRunnable.executeQuery(findRepoTestCases);
		while (repoTestCaseRs.next()) {
			RepoTestCase repoTestCase = new RepoTestCase();
			load(repoTestCase, repoTestCaseRs, 1);
			exercise.addTestCase(repoTestCase);
		}
	}

	/**
	 * Add a {@link RepoProblemTag} to the database as part of a transaction.
	 * 
	 * @param conn             the database connection
	 * @param repoProblemTag   the {@link RepoProblemTag} to add
	 * @param databaseRunnable the transaction ({@link AbstractDatabaseRunnableNoAuthException})
	 * @return true if the tag was added succesfully,
	 *         false if the user has already added an identical tag
	 * @throws SQLException
	 */
	protected Boolean doAddRepoProblemTag(
			Connection conn,
			RepoProblemTag repoProblemTag,
			AbstractDatabaseRunnableNoAuthException<?> databaseRunnable) throws SQLException {
		PreparedStatement stmt = databaseRunnable.prepareStatement(
				conn,
				"insert into " + RepoProblemTag.SCHEMA.getDbTableName() +
				" values (" + DBUtil.getInsertPlaceholders(RepoProblemTag.SCHEMA) + ")"
		);
		
		DBUtil.bindModelObjectValuesForInsert(repoProblemTag, RepoProblemTag.SCHEMA, stmt);
		
		try {
			stmt.executeUpdate();
		} catch (SQLException e) {
			if (e.getSQLState().equals("23000")) {
				// failed due to duplicate key: this almost certainly means
				// that the user has already added the same tag
				return false;
			} else {
				// some other failure
				throw e;
			}
		}
		
		return true;
	}

	protected List<UserAndSubmissionReceipt> doGetBestSubmissionReceipts(
			Connection conn,
			final Problem problem,
			final int section,
			AbstractDatabaseRunnable<?> dbRunnable) throws SQLException {
		
		// Clearly, my SQL is either amazing or appalling.
		// Probably the latter.
		PreparedStatement stmt = dbRunnable.prepareStatement(
				conn,
				
				"select uu.*, best.* from cc_users as uu " +
				"  left join " +
				"         (select u.id as the_user_id, e.*, sr.* " +
				"           from cc_users as u, cc_events as e, cc_submission_receipts as sr," +
				"           (select i_u.id as user_id, best.max_tests_passed as max_tests_passed, MIN(i_e.timestamp) as timestamp" +
				"             from cc_users as i_u," +
				"                  cc_events as i_e," +
				"                  cc_submission_receipts as i_sr," +
				"                  (select ii_u.id as user_id, MAX(ii_sr.num_tests_passed) as max_tests_passed" +
				"                     from cc_users as ii_u, cc_events as ii_e, cc_submission_receipts as ii_sr " +
				"                    where ii_u.id = ii_e.user_id " +
				"                      and ii_e.id = ii_sr.event_id " +
				"                      and ii_e.problem_id = ?" +
				"                   group by ii_u.id) as best" +
				"" +
				"             where i_u.id = i_e.user_id" +
				"               and i_e.id = i_sr.event_id" +
				"               and i_e.problem_id = ?" +
				"               and i_u.id = best.user_id" +
				"               and i_sr.num_tests_passed = best.max_tests_passed" +
				"               group by i_u.id, best.max_tests_passed) as earliest_and_best" +
				"" +
				"          where u.id = e.user_id" +
				"              and e.id = sr.event_id" +
				"              and e.problem_id = ?" +
				"              and u.id = earliest_and_best.user_id" +
				"              and sr.num_tests_passed = earliest_and_best.max_tests_passed" +
				"              and e.timestamp = earliest_and_best.timestamp) as best " +
				"          on uu.id = best.the_user_id " +
				"" +
				" where uu.id in (select distinct xu.id from cc_users as xu, cc_course_registrations as xcr "+
				"                  where xu.id = xcr.user_id " +
				"                    and xcr.course_id = ? " +
				"                    and (? = 0 or xcr.section = ?)) "
		);
		int problemId = problem.getProblemId();
		stmt.setInt(1, problemId);
		stmt.setInt(2, problemId);
		stmt.setInt(3, problemId);
		stmt.setInt(4, problem.getCourseId());
		stmt.setInt(5, section); // if section is 0, all sections will be included
		stmt.setInt(6, section);
		
		ResultSet resultSet = dbRunnable.executeQuery(stmt);
		List<UserAndSubmissionReceipt> result = new ArrayList<UserAndSubmissionReceipt>();
		
		while (resultSet.next()) {
			int index = 1;
			User user = new User();
			index = Queries.loadGeneric(user, resultSet, index, User.SCHEMA);

			SubmissionReceipt receipt;
			
			// Is there a best submission receipt?
			if (resultSet.getObject(index) != null) {
				// Found a best submission receipt

				index++; // skip best.the_user_id column
				
				Event event = new Event();
				index = Queries.loadGeneric(event, resultSet, index, Event.SCHEMA);
				receipt = new SubmissionReceipt();
				Queries.loadGeneric(receipt, resultSet, index, SubmissionReceipt.SCHEMA);
				
				receipt.setEvent(event);
			} else {
				// No best submission receipt
				receipt = null;
			}
			
			UserAndSubmissionReceipt pair = new UserAndSubmissionReceipt();
			pair.setUser(user);
			pair.setSubmissionReceipt(receipt);
			
			result.add(pair);
		}
		
		return result;
	}
	
	protected void load(RepoProblem repoProblem, ResultSet resultSet, int index) throws SQLException {
		Queries.loadGeneric(repoProblem, resultSet, index, RepoProblem.SCHEMA);
	}
	
	protected void load(RepoTestCase repoTestCase, ResultSet resultSet, int index) throws SQLException {
		Queries.loadGeneric(repoTestCase, resultSet, index, RepoTestCase.SCHEMA);
	}

	protected int storeNoId(Problem problem, PreparedStatement stmt, int index) throws SQLException {
		return Queries.storeNoIdGeneric(problem, stmt, index, Problem.SCHEMA);
	}

	protected void storeNoId(TestCase testCase, PreparedStatement stmt, int index) throws SQLException {
		Queries.storeNoIdGeneric(testCase, stmt, index, TestCase.SCHEMA);
	}
	
}
