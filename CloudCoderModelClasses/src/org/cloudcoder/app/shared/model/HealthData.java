// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.shared.model;

/**
 * Model object to represent health data for a CloudCoder webapp instance.
 * This can be exposed via a servlet or other monitoring API.
 * 
 * @author David Hovemeyer
 */
public class HealthData implements IModelObject<HealthData> {
	private int submissionQueueSizeCurrent;
	private int submissionQueueSizeMaxLastFiveMinutes;
	private int numConnectedBuilderThreads;
	
	public static final ModelObjectField<HealthData, Integer> SUBMISSION_QUEUE_SIZE_CURRENT = new ModelObjectField<HealthData, Integer>("submissionQueueSizeCurrent", Integer.class, 0) {
		public void set(HealthData obj, Integer value) { obj.setSubmissionQueueSizeCurrent(value); }
		public Integer get(HealthData obj) { return obj.getSubmissionQueueSizeCurrent(); }
	};
	
	public static final ModelObjectField<HealthData, Integer> SUBMISSION_QUEUE_SIZE_MAX_LAST_FIVE_MINUTES = new ModelObjectField<HealthData, Integer>("submissionQueueSizeMaxLastFiveMinutes", Integer.class, 0) {
		public void set(HealthData obj, Integer value) { obj.setSubmissionQueueSizeMaxLastFiveMinutes(value); }
		public Integer get(HealthData obj) { return obj.getSubmissionQueueSizeMaxLastFiveMinutes(); }
	};
	
	public static final ModelObjectField<HealthData, Integer> NUM_CONNECTED_BUILDER_THREADS = new ModelObjectField<HealthData, Integer>("numConnectedBuilderThreads", Integer.class, 0) {
		public void set(HealthData obj, Integer value) { obj.setNumConnectedBuilderThreads(value); }
		public Integer get(HealthData obj) { return obj.getNumConnectedBuilderThreads(); }
	};
	
	/**
	 * Model object fields (schema version 0).
	 */
	public static final ModelObjectSchema<HealthData> SCHEMA_V0 = new ModelObjectSchema<HealthData>("healthdata")
			.add(SUBMISSION_QUEUE_SIZE_CURRENT)
			.add(SUBMISSION_QUEUE_SIZE_MAX_LAST_FIVE_MINUTES)
			.add(NUM_CONNECTED_BUILDER_THREADS);
	
	/**
	 * Model object fields (current schema version).
	 */
	public static final ModelObjectSchema<HealthData> SCHEMA = SCHEMA_V0;
	
	/**
	 * Constructor.
	 */
	public HealthData() {
		
	}
	
	@Override
	public ModelObjectSchema<? super HealthData> getSchema() {
		return SCHEMA;
	}
	
	/**
	 * Set the current submission queue size.
	 * 
	 * @param submissionQueueSizeCurrent the current submission queue size to set
	 */
	public void setSubmissionQueueSizeCurrent(int submissionQueueSizeCurrent) {
		this.submissionQueueSizeCurrent = submissionQueueSizeCurrent;
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
	 * Set the maximum submission queue size in the last five minutes. 
	 * 
	 * @param submissionQueueSizeMaxLastFiveMinutes the max submission queue size in the last five minutes
	 */
	public void setSubmissionQueueSizeMaxLastFiveMinutes(int submissionQueueSizeMaxLastFiveMinutes) {
		this.submissionQueueSizeMaxLastFiveMinutes = submissionQueueSizeMaxLastFiveMinutes;
	}
	
	/**
	 * Get the maximum submission queue size in the last 5 minutes.
	 * 
	 * @return the maximum submission queue size in the last 5 minutes
	 */
	public int getSubmissionQueueSizeMaxLastFiveMinutes() {
		return submissionQueueSizeMaxLastFiveMinutes;
	}
	
	/**
	 * Set the number of connected builder threads.
	 * 
	 * @return the number of connected builder threads to set
	 */
	public int getNumConnectedBuilderThreads() {
		return numConnectedBuilderThreads;
	}
	
	/**
	 * Get the number of connected builder threads.
	 * 
	 * @param numConnectedBuilderThreads the number of connected builder threads
	 */
	public void setNumConnectedBuilderThreads(int numConnectedBuilderThreads) {
		this.numConnectedBuilderThreads = numConnectedBuilderThreads;
	}
}
