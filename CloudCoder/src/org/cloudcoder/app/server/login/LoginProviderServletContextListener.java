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

package org.cloudcoder.app.server.login;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet context listener that uses the cloudcoder configuration
 * information to create an appropriate {@link ILoginProvider}.
 * 
 * @author David Hovemeyer
 */
public class LoginProviderServletContextListener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(LoginProviderServletContextListener.class);
	
	private static ILoginProvider theInstance;
	
	/**
	 * Get the singleton {@link ILoginProvider} instance.
	 * 
	 * @return the singleton {@link ILoginProvider} instance
	 */
	public static ILoginProvider getProviderInstance() {
		return theInstance;
	}

	@Override
	public void contextInitialized(ServletContextEvent e) {
		String providerType = e.getServletContext().getInitParameter("cloudcoder.login.service");
		
		if (providerType != null) {
			if (providerType.equals("database")) {
				theInstance = new DatabaseLoginProvider();
			} else if (providerType.equals("imap")) {
				theInstance = new ImapLoginProvider(e.getServletContext());
			} else if (providerType.equals("remoteuser")) {
				theInstance = new RemoteUserLoginProvider();
			}
		}
		
		if (theInstance == null) {
			logger.error("Could not create a login provider of type {}", providerType);
			theInstance = new ErrorLoginProvider();
		}
	}
	

	@Override
	public void contextDestroyed(ServletContextEvent ctx) {
		// nothing to do
	}

}
