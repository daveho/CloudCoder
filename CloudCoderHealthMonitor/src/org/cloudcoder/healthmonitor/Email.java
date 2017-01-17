// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2017, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2017, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

/**
 * Utility methods for sending email.
 */
public class Email {
	/**
	 * Create a mail Session based on information in the
	 * given {@link HealthMonitorConfig}.
	 * 
	 * @param config the {@link HealthMonitorConfig}
	 * @return the mail Session
	 */
	static Session createMailSession(HealthMonitorConfig config) {
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
