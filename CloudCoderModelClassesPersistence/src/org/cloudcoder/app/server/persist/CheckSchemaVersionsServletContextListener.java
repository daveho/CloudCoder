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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cloudcoder.app.shared.model.ModelObjectSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ServletContextListener to check that the schema versions of
 * the database tables match the schema versions of the model
 * object classes.  A {@link SchemaVersionChecker} object is used to
 * do the actual checking.  If there is a mismatch, we report it by
 * adding an error to the {@link InitErrorList} singleton. 
 * 
 * @author David Hovemeyer
 */
public abstract class CheckSchemaVersionsServletContextListener implements ServletContextListener {
	private static Logger logger = LoggerFactory.getLogger(CheckSchemaVersionsServletContextListener.class);

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
		SchemaVersionChecker checker = new SchemaVersionChecker();
		checker.check(tableList, new SchemaVersionChecker.Reporter() {
			@Override
			public void reportGeneralError(String error) {
				CheckSchemaVersionsServletContextListener.this.report(error);
			}
			
			@Override
			public void reportMissingSchemaVersion(ModelObjectSchema<?> table) {
				CheckSchemaVersionsServletContextListener.this.report(
						"No schema version found for table " + table.getDbTableName() +
						": cc_schema_version table is incomplete");
			}
			
			@Override
			public void reportTableWrongVersion(ModelObjectSchema<?> schema,
					int dbTableSchemaVersion) {
				CheckSchemaVersionsServletContextListener.this.report(
						"Database table " +
						schema.getDbTableName() +
						" is out of date: run java -jar cloudcoderApp.jar migratedb");
			}
		});
	}

	private void report(String error) {
		InitErrorList.instance().addError(error);
		logger.error(error);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
