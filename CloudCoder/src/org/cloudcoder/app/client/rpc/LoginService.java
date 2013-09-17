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

package org.cloudcoder.app.client.rpc;

import org.cloudcoder.app.shared.model.InitErrorException;
import org.cloudcoder.app.shared.model.LoginSpec;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
	/**
	 * Get the {@link LoginSpec} that describes the information needed to log in.
	 * 
	 * @return the {@link LoginSpec}
	 */
	public LoginSpec getLoginSpec();
	
	/**
	 * Authenticate with given username and password.
	 * 
	 * @param userName the username
	 * @param password the password
	 * @return the authenticated User object, or null if the username/password
	 *         combination is not found
	 */
	public User login(String userName, String password);
	
	/**
	 * Logout current User.
	 */
	public void logout();
	
	/**
	 * Get the currently logged-in User.
	 * @return the currently logged-in User, or null if no User is logged in
	 * @throws InitErrorException 
	 */
	public User getUser() throws InitErrorException;
	
	/**
	 * Get list of (server-side) webapp init errors.
	 * 
	 * @return list of (server-side) webapp init errors
	 */
	public String[] getInitErrorList();
}
