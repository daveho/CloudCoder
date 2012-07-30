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
import java.util.Arrays;

/**
 * Model object representing a user.
 * 
 * @author David Hovemeyer
 */
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String userName;
	private String passwordHash;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema SCHEMA = new ModelObjectSchema(Arrays.asList(
			new ModelObjectField("id", Integer.class, 0, ModelObjectIndexType.IDENTITY),
			new ModelObjectField("username", String.class, 20, ModelObjectIndexType.UNIQUE),
			new ModelObjectField("password_hash", String.class, 60)
	));

	/**
	 * Constructor.
	 */
	public User() {
	}
	
	/**
	 * Set the user's unique id.
	 * @param id the user's unique id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Get the user's unique id.
	 * @return the user's unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the user's username.
	 * @param userName the user's username
	 */
	public void setUsername(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Get the user's username.
	 * @return the user's username
	 */
	public String getUsername() {
		return userName;
	}
	
	/**
	 * Set the user's bcrypt password hash. 
	 * @param passwordHash the user's bcrypt password hash
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	/**
	 * Get the user's bcrypt password hash.
	 * @return the user's brcypt password hash
	 */
	public String getPasswordHash() {
		return passwordHash;
	}
}
