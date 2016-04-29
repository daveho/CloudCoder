// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
 * Data about a {@link User} that is being added or edited in the context
 * of a {@link Course}.
 * 
 * @author David Hovemeyer
 */
public class EditedUser implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private User user;
	private String currentPassword;
	private String password;
	private int section;
	private CourseRegistrationType registrationType;
	
	/**
	 * Constructor.
	 */
	public EditedUser() {
		
	}
	
	/**
	 * Set the {@link User}.
	 * 
	 * @param user the {@link User}
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * @return the {@link User}
	 */
	public User getUser() {
		return user;
	}
	
	/**
	 * Set the current (existing) password.
	 * This is used for verification when updating a password.
	 * It does not need to be set for creating a new user account.
	 * 
	 * @param currentPassword the current password
	 */
	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
	
	/**
	 * @return the current (existing) password
	 */
	public String getCurrentPassword() {
		return currentPassword;
	}
	
	/**
	 * If updating an existing user, set the new (updated) password.
	 * If creating a new user account, this is the initial password to set for
	 * the account.
	 * 
	 * @param password the updated or initial password for the user account
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @return the updated or initial password for the user account
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the section number.
	 * 
	 * @param section the section number
	 */
	public void setSection(int section) {
		this.section = section;
	}
	
	/**
	 * @return the section number
	 */
	public int getSection() {
		return section;
	}
	
	/**
	 * Set the {@link CourseRegistrationType}.
	 * 
	 * @param registrationType the {@link CourseRegistrationType}
	 */
	public void setRegistrationType(CourseRegistrationType registrationType) {
		this.registrationType = registrationType;
	}
	
	/**
	 * @return the {@link CourseRegistrationType}
	 */
	public CourseRegistrationType getRegistrationType() {
		return registrationType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof EditedUser)) {
			return false;
		}
		EditedUser other = (EditedUser) obj;
		return ModelObjectUtil.equals(this.user, other.user)
				&& ModelObjectUtil.equals(this.currentPassword, other.currentPassword)
				&& ModelObjectUtil.equals(this.password, other.password)
				&& this.section == other.section
				&& this.registrationType == other.registrationType;
	}
	
	@Override
	public int hashCode() {
		// This object should really not be used as the key in
		// a hash table.
		return 0;
	}
}
