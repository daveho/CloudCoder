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

package org.cloudcoder.app.shared.model;

/**
 * A request to reset a user's password.
 * 
 * @author David Hovemeyer
 */
public class PasswordResetRequest implements IModelObject<PasswordResetRequest> {
	public static final int SECRET_LEN = 40;
	
	private int id;
	private String email;
	private String username;
	private String secret;
	
	public static final ModelObjectField<PasswordResetRequest, Integer> ID =
		new ModelObjectField<PasswordResetRequest, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
			public void set(PasswordResetRequest obj, Integer value) { obj.setId(value); }
			public Integer get(PasswordResetRequest obj) { return obj.getId(); }
		};
	public static final ModelObjectField<PasswordResetRequest, String> EMAIL =
		new ModelObjectField<PasswordResetRequest, String>("email", String.class, 120) {
			public void set(PasswordResetRequest obj, String value) { obj.setEmail(value); }
			public String get(PasswordResetRequest obj) { return obj.getEmail(); }
		};
	public static final ModelObjectField<PasswordResetRequest, String> USERNAME =
		new ModelObjectField<PasswordResetRequest, String>("username", String.class, User.MAX_USERNAME_LEN) {
			public void set(PasswordResetRequest obj, String value) { obj.setUsername(value); }
			public String get(PasswordResetRequest obj) { return obj.getUsername(); }
		};
	public static final ModelObjectField<PasswordResetRequest, String> SECRET =
		new ModelObjectField<PasswordResetRequest, String>("secret", String.class, SECRET_LEN) {
			public void set(PasswordResetRequest obj, String value) { obj.setSecret(value); }
			public String get(PasswordResetRequest obj) { return obj.getSecret(); }
		};
	
	/**
	 * Schema (version 0).		
	 */
	public static final ModelObjectSchema<PasswordResetRequest> SCHEMA_V0 = new ModelObjectSchema<PasswordResetRequest>("password_reset_request")
			.add(ID)
			.add(EMAIL)
			.add(USERNAME)
			.add(SECRET);
	
	/**
	 * Schema (current version).
	 */
	public static final ModelObjectSchema<PasswordResetRequest> SCHEMA = SCHEMA_V0;
	
	/**
	 * Constructor.
	 */
	public PasswordResetRequest() {
	}
	
	@Override
	public ModelObjectSchema<? super PasswordResetRequest> getSchema() {
		return SCHEMA;
	}

	/**
	 * Set the unique id.
	 * @param id the unique id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the email address.
	 * @param email the email address to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * @return the email address
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Set the username.
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Set the secret.
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * Initialize from a {@link User} and a secret (as specified in the password
	 * reset URL).
	 * 
	 * @param user    the {@link User}
	 * @param secret  the secret
	 */
	public void initFromUserAndSecret(User user, String secret) {
		this.id = -1;
		this.email = user.getEmail();
		this.username = user.getUsername();
		this.secret = secret;
	}
}
