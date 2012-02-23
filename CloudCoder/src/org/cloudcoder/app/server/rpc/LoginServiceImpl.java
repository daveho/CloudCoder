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

import javax.servlet.http.HttpSession;

import org.cloudcoder.app.client.rpc.LoginService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Activity;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of {@link LoginService}.
 * 
 * @author David Hovemeyer
 */
public class LoginServiceImpl extends RemoteServiceServlet implements LoginService {
	private static final long serialVersionUID = 1L;

	@Override
	public User login(String userName, String password) {
		User user = Database.getInstance().authenticateUser(userName, password);

		if (user != null) {
			// Set User object in server HttpSession so that other
			// servlets will know that the client is logged in
			HttpSession session = getThreadLocalRequest().getSession();
			session.setAttribute(SessionAttributeKeys.USER_KEY, user);
		}
		
		return user;
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
