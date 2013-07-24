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
		keys.addAll(dataMap.keySet());
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
		return dataMap.get(key);
	}
}
