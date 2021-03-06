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

package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.ConfigurationSetting;
import org.cloudcoder.app.shared.model.ConfigurationSettingName;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * RPC service for accessing and updating {@link ConfigurationSetting}s.
 *
 * @author David Hovemeyer
 */
@RemoteServiceRelativePath("configurationSetting")
public interface ConfigurationSettingService extends RemoteService {
	/**
	 * Get the value of a specified {@link ConfigurationSetting}.
	 * 
	 * @param name the {@link ConfigurationSettingName}
	 * @return the value of the named {@link ConfigurationSetting}
	 */
	public String getConfigurationSettingValue(ConfigurationSettingName name);
	
	/**
	 * Update specified {@link ConfigurationSetting}s.
	 * 
	 * @param settings the {@link ConfigurationSetting}s to update.
	 * @return true if configuration settings updated successfully, false otherwise
	 */
	public boolean updateConfigurationSettings(ConfigurationSetting[] settings) throws CloudCoderAuthenticationException;
}
