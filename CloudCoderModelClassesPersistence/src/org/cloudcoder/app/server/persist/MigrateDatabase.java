// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2017, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2017, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.server.persist.util.SchemaUtil;
import org.cloudcoder.app.shared.model.ModelObjectSchema;

public class MigrateDatabase {

	protected static void migrateDatabase(ModelObjectSchema<?>[] tables,
			String appName, String dbConfigPrefix)
			throws ClassNotFoundException, IOException, SQLException {
		System.out.println("Migrate the " + appName + " database to the latest schema.");
		System.out.println("Important: make sure " + appName + " is not currently running!");
		
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		
		Class.forName(JDBCDatabase.JDBC_DRIVER_CLASS);
		Properties config = DBUtil.getConfigProperties();
		Connection conn = DBUtil.connectToDatabase(config, dbConfigPrefix);
	
		boolean created = SchemaUtil.createSchemaVersionTableIfNeeded(conn, tables);
		
		if (created) {
			System.out.println("Warning: I just created the cc_schema_version table.");
			System.out.println("That means the schema versions of your tables are not known!");
			System.out.print("Continue (unsafe unless you're really sure this is OK)? (yes/no) ");
			String confirm = keyboard.nextLine();
			if (!confirm.trim().toLowerCase().equals("yes")) {
				return;
			}
		}
	
		int numMigrated = 0;
		for (ModelObjectSchema<?> table : tables) {
			int dbSchemaVersion = SchemaUtil.getDbSchemaVersion(conn, table);
			if (dbSchemaVersion != table.getVersion()) {
				System.out.print("Migrating " + table.getDbTableName() + " to version " + table.getVersion() + "...");
				System.out.flush();
				SchemaUtil.migrateTable(conn, table);
				System.out.println("done");
				numMigrated++;
			}
		}
		
		if (numMigrated == 0) {
			System.out.println("Your database is already up to date");
		} else {
			System.out.println("Successfully migrated " + numMigrated + " table(s)");
		}
	}

}
