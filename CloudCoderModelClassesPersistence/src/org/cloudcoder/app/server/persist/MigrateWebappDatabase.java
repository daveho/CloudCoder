package org.cloudcoder.app.server.persist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.cloudcoder.app.shared.model.ModelObjectSchema;

public class MigrateWebappDatabase {
	public static void main(String[] args) throws IOException {
		configureLog4j();
		
		try {
			doMigrateWebappDatabase();
		} catch (SQLException e) {
			System.err.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void doMigrateWebappDatabase() throws IOException,
			SQLException {
		System.out.println("Migrate the CloudCoder database to the latest schema");
		
		Scanner keyboard = new Scanner(System.in);
		
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
		
		for (ModelObjectSchema<?> table : CreateWebappDatabase.TABLES) {
			System.out.print("Migrating " + table.getDbTableName() + "...");
			System.out.flush();
			SchemaUtil.migrateTable(conn, table);
			System.out.println("OK!");
		}
		
		System.out.println("Done!");
	}

	private static void configureLog4j() {
		// See: http://robertmaldon.blogspot.com/2007/09/programmatically-configuring-log4j-and.html
		Logger rootLogger = Logger.getRootLogger();
		if (!rootLogger.getAllAppenders().hasMoreElements()) {
			// Set this to Level.DEBUG if there are problems running the migration
			rootLogger.setLevel(Level.WARN);
			rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));
		}
	}
}
