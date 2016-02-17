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

package org.cloudcoder.app.server.rpc;

import org.cloudcoder.app.client.rpc.ConfigurationSettingService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConfigurationSettingServiceImpl extends RemoteServiceServlet
		implements ConfigurationSettingService {
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationSettingServiceImpl.class);

	@Override
	public String getConfigurationSettingValue(ConfigurationSettingName name) {
		ConfigurationSetting configurationSetting = Database.getInstance().getConfigurationSetting(name);
		return configurationSetting != null ? configurationSetting.getValue() : null;
	}
	
	@Override
	public boolean updateConfigurationSettings(ConfigurationSetting[] settings) throws CloudCoderAuthenticationException {
		User authenticatedUser = ServletUtil.checkClientIsAuthenticated(
				getThreadLocalRequest(), ConfigurationSettingServiceImpl.class);
		if (!authenticatedUser.isSuperuser()) {
			logger.warn("Attempt by non-superuser {} to update configuration settings", authenticatedUser.getUsername());;
			return false;
		}
		
		return Database.getInstance().updateConfigurationSettings(settings);
	}

}
