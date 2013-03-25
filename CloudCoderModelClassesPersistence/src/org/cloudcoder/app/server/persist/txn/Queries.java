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

import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
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

	public static User getUser(Connection conn, int userId) throws SQLException {
	    PreparedStatement stmt = conn.prepareStatement("select * from "+User.SCHEMA.getDbTableName()+" where id = ?");
	    stmt.setInt(1, userId);
	    
	    ResultSet resultSet = stmt.executeQuery();
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

}
