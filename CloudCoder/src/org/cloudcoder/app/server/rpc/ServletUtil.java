package org.cloudcoder.app.server.rpc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.User;

public class ServletUtil {
	/**
	 * Check whether or not the client is authenticated.
	 * 
	 * @return the authenticated User object
	 * @throws NetCoderAuthenticationException if the client is not authenticated
	 */
	public static User checkClientIsAuthenticated(HttpServletRequest request) throws NetCoderAuthenticationException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		if (user == null) {
			throw new NetCoderAuthenticationException();
		}
		return user;
	}

}
