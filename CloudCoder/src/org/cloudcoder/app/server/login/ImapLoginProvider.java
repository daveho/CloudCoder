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

import java.util.Properties;

import javax.mail.Store;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ILoginProvider} that authenticates against
 * an IMAP server.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class ImapLoginProvider extends AbstractLoginProvider {
	private static final Logger logger = LoggerFactory.getLogger(ImapLoginProvider.class);

	public static final String LOGIN_SERVICE="cloudcoder.login.service";
	public static final String LOGIN_HOST="cloudcoder.login.host";
	public static final String DEFAULT_LOGIN_HOST="imaps.google.com";

	public static final String IMAP_SOCKET_FACTORY_CLASS="mail.imap.socketFactory.class";
	public static final String IMAP_SOCKET_FACTORY_FALLBACK= "mail.imap.socketFactory.fallback";
	public static final String IMAP_SOCKET_FACTORY_PORT= "mail.imap.socketFactory.port";

	private Properties props;

	/**
	 * Constructor.
	 * 
	 * @param ctx the ServletContext (which contains the init parameters,
	 *             which are set based on cloudcoder.properties)
	 */
	public ImapLoginProvider(ServletContext ctx) {

		this.props=new Properties();

		// note that you can also use the defult imap port (including the
		// port specified by mail.imap.port) for your SSL port configuration.
		// however, specifying mail.imap.socketFactory.port means that,
		// if you decide to use fallback, you can try your SSL connection
		// on the SSL port, and if it fails, you can fallback to the normal
		// IMAP port.

		// set this session up to use SSL for IMAP connections
		setProperty(ctx, props, IMAP_SOCKET_FACTORY_CLASS,"javax.net.ssl.SSLSocketFactory");
		// by default, don't fallback to normal IMAP connections on failure.
		setProperty(ctx, props, IMAP_SOCKET_FACTORY_FALLBACK, "false");
		// use the simap port for imap/ssl connections.
		setProperty(ctx, props, IMAP_SOCKET_FACTORY_PORT, "993");
		// get the hostname out of the web.xml file
		setProperty(ctx, props, LOGIN_HOST, DEFAULT_LOGIN_HOST);
	}

	private void setProperty(ServletContext ctx, Properties props, String key, String defaultValue) {
		String val=ctx.getInitParameter(key);
		if (val!=null) {
			props.setProperty(key, val);
		} else {
			props.setProperty(key, defaultValue);
		}
	}

	@Override
	public User login(String username, String password, HttpServletRequest request) {
		User user;

		// Login using IMAP
		user = Database.getInstance().getUserWithoutAuthentication(username);
		if (user != null) {
			// Authenticate via IMAP
			if (!authenticateImap(username, password)) {
				// Authentication failed
				user = null;
			}
		}

		return null;
	}

	/**
	 * Attempt to authenticate a user via IMAP.
	 * 
	 * @param username  the username
	 * @param password  the plaintext password
	 * @return true if the user has been authenticated successfully, false if not
	 */
	private boolean authenticateImap(String username, String password) {
		String url="imap://mailtest:"+username+"@"+props.getProperty(LOGIN_HOST);

		// configure the jvm to use the jsse security.
		java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		try {
			// create the Session
			javax.mail.Session session = javax.mail.Session.getInstance(props);
			// and create the store..
			Store store=session.getStore("imaps");
			//javax.mail.Store store = session.getStore(new 
			//javax.mail.URLName(url));
			//javax.mail.URLName("imap://mailtest:mailtest@localhost/"));
			// and connect.
			store.connect(props.getProperty(LOGIN_HOST), username, password);
			return true;
		} catch (Exception e) {
			logger.error(username+ " unable to connect to "+url, e);
			return false;
		}
	}

}
