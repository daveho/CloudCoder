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

package org.cloudcoder.app.server.submitsvc.oop;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet context listener to create and destroy the
 * {@link OutOfProcessSubmitService} as necessary for a web application.
 * Gets the configuration automatically from the webapp's servlet
 * context init parameters.
 * 
 * @author David Hovemeyer
 */
public class OutOfProcessSubmitServiceServletContextListener implements ServletContextListener {
	private static Logger logger = LoggerFactory.getLogger(OutOfProcessSubmitServiceServletContextListener.class);
	
	private static String getContextParameter(ServletContext ctx, String propName, String defValue) {
		String value = ctx.getInitParameter(propName);
		if (value == null && defValue == null) {
			logger.error("Missing configuration property: {}", propName);
			throw new IllegalArgumentException("Missing configuration property: " + propName);
		}
		return value != null ? value : defValue;
	}

	private void setPropertyFromContextParameter(ServletContext ctx, Properties config, String propName, String defValue) {
		config.setProperty(propName, getContextParameter(ctx, propName, defValue));
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			// Create the singleton instance of OutOfProcessSubmitService
			OutOfProcessSubmitService svc = new OutOfProcessSubmitService();
			OutOfProcessSubmitService.setInstance(svc);
			
			ServletContext servletContext = event.getServletContext();
			
			// Use the servlet context parameters to initialized a Properties object
			Properties config = new Properties();
			setPropertyFromContextParameter(servletContext, config, "cloudcoder.submitsvc.oop.ssl.useSSL", "true");
			setPropertyFromContextParameter(servletContext, config, "cloudcoder.submitsvc.oop.host", "localhost");
			if (Boolean.valueOf(config.getProperty("cloudcoder.submitsvc.oop.ssl.useSSL"))) {
				setPropertyFromContextParameter(servletContext, config, "cloudcoder.submitsvc.ssl.keystore", null);
				setPropertyFromContextParameter(servletContext, config, "cloudcoder.submitsvc.ssl.keystore.password", null);
			}
			setPropertyFromContextParameter(servletContext, config, "cloudcoder.submitsvc.oop.port", String.valueOf(OutOfProcessSubmitService.DEFAULT_PORT));

			// Initialize and start the OutOfProcessSubmitService
			svc.initFromConfigProperties(config);
			svc.start();
			
		} catch (IOException e) {
			throw new IllegalStateException("Could not create server thread for oop submit service", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		try {
			OutOfProcessSubmitService.getInstance().shutdown();
			OutOfProcessSubmitService.setInstance(null);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Interrupted while waiting for oop submit service server thread to shut down", e);
		}
	}

}
