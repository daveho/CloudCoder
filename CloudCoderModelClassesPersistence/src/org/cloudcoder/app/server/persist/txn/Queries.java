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

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.cloudcoder.app.shared.model.Quiz;
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

}
