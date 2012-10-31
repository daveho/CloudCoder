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

package org.cloudcoder.importer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.cloudcoder.app.server.persist.JDBCDatabaseConfig;

/**
 * Base class for command-line tools which use the CloudCoder database.
 * 
 * @author David Hovemeyer
 */
public abstract class UsesDatabase {

	public UsesDatabase(String configPropertiesFileName) throws IOException {
		final Properties config = new Properties();
		FileReader fileReader = new FileReader(configPropertiesFileName);
		try {
			config.load(fileReader);
		} finally {
			fileReader.close();
		}
		JDBCDatabaseConfig.create(new JDBCDatabaseConfig.ConfigProperties() {
			@Override
			public String getUser() {
				return config.getProperty("cloudcoder.db.user");
			}

			@Override
			public String getPasswd() {
				return config.getProperty("cloudcoder.db.passwd");
			}

			@Override
			public String getDatabaseName() {
				return config.getProperty("cloudcoder.db.databaseName");
			}

			@Override
			public String getHost() {
				return config.getProperty("cloudcoder.db.host");
			}

			@Override
			public String getPortStr() {
				return config.getProperty("cloudcoder.db.portStr");
			}
			
		});
	}

	public abstract void run() throws Exception;
}
