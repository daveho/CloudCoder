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
 * Model object representing a user.
 * 
 * @author David Hovemeyer
 */
public class User implements Serializable, IModelObject<User> {
	private static final long serialVersionUID = 1L;

	private int id;
	private String userName;
	private String passwordHash;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema<User> SCHEMA = new ModelObjectSchema<User>()
		.add(new ModelObjectField<User, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
			public void set(User obj, Integer value) { obj.setId(value); }
			public Integer get(User obj) { return obj.getId(); }
		})
		.add(new ModelObjectField<User, String>("username", String.class, 20, ModelObjectIndexType.UNIQUE) {
			public void set(User obj, String value) { obj.setUsername(value); }
			public String get(User obj) { return obj.getUsername(); }
		})
		.add(new ModelObjectField<User, String>("password_hash", String.class, 60) {
			public void set(User obj, String value) { obj.setPasswordHash(value); }
			public String get(User obj) { return obj.getPasswordHash(); }
		});

	/**
	 * Constructor.
	 */
	public User() {
	}
	
	@Override
	public ModelObjectSchema<? extends User> getSchema() {
		return SCHEMA;
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
