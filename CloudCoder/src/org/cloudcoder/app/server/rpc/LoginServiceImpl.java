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
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.servlet.http.HttpSession;

import org.cloudcoder.app.client.rpc.LoginService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Activity;
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
	
	public static final String LOGIN_SERVICE="loginService";
	public static final String LOGIN_DATABASE="database";
	public static final String LOGIN_IMAP="imap";
	public static final String HOST="host";
	public static final String TRANSPORT="transport";

	@Override
	public User login(String userName, String password) {
	    //TODO: Check the type of authentication being asked for
        
	    String loginService = getServletConfig().getInitParameter(LOGIN_SERVICE);
	    User user=null;
	    if (LOGIN_IMAP.equals(loginService)) {
	        Properties props=getImapPropertiesInitParams();
	        user = Database.getInstance().authenticateUserImap(userName, password, props);
	    } else {
	        // default is to use the database
	        user = Database.getInstance().authenticateUser(userName, password);
	    }

		if (user != null) {
			// Set User object in server HttpSession so that other
			// servlets will know that the client is logged in
			HttpSession session = getThreadLocalRequest().getSession();
			session.setAttribute(SessionAttributeKeys.USER_KEY, user);
		}
		
		return user;
	}
	
	/**
     * @return
     */
    private Properties getImapPropertiesInitParams() {
        Properties props=new Properties();
        
        props.setProperty(HOST, getServletConfig().getInitParameter(HOST));
        props.setProperty(TRANSPORT, getServletConfig().getInitParameter(TRANSPORT));
        props.setProperty("mail.smtps.auth", "true");
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
    
    public static boolean authenticateImap(String username, 
            String password, 
            Properties props)
    {
        //String host = "mail.knox.edu";
        //String transport="smtps";
        //String username = "jaime.spacco";
        Transport t=null;
        try {
            // transport should be smtps
            t=Session.getInstance(props).getTransport(props.getProperty(TRANSPORT));    
            t.connect(props.getProperty(HOST), username, password);
            logger.debug("Successful imap login for "+username);
            return true;
        } catch (MessagingException e) {
            logger.warn("Failed imap login for "+username, e);
            return false;
        } finally {
            if (t!=null) {
                try {
                    t.close();
                } catch (Exception e) {
                    logger.error("Unable to close imap Transport connection", e);
                    // ignore
                }
            }
        }
    }

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.client.rpc.LoginService#getUser()
	 */
	@Override
	public User getUser() {
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
}
