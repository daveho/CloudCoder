package org.cloudcoder.app.loadtester;

import java.io.IOException;
import java.util.Properties;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.JDBCDatabaseConfig;
import org.cloudcoder.app.server.persist.util.DBUtil;

public class PrepareDatabaseForLoadTesting {
	public static void main(String[] args) throws IOException {
		final Properties config = DBUtil.getConfigProperties();
		JDBCDatabaseConfig.createFromProperties(config);
		
		//Database.getInstance().
	}
}
