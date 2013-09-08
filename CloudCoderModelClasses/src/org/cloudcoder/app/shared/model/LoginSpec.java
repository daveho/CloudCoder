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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;

/**
 * Information communicated to the webapp client to describe the
 * information required to log in.
 * 
 * @author David Hovemeyer
 */
public class LoginSpec implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String institutionName;
	private boolean usernamePasswordRequired;
	private String preAuthorizedUsername;

	/**
	 * Constructor.
	 */
	public LoginSpec() {
		
	}
	
	/**
	 * Set the institution name.
	 * 
	 * @param institutionName the institution name
	 */
	public void setInstitutionName(String institutionName) {
		this.institutionName = institutionName;
	}
	
	/**
	 * @return the institution name
	 */
	public String getInstitutionName() {
		return institutionName;
	}
	
	/**
	 * Set whether or not username and password are required to log in.
	 * 
	 * @param usernamePasswordRequired true if username and password are required to log in,
	 *                                 false if some type of preauthorization (such as a federated
	 *                                 login service) is required
	 */
	public void setUsernamePasswordRequired(boolean usernamePasswordRequired) {
		this.usernamePasswordRequired = usernamePasswordRequired;
	}

	/**
	 * @return true if username and password are required to log in,
	 *         fales if some type of preauthorization is required
	 */
	public boolean isUsernamePasswordRequired() {
		return usernamePasswordRequired;
	}
	
	/**
	 * @return if a preauthorized username has been set
	 */
	public boolean hasPreAuthorizedUsername() {
		return preAuthorizedUsername != null;
	}
	
	/**
	 * Set the preauthorized username (for example, indicating that the client has
	 * already authenticated via a federated login system.)
	 * 
	 * @param preAuthorizedUsername the preauthorized username to set
	 */
	public void setPreAuthorizedUsername(String preAuthorizedUsername) {
		this.preAuthorizedUsername = preAuthorizedUsername;
	}
	
	/**
	 * Get the preauthorized username.
	 * 
	 * @return the preauthorized username
	 */
	public String getPreAuthorizedUsername() {
		return preAuthorizedUsername;
	}
}
