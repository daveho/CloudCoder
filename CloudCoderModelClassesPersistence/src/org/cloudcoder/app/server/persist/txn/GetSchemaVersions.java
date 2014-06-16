package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;

public class GetSchemaVersions extends AbstractDatabaseRunnableNoAuthException<Map<String, Integer>> {

	@Override
	public Map<String, Integer> run(Connection conn) throws SQLException {
		Map<String, Integer> schemaVersions = new HashMap<String, Integer>();
		PreparedStatement stmt = prepareStatement(conn, "select * from cc_schema_version");
		ResultSet resultSet = executeQuery(stmt);
		while (resultSet.next()) {
			String tableName = resultSet.getString(1);
			int schemaVersion = resultSet.getInt(2);
			schemaVersions.put(tableName, schemaVersion);
		}
		return schemaVersions;
	}

	@Override
	public String getDescription() {
		return " getting schema version numbers";
	}

}
