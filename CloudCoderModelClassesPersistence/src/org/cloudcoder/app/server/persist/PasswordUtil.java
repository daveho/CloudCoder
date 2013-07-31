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

package org.cloudcoder.app.server.persist;

/**
 * Password utility methods.
 * Call these methods in preference to directly calling the
 * underlying BCrypt methods: that way, we can ensure that
 * BCrypt is used consistently (e.g., salt generation,
 * how many rounds of hashing to require, etc.) 
 * 
 * @author David Hovemeyer
 */
public class PasswordUtil {
	/**
	 * Convert a plaintext password to a hashed password.
	 * 
	 * @param plaintext a plaintext password
	 * @return the hashed password
	 */
	public static String hashPassword(String plaintext) {
		return BCrypt.hashpw(plaintext, BCrypt.gensalt());
	}
	
	/**
	 * Determine if the given plaintext password matches a
	 * given hashed password.
	 * 
	 * @param plaintext a plaintext password
	 * @param hashed    a hashed password
	 * @return true if they match, false otherwise
	 */
	public static boolean matches(String plaintext, String hashed) {
		return BCrypt.checkpw(plaintext, hashed);
	}
}
