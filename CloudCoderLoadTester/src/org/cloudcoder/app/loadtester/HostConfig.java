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

package org.cloudcoder.app.loadtester;

/**
 * Configuration specifying how to connect to the webapp.
 * 
 * @author David Hovemeyer
 */
public class HostConfig {
	private String protocol;
	private String hostname;
	private int port;
	private String contextPath;

	/**
	 * Constructor.
	 * 
	 * @param protocol     the protocol, e.g., "https"
	 * @param hostname     the hostname
	 * @param port         the port: 0 if the default port for the protocol
	 * @param contextPath  the context path (where the webapp.nocache.js is located)
	 */
	public HostConfig(String protocol, String hostname, int port, String contextPath) {
		this.protocol = protocol;
		this.hostname = hostname;
		this.port = port;
		this.contextPath = contextPath;
	}
	
	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}
	
	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @return the port (0 if the default port should be used) 
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * @return the context path
	 */
	public String getContextPath() {
		return contextPath;
	}
}
