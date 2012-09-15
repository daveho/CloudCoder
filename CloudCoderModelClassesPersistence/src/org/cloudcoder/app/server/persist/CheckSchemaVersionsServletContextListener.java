// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011,2012 Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011,2012 David H. Hovemeyer <dhovemey@ycp.edu>
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cloudcoder.app.shared.model.ModelObjectSchema;

/**
 * A ServletContextListener to check that the schema versions of
 * the database tables match the schema versions of the model
 * object classes.  If there is a mismatch, we report it by
 * adding an error to the {@link InitErrorList} singleton. 
 * 
 * @author David Hovemeyer
 */
public abstract class CheckSchemaVersionsServletContextListener implements ServletContextListener {

	private List<ModelObjectSchema<?>> tableList;
	
	/**
	 * Constructor.
	 * 
	 * @param tableList the list of {@link ModelObjectSchema} objects whose
	 *                  database schema versions should be checked
	 */
	public CheckSchemaVersionsServletContextListener(List<ModelObjectSchema<?>> tableList) {
		this.tableList = tableList;
	}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		final Map<String, Integer> schemaVersions = new HashMap<String, Integer>();
		try {
			Database.getInstance().databaseRun(new AbstractDatabaseRunnableNoAuthException<Boolean>() {
				@Override
				public Boolean run(Connection conn) throws SQLException {
					PreparedStatement stmt = prepareStatement(conn, "select * from cc_schema_version");
					ResultSet resultSet = executeQuery(stmt);
					while (resultSet.next()) {
						String tableName = resultSet.getString(1);
						int schemaVersion = resultSet.getInt(2);
						schemaVersions.put(tableName, schemaVersion);
					}
					return true;
				}
				@Override
				public String getDescription() {
					return " check database table schema versions";
				}
			});
		} catch (PersistenceException e) {
			InitErrorList.instance().addError(
					"Error checking schema versions: " +
							e.getMessage() +
					": check database configuration");
			return;
		}

		// Check schema versions
		for (ModelObjectSchema<?> schema : tableList) {
			Integer dbSchemaVersion = schemaVersions.get(schema.getDbTableName());
			if (dbSchemaVersion == null) {
				InitErrorList.instance().addError(
						"No schema version found for table " + schema.getDbTableName() +
						": cc_schema_version table is incomplete");
			} else if (dbSchemaVersion.intValue() != schema.getVersion()) {
				InitErrorList.instance().addError(
						"Database table " +
						schema.getDbTableName() +
						" is out of date: run java -jar cloudcoderApp.jar migratedb");
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
