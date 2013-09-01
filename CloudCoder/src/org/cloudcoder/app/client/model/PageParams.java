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

package org.cloudcoder.app.client.model;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

/**
 * Page parameters specified as part of a URL fragment.
 * These are specified in the form "key=value,key=value,...".
 * 
 * @author David Hovemeyer
 */
public class PageParams {
	private Map<String, String> map;
	
	/**
	 * Constructor.
	 * 
	 * @param params page parameters string
	 */
	public PageParams(String params) {
		map = parseParams(params);
	}
	
	/**
	 * Get the value corresponding to given key.
	 * 
	 * @param key the key
	 * @return the value associated with the key, or null if there
	 *         is no associated value
	 */
	public String get(String key) {
		return map.get(key);
	}
	
	/**
	 * Get the integer value corresponding to given key.
	 * 
	 * @param key the key
	 * @return the integer value associated with the key, or null
	 *         if there is no associated value, or if the associated
	 *         value is not an integer 
	 */
	public Integer getInt(String key) {
		try {
			String value = get(key);
			return value != null ? Integer.parseInt(value) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private static Map<String, String> parseParams(String params) {
		HashMap<String, String> map = new HashMap<String, String>();
		String[] pairs = params.split(",");
		for (String pair : pairs) {
			int eq = pair.indexOf('=');
			if (eq >= 0) {
				String key = pair.substring(0, eq);
				String value = pair.substring(eq+1);
				GWT.log("Page param: key=" + key + ",value=" + value);
				map.put(key, value);
			}
		}
		return map;
	}
}
