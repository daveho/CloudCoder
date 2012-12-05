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

package org.cloudcoder.builder2.model;

/**
 * Exception to indicate that an internal error occurred.
 * 
 * @author David Hovemeyer
 */
public class InternalBuilderException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param msg message describing the error
	 */
	public InternalBuilderException(String msg) {
		super(msg);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param msg message describing the error
	 * @param cause the root cause
	 */
	public InternalBuilderException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param cls class raising the exception
	 * @param msg message describing the error
	 */
	public InternalBuilderException(Class<?> cls, String msg) {
		this(cls.getSimpleName() + ": " + msg);
	}

	/**
	 * Constructor.
	 * 
	 * @param cls class raising the exception
	 * @param msg message describing the error
	 * @param cause root cause
	 */
	public InternalBuilderException(Class<?> cls, String message, Throwable cause) {
		this(cls, message);
		initCause(cause);
	}
}
