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

import javax.servlet.http.HttpServletRequest;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ILoginProvider} that authenticates based on
 * the <code>X-Remote-User</code> HTTP header, which is assumed
 * to have been set by a trusted proxy server.  Note that the
 * provided username and plaintext password will be ignored!
 * 
 * @author David Hovemeyer
 */
public class RemoteUserLoginProvider implements ILoginProvider {
	private static final String X_REMOTE_USER = "X-Remote-User";

	private static Logger logger = LoggerFactory.getLogger(RemoteUserLoginProvider.class);
	
	@Override
	public User login(String username, String password, HttpServletRequest request) {
		String headerValue = request.getHeader(X_REMOTE_USER);
		if (headerValue == null) {
			logger.error("Using remote user authentication, but no X-Remote-User header found");
			return null;
		}
		headerValue = headerValue.trim();
		
		User user = Database.getInstance().getUserWithoutAuthentication(headerValue);
		if (user == null) {
			logger.error("Using remote user authentication, but user {} is not a valid CloudCoder user", headerValue);
		}
		
		// Success!
		return user;
	}
	
	@Override
	public boolean isUsernamePasswordRequired() {
		return false;
	}
	
	@Override
	public String getPreAuthorizedUsername(HttpServletRequest request) {
		String username = request.getHeader(X_REMOTE_USER);
		if (username != null) {
			username = username.trim();
		}
		return username;
	}

}
