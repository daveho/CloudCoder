package org.cloudcoder.app.server.persist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.shared.model.ModelObjectSchema;

public class MigrateWebappDatabase {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		ConfigurationUtil.configureLog4j();
		
		try {
			doMigrateWebappDatabase();
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void doMigrateWebappDatabase() throws IOException,
			SQLException, ClassNotFoundException {
		System.out.println("Migrate the CloudCoder database to the latest schema.");
		System.out.println("Important: make sure CloudCoder is not currently running!");
		
		Scanner keyboard = new Scanner(System.in);
		
		Class.forName("com.mysql.jdbc.Driver");
		Properties config = DBUtil.getConfigProperties();
		Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");

		boolean created = SchemaUtil.createSchemaVersionTableIfNeeded(conn, CreateWebappDatabase.TABLES);
		
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
		for (ModelObjectSchema<?> table : CreateWebappDatabase.TABLES) {
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
