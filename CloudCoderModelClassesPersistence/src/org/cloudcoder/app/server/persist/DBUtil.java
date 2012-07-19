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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cloudcoder.app.shared.model.ModelObjectField;
import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC utility methods.
 * 
 * @author David Hovemeyer
 */
public class DBUtil {
    private static final Logger logger=LoggerFactory.getLogger(DBUtil.class);
    
    /**
     * Quietly close a {@link PreparedStatement}.
     * 
     * @param stmt the PreparedStatement to close
     */
	public static void closeQuietly(PreparedStatement stmt) {
		try {
			if (stmt != null) {
				stmt.close();
			}
		} catch (SQLException e) {
		    logger.error("Unable to close prepared statement",e);
		}
	}

	/**
	 * Quietly close a {@link ResultSet}.
	 * 
	 * @param resultSet the ResultSet to close
	 */
	public static void closeQuietly(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (SQLException e) {
		    logger.error("Unable to close result set",e);
		}
	}
	
	/**
	 * Get placeholders for an insert statement.
	 * 
	 * @param schema the schema for the object being inserted
	 * @return placeholders for an insert statement
	 */
	public static String getInsertPlaceholders(ModelObjectSchema schema) {
		return getInsertPlaceholders(schema, schema.getNumFields());
	}

	/**
	 * Get given number of placeholders for an insert statement.
	 * 
	 * @param schema the schema for the object being inserted
	 * @param num number of placeholders (which must be less than or equal to
	 *            the number of fields)
	 * @return the placeholders
	 */
	public static String getInsertPlaceholders(ModelObjectSchema schema, int num) {
		if (num > schema.getNumFields()) {
			throw new IllegalArgumentException();
		}
		
		StringBuilder buf = new StringBuilder();
		
		for (int i = 0; i < num; i++) {
			if (buf.length() > 0) {
				buf.append(", ");
			}
			buf.append("?");
		}
		
		return buf.toString();
	}
	
	/**
	 * Get placeholders for an update statement where all fields
	 * will be updated.
	 * 
	 * @return placeholders for an update statement where all fields will be updated
	 */
	public static String getUpdatePlaceholders(ModelObjectSchema schema) {
		return doGetUpdatePlaceholders(schema, true);
	}
	
	/**
	 * Get placeholders for an update statement where all fields except for
	 * the unique id field will be updated.
	 * 
	 * @return placeholders for an update statement where all fields except the
	 *         unique id field will be update
	 */
	public static String getUpdatePlaceholdersNoId(ModelObjectSchema schema) {
		return doGetUpdatePlaceholders(schema, false);
	}

	private static String doGetUpdatePlaceholders(ModelObjectSchema schema, boolean includeUniqueId) {
		StringBuilder buf = new StringBuilder();
		
		for (ModelObjectField field : schema.getFieldList()) {
			if (!field.isUniqueId() || includeUniqueId) {
				if (buf.length() > 0) {
					buf.append(", ");
				}
				buf.append(field.getName());
				buf.append(" = ?");
			}
		}
		
		return buf.toString();
	}
}
