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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.cloudcoder.app.shared.model.HealthData;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.healthmonitor.HealthMonitorReport.Entry;
import org.cloudcoder.healthmonitor.HealthMonitorReport.Status;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Health monitor agent.  Periodically contacts CloudCoder webapp
 * instances to ensure that they are responsive and that they
 * have builders connected to them.  Sends email if an instance
 * is found to be in an unhealthy state.
 * 
 * @author David Hovemeyer
 */
public class HealthMonitor implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(HealthMonitorConfig.class);
	
	// Check instances every 5 minutes
	private static final int CHECK_INTERVAL_SEC = Integer.getInteger("cloudcoder.healthmonitor.checkIntervalSec", 5*60);

	// If we see a submission queue size spike greater than this threshold
	// then report it.  TODO: make this configurable.
	private static final int SUBMISSION_QUEUE_DANGER_THRESHOLD = 15;
	
	private Object lock;
	private HealthMonitorConfig config;
	private volatile boolean shutdown;

	/**
	 * Constructor.
	 */
	public HealthMonitor() {
		lock = new Object();
	}
	
	/**
	 * Set the {@link HealthMonitorConfig} that specifies the webapp instances to
	 * monitor and the email address where problems should be reported.
	 * 
	 * @param config
	 */
	public void setConfig(HealthMonitorConfig config) {
		synchronized (lock) {
			this.config = config;
		}
	}
	
	/**
	 * Shut down the health monitor.  Note that the thread executing
	 * the health monitor should be interrupted to ensure timely shutdown.
	 * (Do that after calling this method.
	 */
	public void shutdown() {
		shutdown = true;
		// Caller is responsible for interrupting the thread
	}
	
	@Override
	public void run() {
		while (!shutdown) {
			try {
				Thread.sleep(CHECK_INTERVAL_SEC * 1000);
				
				// Make a copy of the config (the "real" config can be set
				// asynchronously)
				HealthMonitorConfig config;
				synchronized (lock) {
					config = this.config.clone();
				}
				
				// Check instances
				HealthMonitorReport report = new HealthMonitorReport();
				for (String instance : config.getWebappInstanceList()) {
					report.addEntry(checkInstance(instance));
				}
				
				if (report.hasUnhealthyStatus()) {
					logger.error("Unhealthy status detected!");
					for (Entry entry : report.getEntryList()) {
						logger.error("Instance {}, status={}", entry.instance, entry.status);
					}
					
					// TODO: send email!
				}
			} catch (InterruptedException e) {
				logger.info("HealthMonitor interrupted (shutdown requested?)");
			}
		}
	}

	private Entry checkInstance(String instance) {
		logger.debug("Checking instance {}", instance);
		
		// We expect that the instance is the base URL by which clients
		// connect to CloudCoder, e.g., "https://cloudcoder.org/demo".
		// We can use this URL to derive the URL fo the health servlet.
		StringBuilder buf = new StringBuilder();
		buf.append(instance);
		if (!instance.endsWith("/")) {
			buf.append("/");
		}
		buf.append("health");

		// Get the instance's health information
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(buf.toString());
		
		Entry result = null;
		try {
			HttpResponse response = client.execute(request);
			String responseBody = EntityUtils.toString(response.getEntity());
			Object responseObj = new JSONParser().parse(responseBody);
			HealthData healthData = new HealthData();
			JSONConversion.convertJSONToModelObject(responseObj, healthData, HealthData.SCHEMA);
			
			// Create a report entry.
			if (healthData.getNumConnectedBuilderThreads() == 0) {
				result = new Entry(instance, Status.NO_BUILDER_THREADS);
			} else if (healthData.getSubmissionQueueSizeMaxLastFiveMinutes() >= SUBMISSION_QUEUE_DANGER_THRESHOLD) {
				result = new Entry(instance, Status.EXCESSIVE_LOAD);
			} else {
				// Woo-hoo, everything looks fine.
				result = new Entry(instance, Status.HEALTHY);
				logger.debug("Instance {} is healthy", instance);
			}
		} catch (Exception e) {
			logger.error("Error connecting to instance " + instance + ": " + e.getMessage(), e);
			result = new Entry(instance, Status.CANNOT_CONNECT);
		} finally {
			client.getConnectionManager().shutdown();
		}
		
		return result;
	}

}
