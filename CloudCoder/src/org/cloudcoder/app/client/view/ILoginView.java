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

package org.cloudcoder.app.client.view;

import org.cloudcoder.app.shared.model.LoginSpec;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author David Hovemeyer
 */
public interface ILoginView extends IsWidget {
	/**
	 * Set the {@link LoginSpec}.
	 * 
	 * @param loginSpec the {@link LoginSpec}
	 */
	public void setLoginSpec(LoginSpec loginSpec);
	
	/**
	 * Get the username to send to the server
	 * in order to log in.
	 * 
	 * @return the username
	 */
	public String getUsername();
	
	/**
	 * Get the password to send to the server
	 * in order to log in.
	 * 
	 * @return the password
	 */
	public String getPassword();
	
	/**
	 * Set the callback that should be invoked when the
	 * user initiates login.
	 * 
	 * @param callback the login callback
	 */
	public void setLoginCallback(Runnable callback);
	
	/**
	 * Set an informational message (for example, to indicate that
	 * login is in progress).
	 * 
	 * @param infoMessage the informational message
	 */
	public void setInfoMessage(String infoMessage);
	
	/**
	 * Set an error message (e.g., to indicate a login failure).
	 * 
	 * @param errorMessage the error message to set
	 */
	public void setErrorMessage(String errorMessage);
	
	/**
	 * Activate the view.
	 */
	public void activate();
}
