package org.cloudcoder.app.server.rpc;

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
			logger.info("Authentication failure - no user in session");
			throw new NetCoderAuthenticationException();
		}
		return user;
	}

}
