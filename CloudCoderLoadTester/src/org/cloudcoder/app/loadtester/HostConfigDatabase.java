package org.cloudcoder.app.loadtester;

import java.util.HashMap;
import java.util.Map;

public class HostConfigDatabase {
	private static final Map<String, HostConfig> HOST_CONFIG_MAP = new HashMap<String, HostConfig>();
	static {
		HOST_CONFIG_MAP.put("default", new HostConfig("http", "localhost", 8081, "cloudcoder/cloudcoder"));
		HOST_CONFIG_MAP.put("ycp", new HostConfig("https", "cs.ycp.edu", 0, "cloudcoder/cloudcoder"));
	}
	
	public static HostConfig forName(String name) {
		HostConfig result = HOST_CONFIG_MAP.get(name);
		if (result == null) {
			throw new IllegalArgumentException("Unknown host config: " + name);
		}
		return result;
	}
}
