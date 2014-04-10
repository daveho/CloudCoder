// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.healthmonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A report produced by the {@link HealthMonitor}.
 * Specifies a status for each monitored webapp instance.
 * 
 * @author David Hovemeyer
 */
public class HealthMonitorReport {
	/**
	 * Status of an instance.
	 */
	public enum Status {
		/** Instance appears to be healthy. */
		HEALTHY,
		
		/** Cannot conncet to the instance. */
		CANNOT_CONNECT,
		
		/** No builder threads are connected to the instance. */
		NO_BUILDER_THREADS,
		
		/** The instance appears to be under excessive load. */
		EXCESSIVE_LOAD,
	}
	
	/**
	 * An entry in the report.
	 */
	public static class Entry {
		public final String instance;
		public final Status status;
		public final long timestamp;
		
		public Entry(String instance, Status status, long timestamp) {
			this.instance = instance;
			this.status = status;
			this.timestamp = timestamp;
		}
	}
	
	private List<Entry> entryList;
	
	/**
	 * Constructor.
	 */
	public HealthMonitorReport() {
		this.entryList = new ArrayList<Entry>();
	}
	
	/**
	 * Add an entry.
	 * 
	 * @param entry the entry to add
	 */
	public void addEntry(Entry entry) {
		entryList.add(entry);
	}
	
	/**
	 * @return list of {@link Entry}s
	 */
	public List<Entry> getEntryList() {
		return Collections.unmodifiableList(entryList);
	}
	
	/**
	 * @return true if any instance has an unhealthy status, false
	 *         if all instances appear to be healthy
	 */
	public boolean hasUnhealthyStatus() {
		for (Entry entry : entryList) {
			if (entry.status != Status.HEALTHY) {
				return true;
			}
		}
		return false;
	}
}
