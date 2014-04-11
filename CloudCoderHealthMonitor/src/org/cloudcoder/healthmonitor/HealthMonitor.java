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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

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
	private static final int CHECK_INTERVAL_SEC =
			Integer.getInteger("cloudcoder.healthmonitor.checkIntervalSec", 5*60);

	// If we see a submission queue size spike greater than this threshold
	// then report it.  TODO: make this configurable.
	private static final int SUBMISSION_QUEUE_DANGER_THRESHOLD = 15;
	
	// If an instance has been offline for more than this many seconds,
	// send an email report.  TODO: make this configurable.
	private static final long OFFLINE_INSTANCE_RESEND_INTERVAL_SEC =
			Integer.getInteger("cloudcoder.healthmonitor.offlineInstanceResendIntervalSec", 2*60*60);
	
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
	
	/**
	 * Object representing the last known status, last report timestamp,
	 * and unhealthyTimestamp for an instance.
	 */
	private static class Info {
		final Status status;
		final long lastReportTimestamp;
		final long unhealthyTimestamp;  // when the instance was found to be unhealthy
		
		Info(Status status, long lastReportTimestamp, long unhealthyTimestamp) {
			this.status = status;
			this.lastReportTimestamp = lastReportTimestamp;
			this.unhealthyTimestamp = unhealthyTimestamp;
		}
	}
	
	/**
	 * Information about an instance including the {@link Entry}
	 * representing its current status and the {@link Info} representing
	 * its last known status and report time.
	 */
	private static class ReportItem {
		final Entry entry;
		final Info info;
		
		public ReportItem(Entry entry, Info info) {
			this.entry = entry;
			this.info = info;
		}
		
		boolean statusChange() {
			return entry.status != info.status;
		}
		
		boolean goodNews() {
			return statusChange() && entry.status == Status.HEALTHY;
		}
		
		boolean badNews() {
			if (statusChange() && info.status == Status.HEALTHY) {
				return true;
			}
			
			// Instances that have been offline a long time are bad news,
			// even if there is no status change.
			if (entry.status != Status.HEALTHY) {
				long timeElapsedSinceLastReport = System.currentTimeMillis() - info.lastReportTimestamp;
				if (timeElapsedSinceLastReport > OFFLINE_INSTANCE_RESEND_INTERVAL_SEC*1000L) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	@Override
	public void run() {
		Map<String, Info> infoMap = new HashMap<String, Info>();
		
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
				
				// Update statuses and send email if appropriate
				update(report, infoMap);
			} catch (InterruptedException e) {
				logger.info("HealthMonitor interrupted (shutdown requested?)");
			}
		}
	}

	/**
	 * Check a webapp instance to see whether or not it is healthy,
	 * creating an {@link Entry}.
	 *   
	 * @param instance the webapp instance to check
	 * @return the {@link Entry} describing the health of the instance
	 */
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
		long timestamp = System.currentTimeMillis();
		try {
			HttpResponse response = client.execute(request);
			String responseBody = EntityUtils.toString(response.getEntity());
			Object responseObj = new JSONParser().parse(responseBody);
			HealthData healthData = new HealthData();
			JSONConversion.convertJSONToModelObject(responseObj, healthData, HealthData.SCHEMA);
			
			// Create a report entry.
			if (healthData.getNumConnectedBuilderThreads() == 0) {
				result = new Entry(instance, Status.NO_BUILDER_THREADS, timestamp);
				logger.info("Unhealthy instance {} detected: {}", instance, Status.NO_BUILDER_THREADS);
			} else if (healthData.getSubmissionQueueSizeMaxLastFiveMinutes() >= SUBMISSION_QUEUE_DANGER_THRESHOLD) {
				result = new Entry(instance, Status.EXCESSIVE_LOAD, timestamp);
				logger.info("Unhealthy instance {} detected: {}", instance, Status.EXCESSIVE_LOAD);
			} else {
				// Woo-hoo, everything looks fine.
				result = new Entry(instance, Status.HEALTHY, timestamp);
				logger.debug("Instance {} is healthy", instance);
			}
		} catch (Exception e) {
			logger.info("Error connecting to instance " + instance + ": " + e.getMessage(), e);
			result = new Entry(instance, Status.CANNOT_CONNECT, timestamp);
		} finally {
			client.getConnectionManager().shutdown();
		}
		
		return result;
	}

	/**
	 * Based on new information (the {@link HealthMonitorReport}) and
	 * current information (the info map, which keeps track of the
	 * last known status for each monitored instance), see whether
	 * any instance statuses have changed, and send email as appropriate.
	 * 
	 * @param report   the {@link HealthMonitorReport} representing current statuses
	 * @param infoMap  the map of instances to previously-known statuses
	 */
	private void update(HealthMonitorReport report, Map<String, Info> infoMap) {
		// Generate ReportItems
		List<ReportItem> reportItems = new ArrayList<ReportItem>();
		for (Entry entry : report.getEntryList()) {
			Info info = infoMap.get(entry.instance);
			if (info == null) {
				// no previous status: pretend the instance was fine
				info = new Info(Status.HEALTHY, 0L, -1L);
			}
			reportItems.add(new ReportItem(entry, info));
		}
		
		// See if any statuses have changed
		boolean goodNews = false;
		boolean badNews = false;
		for (ReportItem item : reportItems) {
			if (item.goodNews()) {
				goodNews = true;
			}
			if (item.badNews()) {
				badNews = true;
			}
		}
		
		if (goodNews || badNews) {
			// Generate email report
			try {
				Session session = createMailSession(config);
				
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(config.getReportEmailAddress()));
				message.addRecipient(RecipientType.TO, new InternetAddress(config.getReportEmailAddress()));
				message.setSubject("CloudCoder health monitor report");
				
				StringBuilder body = new StringBuilder();
				body.append("<h1>CloudCoder health monitor report</h1>\n");
				
				if (badNews) {
					body.append("<h2>Unhealthy instances</h2>\n");
					body.append("<ul>\n");
					for (ReportItem item : reportItems) {
						if (item.badNews()) {
							appendReportItem(body, item, infoMap);
						}
					}
					body.append("</ul>\n");
				}
				
				if (goodNews) {
					body.append("<h2>Healthy instances (back on line)</h2>\n");
					body.append("<ul>\n");
					for (ReportItem item : reportItems) {
						if (item.goodNews()) {
							appendReportItem(body, item, infoMap);
						}
					}
					body.append("</ul>\n");
				}
				
				message.setContent(body.toString(), "text/html");
				
				Transport.send(message);
			} catch (Exception e) {
				// This is bad
				logger.error("Could not send report email!", e);
			}
		}
	}

	/**
	 * Append a report item to a list in the body of a report email.
	 * 
	 * @param body     StringBuilder being used to build the body of the report email
	 * @param item     the report item to append
	 * @param infoMap  map of instances to Info objects recording current status;
	 *                 the lastReportTimestamp for the instance will be updated
	 */
	private void appendReportItem(StringBuilder body, ReportItem item, Map<String, Info> infoMap) {
		body.append("<li>");
		body.append(item.entry.instance);
		body.append(": ");
		body.append(item.entry.status);
		body.append(" at ");
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
		body.append(fmt.format(new Date(item.entry.timestamp)));

		long now = System.currentTimeMillis();
		
		// Determine updated unhealthyTimestamp
		long unhealthyTimestamp;
		if (item.entry.status == Status.HEALTHY) {
			// Instance is healthy, reset unhealthyTimestamp
			unhealthyTimestamp = -1L;
		} else {
			if (item.info.status == Status.HEALTHY) {
				// Instance just went offline
				unhealthyTimestamp = now;
			} else {
				// Instance remains offline
				unhealthyTimestamp = item.info.unhealthyTimestamp;
			}
		}
		
		if (item.badNews() && item.info.status != Status.HEALTHY) {
			body.append(" (unhealthy since");
			body.append(fmt.format(new Date(unhealthyTimestamp)));
			body.append(")");
		}
		
		body.append("</li>\n");
		
		// Since a report has been generated, update the Info for the instance
		Info updatedInfo = new Info(item.entry.status, now, unhealthyTimestamp);
		infoMap.put(item.entry.instance, updatedInfo);
	}

	/**
	 * Create a mail Session based on information in the
	 * given {@link HealthMonitorConfig}.
	 * 
	 * @param config the {@link HealthMonitorConfig}
	 * @return the mail Session
	 */
	private Session createMailSession(HealthMonitorConfig config) {
		final PasswordAuthentication passwordAuthentication =
				new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
		
		Properties properties = new Properties();
		properties.putAll(System.getProperties());
		properties.setProperty("mail.smtp.submitter", passwordAuthentication.getUserName());
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.host", config.getSmtpServer());
		properties.setProperty("mail.smtp.port", String.valueOf(config.getSmtpPort()));
		properties.setProperty("mail.smtp.starttls.enable", String.valueOf(config.isSmtpUseTLS()));
		
		return Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return passwordAuthentication;
			}
		});
	}
}
