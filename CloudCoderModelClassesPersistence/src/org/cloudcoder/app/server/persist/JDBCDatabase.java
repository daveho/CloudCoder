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
import java.util.List;

import org.cloudcoder.app.server.persist.txn.AddTestCasesToProblem;
import org.cloudcoder.app.server.persist.txn.AddUserRegistrationRequest;
import org.cloudcoder.app.server.persist.txn.AddUserToCourse;
import org.cloudcoder.app.server.persist.txn.AuthenticateUser;
import org.cloudcoder.app.server.persist.txn.CreateProblemSummary;
import org.cloudcoder.app.server.persist.txn.DeleteProblem;
import org.cloudcoder.app.server.persist.txn.EditUser;
import org.cloudcoder.app.server.persist.txn.EditUserGivenUserData;
import org.cloudcoder.app.server.persist.txn.FindCourseRegistrationsGivenUserAndCourse;
import org.cloudcoder.app.server.persist.txn.FindCourseRegistrationsGivenUserAndCourseId;
import org.cloudcoder.app.server.persist.txn.FindUserRegistrationRequestGivenSecret;
import org.cloudcoder.app.server.persist.txn.GetAllChangesNewerThan;
import org.cloudcoder.app.server.persist.txn.GetBestSubmissionReceiptsForProblem;
import org.cloudcoder.app.server.persist.txn.GetBestSubmissionReceiptsForProblemForAuthenticatedUser;
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
import org.cloudcoder.app.server.persist.txn.GetRepoProblemAndTestCaseListGivenHash;
import org.cloudcoder.app.server.persist.txn.GetSubmissionReceipt;
import org.cloudcoder.app.server.persist.txn.GetTestCasesForProblem;
import org.cloudcoder.app.server.persist.txn.GetTestCasesForProblemCheckAuth;
import org.cloudcoder.app.server.persist.txn.GetUserGivenId;
import org.cloudcoder.app.server.persist.txn.GetUserWithoutAuthentication;
import org.cloudcoder.app.server.persist.txn.GetUsersInCourse;
import org.cloudcoder.app.server.persist.txn.InsertProblem;
import org.cloudcoder.app.server.persist.txn.InsertUsersFromInputStream;
import org.cloudcoder.app.server.persist.txn.Queries;
import org.cloudcoder.app.server.persist.txn.ReplaceSubmissionReceipt;
import org.cloudcoder.app.server.persist.txn.ReplaceTestResults;
import org.cloudcoder.app.server.persist.txn.SearchRepositoryExercises;
import org.cloudcoder.app.server.persist.txn.StoreChanges;
import org.cloudcoder.app.server.persist.txn.StoreProblemAndTestCaseList;
import org.cloudcoder.app.server.persist.txn.StoreRepoProblemAndTestCaseList;
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
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.Module;
import org.cloudcoder.app.shared.model.NamedTestResult;
import org.cloudcoder.app.shared.model.OperationResult;
import org.cloudcoder.app.shared.model.Pair;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndSubmissionReceipt;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.ProblemList;
import org.cloudcoder.app.shared.model.ProblemSummary;
import org.cloudcoder.app.shared.model.ProblemText;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.RepoProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.RepoProblemSearchCriteria;
import org.cloudcoder.app.shared.model.RepoProblemSearchResult;
import org.cloudcoder.app.shared.model.RepoProblemTag;
import org.cloudcoder.app.shared.model.StartedQuiz;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
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
		databaseRun(new InsertProblem(problem));
	}
	
	@Override
	public void addTestCases(final Problem problem, final List<TestCase> testCaseList) {
		databaseRun(new AddTestCasesToProblem(problem, testCaseList));
	}
	
	@Override
	public ProblemSummary createProblemSummary(final Problem problem) {
		return databaseRun(new CreateProblemSummary(problem));
	}
	
	@Override
	public SubmissionReceipt getSubmissionReceipt(final int submissionReceiptId) {
		return databaseRun(new GetSubmissionReceipt(submissionReceiptId));
	}
	
	@Override
	public void replaceTestResults(final TestResult[] testResults, final int submissionReceiptId) {
		databaseRun(new ReplaceTestResults(submissionReceiptId, testResults));
	}
	
	@Override
	public void updateSubmissionReceipt(final SubmissionReceipt receipt) {
		databaseRun(new ReplaceSubmissionReceipt(receipt));
	}
	
	@Override
	public ProblemAndTestCaseList storeProblemAndTestCaseList(
			final ProblemAndTestCaseList problemAndTestCaseList, final Course course, final User user)
			throws CloudCoderAuthenticationException {
		return databaseRunAuth(new StoreProblemAndTestCaseList(problemAndTestCaseList, course, user));
	}
	
	@Override
	public RepoProblemAndTestCaseList getRepoProblemAndTestCaseList(final String hash) {
		return databaseRun(new GetRepoProblemAndTestCaseListGivenHash(hash));
	}

	@Override
	public void storeRepoProblemAndTestCaseList(final RepoProblemAndTestCaseList exercise, final User user) {
		databaseRun(new StoreRepoProblemAndTestCaseList(user, exercise));
	}
	
	@Override
	public List<RepoProblemSearchResult> searchRepositoryExercises(final RepoProblemSearchCriteria searchCriteria) {
		return databaseRun(new SearchRepositoryExercises(searchCriteria));
	}
	
	@Override
	public CourseRegistrationList findCourseRegistrations(final User user, final Course course) {
		return databaseRun(new FindCourseRegistrationsGivenUserAndCourse(course, user));
	}
	
	@Override
	public CourseRegistrationList findCourseRegistrations(final User user, final int courseId) {
		return databaseRun(new FindCourseRegistrationsGivenUserAndCourseId(user, courseId));
	}
	
	@Override
	public List<UserAndSubmissionReceipt> getBestSubmissionReceipts(
			final Course unused, final int section, final Problem problem) {
		return databaseRun(new GetBestSubmissionReceiptsForProblem(section, problem));
	}

	@Override
	public List<UserAndSubmissionReceipt> getBestSubmissionReceipts(final Problem problem, final int section, final User authenticatedUser) {
		return databaseRun(new GetBestSubmissionReceiptsForProblemForAuthenticatedUser(section,
				problem, authenticatedUser));
	}
	
	@Override
	public boolean deleteProblem(final User user, final Course course, final Problem problem)
			throws CloudCoderAuthenticationException {
		return databaseRunAuth(new DeleteProblem(user, problem, course));
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
		return databaseRun(new AddUserRegistrationRequest(request));
	}
	
	@Override
	public UserRegistrationRequest findUserRegistrationRequest(final String secret) {
		return databaseRun(new FindUserRegistrationRequestGivenSecret(secret));
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
				return Queries.doAddRepoProblemTag(conn, repoProblemTag, this);
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
	
}
