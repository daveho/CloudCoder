// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.shared.model.ConfigurationSetting;

/**
 * Update specified {@link ConfigurationSetting}s.
 * 
 * @author David Hovemeyer
 */
public class UpdateConfigurationSettings extends AbstractDatabaseRunnableNoAuthException<Boolean> {

	private ConfigurationSetting[] settings;

	public UpdateConfigurationSettings(ConfigurationSetting[] settings) {
		this.settings = settings;
	}

	@Override
	public String getDescription() {
		return " update configuration settings";
	}

	@Override
	public Boolean run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"update cc_configuration_settings set value = ? where name = ?"
		);
		
		for (ConfigurationSetting setting : settings) {
			stmt.setString(1, setting.getValue());
			stmt.setString(2, setting.getName());
			stmt.addBatch();
		}
		
		stmt.executeBatch();
		
		return true;
	}

}
