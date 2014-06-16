// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
 * Data about the anonymization of a user account.
 * 
 * @author David Hovemeyer
 */
public class Anonymization {
	private int userId;
	private String anonUsername;
	private String genPassword;
	private String realUsername;
	private String realFirstname;
	private String realLastname;
	private String realEmail;
	private String realWebsite;
	
	public Anonymization(int userId, String anonUsername, String genPassword, String realUsername, String realFirstname, String realLastname, String realEmail, String realWebsite) {
		this.userId = userId;
		this.anonUsername = anonUsername;
		this.genPassword = genPassword;
		this.realUsername = realUsername;
		this.realFirstname = realFirstname;
		this.realLastname = realLastname;
		this.realEmail = realEmail;
		this.realWebsite = realWebsite;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public String getAnonUsername() {
		return anonUsername;
	}
	
	public void setAnonUsername(String anonUsername) {
		this.anonUsername = anonUsername;
	}
	
	public String getGenPassword() {
		return genPassword;
	}
	
	public void setGenPassword(String genPassword) {
		this.genPassword = genPassword;
	}
	
	public String getRealUsername() {
		return realUsername;
	}
	
	public void setRealUsername(String realUsername) {
		this.realUsername = realUsername;
	}
	
	public String getRealFirstname() {
		return realFirstname;
	}
	
	public void setRealFirstname(String realFirstname) {
		this.realFirstname = realFirstname;
	}
	
	public String getRealLastname() {
		return realLastname;
	}
	
	public void setRealLastname(String realLastname) {
		this.realLastname = realLastname;
	}
	
	public String getRealEmail() {
		return realEmail;
	}
	
	public void setRealEmail(String realEmail) {
		this.realEmail = realEmail;
	}
	
	public String getRealWebsite() {
		return realWebsite;
	}
	
	public void setRealWebsite(String realWebsite) {
		this.realWebsite = realWebsite;
	}
}