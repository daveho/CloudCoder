package org.cloudcoder.app.server.rpc;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletUtil {
	private static Logger logger = LoggerFactory.getLogger(ServletUtil.class); 
	
	/**
	 * Check whether or not the client is authenticated.
	 * 
	 * @return the authenticated User object
	 * @throws NetCoderAuthenticationException if the client is not authenticated
	 */
	public static User checkClientIsAuthenticated(HttpServletRequest request) throws NetCoderAuthenticationException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute(SessionAttributeKeys.USER_KEY);
		if (user == null) {
			logger.warn("Authentication failure - no user in session");
			throw new NetCoderAuthenticationException();
		}
		return user;
	}
	
	/**
	 * Attempt to authenticate a user via IMAP.
	 * 
	 * @param username  the username
	 * @param password  the plaintext password
	 * @param props     login properties from the servlet context init parameters
	 * @return true if the user has been authenticated successfully, false if not
	 */
	public static boolean authenticateImap(String username,
            String password,
            Properties props)
    {
        String url="imap://mailtest:"+username+"@"+props.getProperty(LoginServiceImpl.LOGIN_HOST);
        
        // configure the jvm to use the jsse security.
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        try {
            // create the Session
            javax.mail.Session session = javax.mail.Session.getInstance(props);
            // and create the store..
            javax.mail.Store store = session.getStore(new 
                    javax.mail.URLName(url));
                    //javax.mail.URLName("imap://mailtest:mailtest@localhost/"));
            // and connect.
            store.connect(username, password);
            return true;
        } catch (Exception e) {
            logger.error(username+ " unable to connect to "+url, e);
            return false;
        }
    }

}
