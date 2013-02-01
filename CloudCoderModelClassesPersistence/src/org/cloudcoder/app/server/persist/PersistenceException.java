// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
 * Runtime exception indicating that a persistence operation
 * failed.
 * 
 * @author David Hovemeyer
 */
public class PersistenceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param msg   message describing the failure
	 * @param cause the cause
	 */
	public PersistenceException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param msg   message describing the failure
	 */
	public PersistenceException(String msg) {
		super(msg);
	}
}
