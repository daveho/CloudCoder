// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import org.cloudcoder.app.shared.model.User;

/**
 * Interface for a login provider.
 * 
 * @author David Hovemeyer
 */
public interface ILoginProvider {
	/**
	 * Attempt to log in by finding the {@link User} that corresponds to
	 * the given username and plaintext password.
	 * 
	 * @param username the username
	 * @param password the plaintext password
	 * @param request  the HttpServletRequest
	 * @return the authenticated {@link User} corresponding to the username/password,
	 *         or null if the username/password don't correspond to a known user
	 */
	public User login(String username, String password, HttpServletRequest request);

	/**
	 * @return true if username and password are required to log in,
	 *         fales if some type of preauthorization is required
	 */
	public boolean isUsernamePasswordRequired();

	/**
	 * Get the preauthorized username from the servlet request.
	 * This method should only be called if {@link #isUsernamePasswordRequired()}
	 * returns false.
	 * 
	 * @param request  the servlet request
	 * @return the preauthorized username, or null if there is no preauthorized username
	 *         (for example, because the user has not actually preauthenticated)
	 */
	public String getPreAuthorizedUsername(HttpServletRequest request);
}
