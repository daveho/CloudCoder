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

import java.util.HashMap;
import java.util.Map;

/**
 * Database of {@link HostConfig}s specifying how the {@link LoadTester}
 * should connect to the webapp.
 * 
 * @author David Hovemeyer
 */
public class HostConfigDatabase {
	private static final Map<String, HostConfig> HOST_CONFIG_MAP = new HashMap<String, HostConfig>();
	static {
		HOST_CONFIG_MAP.put("default", new HostConfig("http", "localhost", 8081, "cloudcoder/cloudcoder"));
		HOST_CONFIG_MAP.put("ycp", new HostConfig("https", "cs.ycp.edu", 0, "cloudcoder/cloudcoder"));
		HOST_CONFIG_MAP.put("lobsang", new HostConfig("http", "172.31.15.49", 8081, "cloudcoder/cloudcoder"));
	}
	
	public static HostConfig forName(String name) {
		HostConfig result = HOST_CONFIG_MAP.get(name);
		if (result == null) {
			throw new IllegalArgumentException("Unknown host config: " + name);
		}
		return result;
	}
}
