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

package org.cloudcoder.app.server.model;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Singleton storing health data for the CloudCoder webapp.
 * This can be exposed via a servlet or other monitoring API.
 * 
 * @author David Hovemeyer
 */
public class HealthData {
	
	private static final HealthData theInstance = new HealthData();
	
	/**
	 * Get the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static HealthData getInstance() {
		return theInstance;
	}
	
	private static class SubmissionQueueSizeSample {
		public SubmissionQueueSizeSample(int queueSize, long timestamp) {
			this.queueSize = queueSize;
			this.timestamp = timestamp;
		}
		int queueSize;
		long timestamp;
	}
	
	private LinkedList<SubmissionQueueSizeSample> submissionQueueSizeSampleList;
	private volatile int submissionQueueSizeCurrent;
	private volatile int submissionQueueSizeMaxLastFiveMinutes;
	
	private HealthData() {
		this.submissionQueueSizeSampleList = new LinkedList<SubmissionQueueSizeSample>();
	}
	
	/**
	 * Update the current submission queue size.
	 * The maximum submission queue size for the last 5 minutes is also
	 * updated.  Note that no synchronization is done, so this method
	 * should only be called from a single thread.
	 * 
	 * @param submissionQueueSize
	 */
	public void updateSubmissionQueueSize(int submissionQueueSize) {
		long now = System.currentTimeMillis();
		
		submissionQueueSizeSampleList.add(new SubmissionQueueSizeSample(submissionQueueSize, now));
		
		this.submissionQueueSizeCurrent = submissionQueueSize;
		
		int max = 0;
		
		// Purge all samples older than 5 minutes
		for (Iterator<SubmissionQueueSizeSample> i = submissionQueueSizeSampleList.iterator(); i.hasNext(); ) {
			SubmissionQueueSizeSample sample = i.next();
			if (now - sample.timestamp > 5L*60*1000) {
				i.remove();
			} else if (sample.queueSize > max) {
				max = sample.queueSize;
			}
		}
		
		this.submissionQueueSizeMaxLastFiveMinutes = max;
	}
	
	/**
	 * Get the current submission queue size.
	 * 
	 * @return the current submission queue size
	 */
	public int getSubmissionQueueSizeCurrent() {
		return submissionQueueSizeCurrent;
	}
	
	/**
	 * Get the maximum submission queue size in the last 5 minutes.
	 * 
	 * @return the maximum submission queue size in the last 5 minutes
	 */
	public int getSubmissionQueueSizeMaxLastFiveMinutes() {
		return submissionQueueSizeMaxLastFiveMinutes;
	}
}
