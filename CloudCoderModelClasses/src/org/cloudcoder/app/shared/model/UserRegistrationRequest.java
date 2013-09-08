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

/**
 * A request to create an account.
 * Stores the (potential) user's email and a secret to be emailed
 * to them, that will then be used to create a secret registration URL.
 * 
 * @author David Hovemeyer
 */
public class UserRegistrationRequest extends User {
	private static final long serialVersionUID = 1L;

	public static final int SECRET_LENGTH = 40;
	
	private UserRegistrationRequestStatus status;
	private long timestamp;
	private String secret;
	
	public static final ModelObjectField<UserRegistrationRequest, UserRegistrationRequestStatus> STATUS = new ModelObjectField<UserRegistrationRequest, UserRegistrationRequestStatus>("status", UserRegistrationRequestStatus.class, 0) {
		public UserRegistrationRequestStatus get(UserRegistrationRequest obj) { return obj.getStatus(); }
		public void set(UserRegistrationRequest obj, UserRegistrationRequestStatus value) { obj.setStatus(value); }
	};
	
	public static final ModelObjectField<UserRegistrationRequest, Long> TIMESTAMP = new ModelObjectField<UserRegistrationRequest, Long>("timestamp", Long.class, 0) {
		public Long get(UserRegistrationRequest obj) { return obj.getTimestamp(); }
		public void set(UserRegistrationRequest obj, Long value) { obj.setTimestamp(value); }
	};
	
	public static final ModelObjectField<UserRegistrationRequest, String> SECRET = new ModelObjectField<UserRegistrationRequest, String>("secret", String.class, SECRET_LENGTH, ModelObjectIndexType.NON_UNIQUE) {
		public String get(UserRegistrationRequest obj) { return obj.getSecret(); }
		public void set(UserRegistrationRequest obj, String value) { obj.setSecret(value); }
	};

	public static final ModelObjectSchema<UserRegistrationRequest> SCHEMA_V0 = new ModelObjectSchema<UserRegistrationRequest>("user_registration_request")
			.addAll(User.SCHEMA_V1.getFieldList())
			.setIndexOn(User.USERNAME, ModelObjectIndexType.NONE) // Allow duplicate reg requests for same username
			.setIndexOn(User.EMAIL, ModelObjectIndexType.NONE)    // Allow duplicate reg requests for same email address
			.add(STATUS)
			.add(TIMESTAMP)
			.add(SECRET);
	
	public static final ModelObjectSchema<UserRegistrationRequest> SCHEMA = SCHEMA_V0;
	
	public UserRegistrationRequest() {
		
	}
	
	public UserRegistrationRequestStatus getStatus() {
		return status;
	}
	
	public void setStatus(UserRegistrationRequestStatus status) {
		this.status = status;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getSecret() {
		return secret;
	}
	
	public void setSecret(String secret) {
		this.secret = secret;
	}
}
