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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet context listener to create and destroy the
 * {@link OutOfProcessSubmitService} as necessary for a web application.
 * 
 * @author David Hovemeyer
 */
public class OutOfProcessSubmitServiceServletContextListener implements ServletContextListener {
	private static Logger logger = LoggerFactory.getLogger(OutOfProcessSubmitServiceServletContextListener.class);
	
	private static String getContextParameter(ServletContext ctx, String propName, String defValue) {
		String value = ctx.getInitParameter(propName);
		return value != null ? value : defValue;
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			OutOfProcessSubmitService svc = new OutOfProcessSubmitService();
			
			ServletContext servletContext = event.getServletContext();

			svc.setUseSSL(Boolean.parseBoolean(
					getContextParameter(servletContext, "cloudcoder.submitsvc.oop.ssl.useSSL", "true")));
			logger.info("OOP build service: useSSL={}", svc.isUseSSL());
			
			svc.setHostName(getContextParameter(servletContext, "cloudcoder.submitsvc.oop.host", "localhost"));
			
			if (svc.isUseSSL()) {
				// Determine keystore filename and password
				svc.setKeystoreFilename(servletContext.getInitParameter("cloudcoder.submitsvc.ssl.keystore"));
				if (svc.getKeystoreFilename() == null) {
					throw new IllegalArgumentException("cloudcoder.submitsvc.ssl.keystore property is not set");
				}
				svc.setKeystorePassword(servletContext.getInitParameter("cloudcoder.submitsvc.ssl.keystore.password"));
				if (svc.getKeystorePassword() == null) {
					throw new IllegalArgumentException("cloudcoder.submitsvc.ssl.keystore.password property is not set");
				}
				System.out.println("keystore=" + svc.getKeystoreFilename() + ",password=" + svc.getKeystorePassword());
			}
			
			// See if a non-default port was specified
			String p = servletContext.getInitParameter("cloudcoder.submitsvc.oop.port");
			int port = (p != null) ? Integer.parseInt(p) : OutOfProcessSubmitService.DEFAULT_PORT;
			
			svc.start(port);
			//instance = this;
			OutOfProcessSubmitService.setInstance(svc);
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
