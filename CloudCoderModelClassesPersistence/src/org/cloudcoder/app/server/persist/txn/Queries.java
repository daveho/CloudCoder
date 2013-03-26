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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.CourseRegistration;
import org.cloudcoder.app.shared.model.CourseRegistrationList;
import org.cloudcoder.app.shared.model.CourseRegistrationType;
import org.cloudcoder.app.shared.model.Event;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Quiz;
import org.cloudcoder.app.shared.model.Term;
import org.cloudcoder.app.shared.model.User;

/**
 * Class containing methods performing various queries.
 * Transaction classes can use these as subroutines in order to
 * share code.
 * 
 * @author David Hovemeyer
 */
public class Queries {

	/**
	 * Generic method to load model object data from the current row of
	 * a ResultSet.
	 * 
	 * @param modelObj   the model object
	 * @param resultSet  the ResultSet
	 * @param index      the index of the first column containing model object data
	 * @param schema     the schema of the model object
	 * @return           the index of the first column after the model object data in the result set
	 * @throws SQLException
	 */
	public static<E> int loadGeneric(E modelObj, ResultSet resultSet, int index, ModelObjectSchema<E> schema) throws SQLException {
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			// Note: this could return an object which does not exactly match the field type
			Object value = resultSet.getObject(index++);
			value = DBUtil.convertValue(value, field.getType());
			
			field.setUntyped(modelObj, value);
		}
		return index;
	}

	public static void load(User user, ResultSet resultSet, int index) throws SQLException {
		loadGeneric(user, resultSet, index, User.SCHEMA);
	}

	public static User getUser(Connection conn, int userId, AbstractDatabaseRunnable<?> dbRunnable) throws SQLException {
	    PreparedStatement stmt = dbRunnable.prepareStatement(conn, "select * from "+User.SCHEMA.getDbTableName()+" where id = ?");
	    stmt.setInt(1, userId);
	    
	    ResultSet resultSet = dbRunnable.executeQuery(stmt);
	    if (!resultSet.next()) {
	        return null;
	    }
	    
	    User user = new User();
	    load(user, resultSet, 1);
	    return user;
	}

	public static void load(ConfigurationSetting configurationSetting, ResultSet resultSet, int index) throws SQLException {
		loadGeneric(configurationSetting, resultSet, index, ConfigurationSetting.SCHEMA);
	}

	public static User getUser(Connection conn, String userName, AbstractDatabaseRunnable<?> dbRunnable) throws SQLException {
	    PreparedStatement stmt = dbRunnable.prepareStatement(conn, "select * from "+User.SCHEMA.getDbTableName()+" where username = ?");
	    stmt.setString(1, userName);
	    
	    ResultSet resultSet = dbRunnable.executeQuery(stmt);
	    if (!resultSet.next()) {
	        return null;
	    }
	    
	    User user = new User();
	    load(user, resultSet, 1);
	    return user;
	}

	public static Quiz doFindQuiz(
			int problemId,
			int section,
			long currentTimeMillis,
			Connection conn,
			AbstractDatabaseRunnableNoAuthException<?> dbRunnable) throws SQLException {
		PreparedStatement stmt = dbRunnable.prepareStatement(
				conn,
				"select q.* from cc_quizzes as q " +
				" where q.problem_id = ? " +
				"   and q.section = ? " +
				"   and q.start_time <= ? " +
				"   and (q.end_time >= ? or q.end_time = 0)"
		);
		stmt.setInt(1, problemId);
		stmt.setInt(2, section);
		stmt.setLong(3, currentTimeMillis);
		stmt.setLong(4, currentTimeMillis);
		
		Quiz result = null;
		
		ResultSet resultSet = dbRunnable.executeQuery(stmt);
		while (resultSet.next()) {
			Quiz quiz = new Quiz();
			DBUtil.loadModelObjectFields(quiz, Quiz.SCHEMA, resultSet);
			if (result == null || quiz.getEndTime() > result.getEndTime()) {
				result = quiz;
			}
		}
	
		return result;
	}

	public static void load(Change change, ResultSet resultSet, int index) throws SQLException {
		// Change objects require special handling because the database
		// has two columns for the change text (depending on how long the
		// text is).  Whichever of the columns is not null should be used
		// as the text value to store in the model object.
		
		String text = null;
		
		for (ModelObjectField<? super Change, ?> field : Change.SCHEMA.getFieldList()) {
			Object value = resultSet.getObject(index++);
			if (field != Change.TEXT_SHORT && field != Change.TEXT) {
				field.setUntyped(change, DBUtil.convertValue(value, field.getType()));
			} else {
				// This is the value of either the text_short or text columns.
				// Use whichever is not null.
				if (value != null) {
					text = (String) value;
				}
			}
		}
		change.setText(text);
	}

	public static Change getChangeAndEvent(ResultSet resultSet) throws SQLException {
		Change change = new Change();
		load(change, resultSet, 1);
		Event event = new Event();
		loadGeneric(event, resultSet, Change.NUM_FIELDS + 1, Event.SCHEMA);
		change.setEvent(event);
		return change;
	}

	public static List<? extends Object[]> doGetCoursesForUser(
			final User user,
			Connection conn,
			AbstractDatabaseRunnable<?> databaseRunnable) throws SQLException {
		List<Object[]> result = new ArrayList<Object[]>();
	
		PreparedStatement stmt = databaseRunnable.prepareStatement(
				conn,
				"select c.*, t.*, r.* from " + Course.SCHEMA.getDbTableName() + " as c, " + Term.SCHEMA.getDbTableName() + " as t, " + CourseRegistration.SCHEMA.getDbTableName() + " as r " +
				" where c.id = r.course_id " + 
				"   and c.term_id = t.id " +
				"   and r.user_id = ? " +
				" order by c.year desc, t.seq desc"
		);
		stmt.setInt(1, user.getId());
		
		ResultSet resultSet = databaseRunnable.executeQuery(stmt);
		
		while (resultSet.next()) {
			Course course = new Course();
			loadGeneric(course, resultSet, 1, Course.SCHEMA);
			Term term = new Term();
			loadGeneric(term, resultSet, Course.NUM_FIELDS + 1, Term.SCHEMA);
			CourseRegistration reg = new CourseRegistration();
			loadGeneric(reg, resultSet, Course.NUM_FIELDS + Term.NUM_FIELDS + 1, CourseRegistration.SCHEMA);
			result.add(new Object[]{course, term, reg});
		}
		
		return result;
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
	public static List<Problem> doGetProblemsInCourse(User user, Course course,
			Connection conn,
			AbstractDatabaseRunnableNoAuthException<?> dbRunnable) throws SQLException {
		
		//
		// Note that we have to join on course registrations to ensure
		// that we return courses that the user is actually registered for.
		// For each problem, we also have to check that the user is either
		// an instructor or that the problem is visible.  Students should
		// not be allowed to see problems that are not visible.
		//
		PreparedStatement stmt = dbRunnable.prepareStatement(
				conn,
				"select p.* from " + Problem.SCHEMA.getDbTableName() + " as p, " + Course.SCHEMA.getDbTableName() + " as c, " + CourseRegistration.SCHEMA.getDbTableName() + " as r " +
				" where p.course_id = c.id " +
				"   and p." + Problem.DELETED.getName() + " = 0 " +
				"   and r.course_id = c.id " +
				"   and r.user_id = ? " +
				"   and (r.registration_type >= " + CourseRegistrationType.INSTRUCTOR.ordinal() + " or p.visible <> 0)" +
				"   and c.id = ?"
		);
		stmt.setInt(1, user.getId());
		stmt.setInt(2, course.getId());
		
		ResultSet resultSet = dbRunnable.executeQuery(stmt);
		
		List<Problem> resultList = new ArrayList<Problem>();
		while (resultSet.next()) {
			Problem problem = new Problem();
			loadGeneric(problem, resultSet, 1, Problem.SCHEMA);
			resultList.add(problem);
		}
		
		return resultList;
	}

	public static CourseRegistrationList doGetCourseRegistrations(
			Connection conn,
			int courseId,
			int userId,
			AbstractDatabaseRunnable<?> abstractDatabaseRunnable) throws SQLException {
		PreparedStatement stmt = abstractDatabaseRunnable.prepareStatement(
				conn,
				"select cr.* from " + CourseRegistration.SCHEMA.getDbTableName() + " as cr " +
				" where cr." + CourseRegistration.USER_ID.getName() + " = ? " +
				"   and cr." + CourseRegistration.COURSE_ID.getName() + " = ?"
		);
		stmt.setInt(1, userId);
		stmt.setInt(2, courseId);
		
		ResultSet resultSet = abstractDatabaseRunnable.executeQuery(stmt);
		
		CourseRegistrationList result = new CourseRegistrationList();
		
		while (resultSet.next()) {
			CourseRegistration reg = new CourseRegistration();
			DBUtil.loadModelObjectFields(reg, CourseRegistration.SCHEMA, resultSet);
			result.getList().add(reg);
		}
		
		return result;
	}

	/**
	 * Store the field values of a model object (sans unique id) in the parameters
	 * of the given PreparedStatement. 
	 * 
	 * @param modelObj the model object
	 * @param stmt     the PreparedStatement
	 * @param index    the index of the first PreparedStatement parameter where the model object data should be stored
	 * @param schema   the schema of the model object
	 * @return the index of the parameter just after where the model object's field values are stored
	 * @throws SQLException
	 */
	public static<E> int storeNoIdGeneric(E modelObj, PreparedStatement stmt, int index, ModelObjectSchema<E> schema) throws SQLException {
		for (ModelObjectField<? super E, ?> field : schema.getFieldList()) {
			if (!field.isUniqueId()) {
				Object value = field.get(modelObj);
				value = DBUtil.convertValueToStore(value);
				
				if (value instanceof String) {
					String s = (String) value;
					// Somewhat hackish solution to avoiding "string too long" errors inserting into database
					// FIXME: broken if string contains characters that don't have a 1-byte encoding in UTF8
					if (s.length() > field.getSize()) {
						s = s.substring(0, field.getSize());
						value = s;
					}
				}
				
				stmt.setObject(index++, value);
			}
		}
		return index;
	}

	public static void store(Change change, PreparedStatement stmt, int index) throws SQLException {
		// Change objects require special handling so that we use the correct
		// database column to store the change text.
		
		String changeText = change.getText();
		boolean isShort = changeText.length() <= Change.MAX_TEXT_LEN_IN_ROW;
		String textShort = isShort ? changeText : null;
		String textLong  = !isShort ? changeText : null;
		
		for (ModelObjectField<? super Change, ?> field : Change.SCHEMA.getFieldList()) {
			if (field == Change.TEXT_SHORT) {
				stmt.setString(index++, textShort);
			} else if (field == Change.TEXT) {
				stmt.setString(index++, textLong);
			} else {
				stmt.setObject(index++, DBUtil.convertValueToStore(field.get(change)));
			}
		}
	}

}
