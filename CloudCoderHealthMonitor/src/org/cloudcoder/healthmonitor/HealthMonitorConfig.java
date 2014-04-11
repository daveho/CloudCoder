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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.cloudcoder.daemon.IOUtil;

/**
 * Health monitor configuration.
 * 
 * @author David Hovemeyer
 */
public class HealthMonitorConfig implements Cloneable {
	private List<String> webappInstanceList;
	private String reportEmailAddress;
	private String smtpUsername;
	private String smtpPassword;
	private String smtpServer;
	private int smtpPort;
	private boolean smtpUseTLS;
	
	/**
	 * Constructor.
	 */
	public HealthMonitorConfig() {
		webappInstanceList = new ArrayList<String>();
	}
	
	/**
	 * Set the list of webapp instance host names.
	 * 
	 * @param webappInstanceList list of webapp instance hostnames
	 */
	public void setWebappInstanceList(List<String> webappInstanceList) {
		this.webappInstanceList.clear();
		this.webappInstanceList.addAll(webappInstanceList);
	}
	
	/**
	 * @return the list of webapp instance hostnames
	 */
	public List<String> getWebappInstanceList() {
		return Collections.unmodifiableList(webappInstanceList);
	}
	
	/**
	 * Add a webapp instance to monitor.
	 * 
	 * @param instance the webapp instance to monitor
	 */
	public void addWebappInstance(String instance) {
		webappInstanceList.add(instance);
	}
	
	/**
	 * @return the email address to which to report unhealthy webapp instances
	 */
	public String getReportEmailAddress() {
		return reportEmailAddress;
	}
	
	/**
	 * Set the report email address.
	 * 
	 * @param reportEmailAddress the report email address to set
	 */
	public void setReportEmailAddress(String reportEmailAddress) {
		this.reportEmailAddress = reportEmailAddress;
	}
	
	/**
	 * @return the SMTP username to use when sending email
	 */
	public String getSmtpUsername() {
		return smtpUsername;
	}
	
	/**
	 * Set the SMTP username.
	 * 
	 * @param smtpUsername the SMTP username to set
	 */
	public void setSmtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
	}
	
	/**
	 * @return the SMTP password to use when sending email
	 */
	public String getSmtpPassword() {
		return smtpPassword;
	}
	
	/**
	 * Set the SMTP password.
	 * 
	 * @param smtpPassword the SMTP password to set
	 */
	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}
	
	/**
	 * @return the SMTP server to use when sending email
	 */
	public String getSmtpServer() {
		return smtpServer;
	}
	
	/**
	 * Set the SMTP server.
	 * 
	 * @param smtpServer the SMTP server to set.
	 */
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}
	
	/**
	 * @return the SMTP port to use when sending email
	 */
	public int getSmtpPort() {
		return smtpPort;
	}
	
	/**
	 * Set the SMTP port.
	 * 
	 * @param smtpPort the SMTP port to set.
	 */
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}
	
	/**
	 * Set whether SMTP should use TLS.
	 * 
	 * @param smtpUseTLS true if SMTP should use TLS, false if not 
	 */
	public void setSmtpUseTLS(boolean smtpUseTLS) {
		this.smtpUseTLS = smtpUseTLS;
	}
	
	/**
	 * @return true if SMTP should use TLS, false if not
	 */
	public boolean isSmtpUseTLS() {
		return smtpUseTLS;
	}
	
	/**
	 * Load from a reader reading a properties file.
	 * 
	 * @param reader a Reader reading the properties file
	 * @throws IOException
	 */
	public void load(Reader reader) throws IOException {
		Properties props = new Properties();
		props.load(reader);
		load(props);
	}

	/**
	 * Load from {@link Properties}.
	 * 
	 * @param props the properties to load from
	 */
	public void load(Properties props) {
		String instances = getRequiredProperty(props, "cloudcoder.healthmonitor.instances");
		StringTokenizer tok = new StringTokenizer(instances, ",");
		while (tok.hasMoreTokens()) {
			webappInstanceList.add(tok.nextToken());
		}
		reportEmailAddress = getRequiredProperty(props, "cloudcoder.healthmonitor.reportEmail");
		smtpUsername = getRequiredProperty(props, "cloudcoder.healthmonitor.smtp.user");
		smtpPassword = getRequiredProperty(props, "cloudcoder.healthmonitor.smtp.passwd");
		smtpServer = getRequiredProperty(props, "cloudcoder.healthmonitor.smtp.host");
		smtpPort = Integer.parseInt(getRequiredProperty(props, "cloudcoder.healthmonitor.smtp.port"));
		smtpUseTLS = Boolean.parseBoolean(getRequiredProperty(props, "cloudcoder.healthmonitor.smtp.useTLS"));
	}
	
	/**
	 * Load from a properties file.
	 * 
	 * @param fileName filename of the properties file
	 * @throws IOException
	 */
	public void load(String fileName) throws IOException {
		Reader reader = new BufferedReader(new FileReader(fileName));
		try {
			load(fileName);
		} finally {
			IOUtil.closeQuietly(reader);
		}
	}
	
	@Override
	protected HealthMonitorConfig clone() {
		try {
			HealthMonitorConfig dup = (HealthMonitorConfig) super.clone(); // shallow copy
			dup.webappInstanceList = new ArrayList<String>();
			dup.webappInstanceList.addAll(this.webappInstanceList); // deep copy
			return dup;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	private String getRequiredProperty(Properties props, String propName) {
		String instances = props.getProperty(propName);
		if (instances == null) {
			throw new IllegalArgumentException("Missing cloudcoder.healthmonitor.instances property");
		}
		return instances;
	}
}
