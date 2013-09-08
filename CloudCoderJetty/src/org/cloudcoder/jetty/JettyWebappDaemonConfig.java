// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.jetty;

import java.util.Properties;

/**
 * Extension of {@link JettyDaemonConfig} with additional methods
 * to configure a webapp being hosted by {@link JettyWebappDaemon}.
 */
public interface JettyWebappDaemonConfig extends JettyDaemonConfig {
	/**
	 * @return the context path on which the embedded webapp should be
	 *         made available (e.g., "/cloudcoder")
	 */
	public String getContext();
	
	/**
	 * @return the path of the embedded resource containing the webapp,
	 *         e.g., "/war" if the webapp is embedded in the codebase in a
	 *         directory called "/war"; the string returned <em>must</em>
	 *         start with a slash ("/") character
	 */
	public String getWebappResourcePath();
	
	/**
	 * @return configuration {@link Properties} to override context parameters in the
	 *         webapp's web.xml; null if the webapp's context parameters should
	 *         be used as-is
	 */
	public Properties getContextParamOverrides();
}
