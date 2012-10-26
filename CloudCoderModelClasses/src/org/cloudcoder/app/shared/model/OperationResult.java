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
 * A generic success/error result object for RPC operations.
 * 
 * @author David Hovemeyer
 */
public class OperationResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean success;
	private String message;

	/**
	 * Constructor.
	 */
	public OperationResult() {
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param success true if the operation succeeded, false otherwise
	 * @param message message describing the failure (if the operation was a failure)
	 */
	public OperationResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}
	
	/**
	 * @return true if the operation was a success, false if there was an error
	 */
	public boolean isSuccess() {
		return success;
	}
	
	/**
	 * @return the message describing the error (if any)
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Set message.
	 * 
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
