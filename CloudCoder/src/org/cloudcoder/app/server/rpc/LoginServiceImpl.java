// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.rpc;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.cloudcoder.app.client.rpc.LoginService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.InitErrorList;
import org.cloudcoder.app.shared.model.Activity;
import org.cloudcoder.app.shared.model.InitErrorException;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link LoginService}.
 * 
 * @author David Hovemeyer
 */
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(LoginServiceImpl.class);
	
	public static final String LOGIN_SERVICE="cloudcoder.login.service";
	public static final String LOGIN_DATABASE="database";
	public static final String LOGIN_IMAP="imap";
	public static final String LOGIN_HOST="cloudcoder.login.host";
	public static final String DEFAULT_LOGIN_HOST="imaps.google.com";
	
	public static final String IMAP_SOCKET_FACTORY_CLASS="mail.imap.socketFactory.class";
    public static final String IMAP_SOCKET_FACTORY_FALLBACK= "mail.imap.socketFactory.fallback";
    public static final String IMAP_SOCKET_FACTORY_PORT= "mail.imap.socketFactory.port";

	@Override
	public User login(String userName, String password) {
	    // Can this method be called anywhere?
	    // Does AdminAuthorizationFilter have access to the ServletConfig?
        
		User user=null;
		Properties props=getLoginPropertiesInitParams();
	    
	    if (LOGIN_IMAP.equals(props.getProperty(LOGIN_SERVICE, ""))) {
	    	// Login using IMAP
	    	user = Database.getInstance().getUserWithoutAuthentication(userName);
	    	if (user != null) {
	    		// Authenticate via IMAP
	    		if (!ServletUtil.authenticateImap(userName, password, props)) {
	    			// Authentication failed
	    			user = null;
	    		}
	    	}
	    } else {
	    	// Login by checking provided credentials against database 
		    user = Database.getInstance().authenticateUser(userName, password);
	    }
	    
	    if (user == null) {
	    	logger.info("Login failure for user {}", userName);
	    }

		if (user != null) {
			// Set User object in server HttpSession so that other
			// servlets will know that the client is logged in
			HttpSession session = getThreadLocalRequest().getSession();
			session.setAttribute(SessionAttributeKeys.USER_KEY, user);
		}
		
		return user;
	}
	
	
		
	public void setProperty(Properties props, String key, String defaultValue) {
	    String val=getServletContext().getInitParameter(key);
	    if (val!=null) {
	        props.setProperty(key, val);
	    } else {
	        props.setProperty(key, defaultValue);
	    }
	}
	
	/**
	 * Will use the defaults given in this method, unless 
	 * values are specified in web.xml, in which case will
	 * use the values from web.xml.
	 * 
	 * @return A Properties object configured to authenticate with IMAP.
	 */
	private Properties getLoginPropertiesInitParams() {
        Properties props=new Properties();

        // note that you can also use the defult imap port (including the
        // port specified by mail.imap.port) for your SSL port configuration.
        // however, specifying mail.imap.socketFactory.port means that,
        // if you decide to use fallback, you can try your SSL connection
        // on the SSL port, and if it fails, you can fallback to the normal
        // IMAP port.
        
        // set the login service (defaults to database)
        setProperty(props, LOGIN_SERVICE, LOGIN_DATABASE);
        // set this session up to use SSL for IMAP connections
        setProperty(props, IMAP_SOCKET_FACTORY_CLASS,"javax.net.ssl.SSLSocketFactory");
        // by default, don't fallback to normal IMAP connections on failure.
        setProperty(props, IMAP_SOCKET_FACTORY_FALLBACK, "false");
        // use the simap port for imap/ssl connections.
        setProperty(props, IMAP_SOCKET_FACTORY_PORT, "993");
        // get the hostname out of the web.xml file
        setProperty(props, LOGIN_HOST, DEFAULT_LOGIN_HOST);

        return props;
    }

    @Override
	public void logout() {
		HttpSession session = getThreadLocalRequest().getSession();
		
		@SuppressWarnings("unchecked")
		Enumeration<String> attributeNames = (Enumeration<String>) session.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String attr = attributeNames.nextElement();
			session.removeAttribute(attr);
		}
	}
    
    
    
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.LoginService#getUser()
	 */
	@Override
	public User getUser() throws InitErrorException {
		// Special case: this is the first RPC call that is made by the
		// client.  If a fatal init error occurred here in the server side
		// of the webapp, throw an InitErrorException to let the client
		// know to display a diagnostic page (that the cloudcoder admin
		// can use to resolve the issue.)
		if (InitErrorList.instance().hasErrors()) {
			throw new InitErrorException();
		}
		
		return (User) getThreadLocalRequest().getSession().getAttribute(SessionAttributeKeys.USER_KEY);
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.LoginService#getActivity()
	 */
	@Override
	public Activity getActivity() {
		return (Activity) getThreadLocalRequest().getSession().getAttribute(SessionAttributeKeys.ACTIVITY_KEY);
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.LoginService#setActivity(org.cloudcoder.app.shared.model.Activity)
	 */
	@Override
	public void setActivity(Activity activity) {
		getThreadLocalRequest().getSession().setAttribute(SessionAttributeKeys.ACTIVITY_KEY, activity);
	}
	
	@Override
	public String[] getInitErrorList() {
		List<String> initErrorList = InitErrorList.instance().getErrorList();
		return initErrorList.toArray(new String[initErrorList.size()]);
	}
}
