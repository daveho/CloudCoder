// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014 Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014 David H. Hovemeyer <dhovemey@ycp.edu>
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

import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.ModelObjectSchema;

/**
 * Checker to check whether the tables in the CloudCoder database
 * are at the correct versions relative to the model object classes.
 * 
 * @author David Hovemeyer
 */
public class SchemaVersionChecker {
	/**
	 * Callback interface for reporting an error.
	 */
	public interface Reporter {
		/**
		 * Called to report a general error such as a problem connecting
		 * to the database.
		 * 
		 * @param error the error
		 */
		void reportGeneralError(String error);

		/**
		 * Called to report that the database does not contain a schema version number
		 * for the given table.
		 * 
		 * @param schema the table
		 */
		void reportMissingSchemaVersion(ModelObjectSchema<?> schema);

		/**
		 * Called to report that the schema version number of a database table
		 * does not match the model object class schema version number.
		 * 
		 * @param schema                the model object class schema
		 * @param dbTableSchemaVersion  the database table schema version number
		 */
		void reportTableWrongVersion(ModelObjectSchema<?> schema, int dbTableSchemaVersion);
	}
	
	/**
	 * Check the database to see if the database table schema version numbers
	 * correctly match the corresponding model class schema version numbers.
	 * 
	 * @param tableList list of model classes/database tables to check
	 * @param reporter reporter used to report errors
	 */
	public void check(List<ModelObjectSchema<?>> tableList, Reporter reporter) {
		try {
			// Get schema versions
			Map<String, Integer> schemaVersions = Database.getInstance().getSchemaVersions();
			
			// Check schema versions
			for (ModelObjectSchema<?> schema : tableList) {
				Integer dbSchemaVersion = schemaVersions.get(schema.getDbTableName());
				if (dbSchemaVersion == null) {
					reporter.reportMissingSchemaVersion(schema);
				} else if (dbSchemaVersion.intValue() != schema.getVersion()) {
					reporter.reportTableWrongVersion(schema, dbSchemaVersion.intValue());
				}
			}
		} catch (PersistenceException e) {
			reporter.reportGeneralError("Error checking schema versions: " +
					e.getMessage() +
					": check database configuration");
			return;
		}
	}
}
