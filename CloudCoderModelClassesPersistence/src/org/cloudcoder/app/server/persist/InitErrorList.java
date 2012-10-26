// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011,2012 Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011,2012 David H. Hovemeyer <dhovemey@ycp.edu>
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

import java.util.ArrayList;
import java.util.List;

/**
 * A singleton representing a list of initialization errors.
 * This can be used to log any serious initialization failures
 * (such as the database schema being out of date),
 * and the webapp can check for such errors and report them
 * explicitly.
 * 
 * The singleton is thread safe.
 * 
 * @author David Hovemeyer
 */
public class InitErrorList {
	private Object lock;
	private List<String> errorList;
	
	// The singleton instance
	private static final InitErrorList theInstance = new InitErrorList();
	
	/**
	 * Get the singleton instance
	 */
	public static InitErrorList instance() {
		return theInstance;
	}
	
	private InitErrorList() {
		this.lock = new Object();
		this.errorList = new ArrayList<String>();
	}
	
	/**
	 * Add an error to the list.
	 * 
	 * @param error the error to add
	 */
	public void addError(String error) {
		synchronized (lock) {
			errorList.add(error);
		}
	}
	
	/**
	 * Get the list of errors.
	 * 
	 * @return the list of errors
	 */
	public List<String> getErrorList() {
		synchronized (lock) {
			ArrayList<String> copy = new ArrayList<String>(errorList);
			return copy;
		}
	}

	/**
	 * Determine whether there are any init errors.
	 * 
	 * @return true if there is at least one init error, false if
	 *         there are no init errors
	 */
	public boolean hasErrors() {
		synchronized (lock) {
			return !errorList.isEmpty();
		}
	}
}
