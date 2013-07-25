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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store timing statistics.
 * Collects data sets consisting of long values (typically,
 * elapsed times in milliseconds), organized by arbitrary
 * keys.
 * 
 * @author David Hovemeyer
 */
public class StatsCollector {
	private Object lock;
	private Map<Object, List<Long>> dataMap;
	
	/**
	 * Constructor.
	 */
	public StatsCollector() {
		lock = new Object();
		dataMap = new HashMap<Object, List<Long>>();
	}
	
	/**
	 * Add a statistic.  This method is thread safe.
	 * 
	 * @param key    the key
	 * @param datum  the datum
	 */
	public void addStat(Object key, long datum) {
		synchronized (lock) {
			List<Long> list = dataMap.get(key);
			if (list == null) {
				list = new ArrayList<Long>();
				dataMap.put(key, list);
			}
			list.add(datum);
		}
	}

	/**
	 * Get keys in sorted order (sorted lexicographically by their
	 * string representation.)
	 * 
	 * @return sorted keys
	 */
	public List<Object> getSortedKeys() {
		List<Object> keys = new ArrayList<Object>();
		synchronized (lock) {
			keys.addAll(dataMap.keySet());
		}
		Collections.sort(keys, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		return keys;
	}
	
	/**
	 * Get dataset for given key.
	 * 
	 * @param key the key
	 * @return the dataset for the given key
	 */
	public List<Long> getData(Object key) {
		synchronized (lock) {
			return dataMap.get(key);
		}
	}
}
