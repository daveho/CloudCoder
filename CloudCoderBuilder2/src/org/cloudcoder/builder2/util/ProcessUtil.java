// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2010-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;

import org.cloudcoder.daemon.IOUtil;

/**
 * Utility functions for running subprocesses.
 * 
 * @author David Hovemeyer
 */
public class ProcessUtil {
	/**
	 * Get array containing current environment variables
	 * in a form suitable for starting a subprocess.
	 * 
	 * @return array of environment variables
	 */
	public static String[] getEnvArray() {
		return getEnvArray(System.getenv());
	}

	/**
	 * Convert a map containing environment variables into
	 * an array suitable for starting a subprocess.
	 * 
	 * @return array of environment variables
	 */
	public static String[] getEnvArray(Map<String, String> envMap) {
		String[] result = new String[envMap.size()];
		int count = 0;
		for (Map.Entry<String, String> entry : envMap.entrySet()) {
			result[count++] = entry.getKey() + "=" + entry.getValue();
		}
		return result;
	}

	/**
	 * Read a resource and return it as a string.
	 * 
	 * @param resourceName the resource name
	 * @return the resource as a string
	 * @throws IOException
	 */
	public static String resourceToString(String resourceName) throws IOException {
		InputStream in = null;
		try {
			in = ProcessUtil.class.getClassLoader().getResourceAsStream(resourceName);
			InputStreamReader r = new InputStreamReader(in);
			StringWriter sw = new StringWriter();
			IOUtil.copy(r, sw);
			return sw.toString();
		} finally {
			IOUtil.closeQuietly(in);
		}
	}
}
