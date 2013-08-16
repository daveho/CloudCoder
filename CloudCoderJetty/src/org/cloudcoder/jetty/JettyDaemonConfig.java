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
 * Interface for information about how Jetty should be configured.
 */
public interface JettyDaemonConfig {
	/**
	 * @return the port on which Jetty should listen for connections
	 */
	public int getPort();
	
	/**
	 * @return true if Jetty should only allow connections from localhost
	 *         (for example, if Jetty is behind a reverse proxy server),
	 *         false if Jetty should allow connections from any network
	 */
	public boolean isLocalhostOnly();
	
	/**
	 * @return the {@link Properties} to be used to configure log4j
	 */
	public Properties getLog4jProperties();
	
	/**
	 * Get the number of threads that Jetty should use to handle requests.
	 * 
	 * @return number of threads
	 */
	public int getNumThreads();
}
