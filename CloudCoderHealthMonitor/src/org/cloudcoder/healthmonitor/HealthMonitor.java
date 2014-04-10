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

import java.sql.Date;
import java.text.SimpleDateFormat;
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
					//Session session = createMailSession(config);
					sendReportEmail(report, config);
				} else {
					logger.debug("All instances are healthy");
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
			} else if (healthData.getSubmissionQueueSizeMaxLastFiveMinutes() >= SUBMISSION_QUEUE_DANGER_THRESHOLD) {
				result = new Entry(instance, Status.EXCESSIVE_LOAD, timestamp);
			} else {
				// Woo-hoo, everything looks fine.
				result = new Entry(instance, Status.HEALTHY, timestamp);
				logger.debug("Instance {} is healthy", instance);
			}
		} catch (Exception e) {
			logger.error("Error connecting to instance " + instance + ": " + e.getMessage(), e);
			result = new Entry(instance, Status.CANNOT_CONNECT, timestamp);
		} finally {
			client.getConnectionManager().shutdown();
		}
		
		return result;
	}

	private void sendReportEmail(HealthMonitorReport report, HealthMonitorConfig config) {
		try {
			Session session = createMailSession(config);
	
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(config.getReportEmailAddress()));
			message.addRecipient(RecipientType.TO, new InternetAddress(config.getReportEmailAddress()));
			message.setSubject("CloudCoder health monitor failure report");
			
			StringBuilder body = new StringBuilder();
			body.append("<h1>CloudCoder health monitor failure report</h1>");
			body.append("<p>One or more CloudCoder webapp instances are unhealthy:</p>");
			body.append("<ul>");
			for (Entry entry : report.getEntryList()) {
				if (entry.status != Status.HEALTHY) {
					body.append("<li>");
					body.append(entry.instance);
					body.append(": ");
					body.append(entry.status);
					body.append(" at ");
					SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
					body.append(fmt.format(new Date(entry.timestamp)));
					body.append("</li>");
				}
			}
			
			message.setContent(body.toString(), "text/html");
			
			Transport.send(message);
		} catch (Exception e) {
			// This is bad
			logger.error("Error sending report email!", e);
		}
	}

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
