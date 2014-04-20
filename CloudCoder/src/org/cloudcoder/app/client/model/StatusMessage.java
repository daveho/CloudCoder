// CloudCloder - a web-based pedagogical programming environment
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

package org.cloudcoder.app.client.model;

import org.cloudcoder.app.shared.model.OperationResult;

/**
 * A status message describing the outcome of an operation
 * or other interesting piece of information the user should
 * know about.
 * 
 * Use the factory methods {@link #goodNews(String)}.
 * {@link #error(String)}, {@link #pending(String)}, and
 * {@link #information(String)} to create StatusMessage objects.
 */
public class StatusMessage {
	/**
	 * Status message categories.
	 */
	public enum Category {
		/** An informational status message. */
		INFORMATION,
		
		/** An error message. */
		ERROR,
		
		/** A notification of good news, e.g., all tests passed. */
		GOOD_NEWS,
		
		/**
		 * A status message describing a potentially long-running operation
		 * (like testing a submission).
		 */
		PENDING,
	}
	
	private Category category;
	private String message;
	
	public StatusMessage(Category category, String message) {
		this.category = category;
		this.message = message;
	}
	
	/**
	 * @return the status message category
	 */
	public Category getCategory() {
		return category;
	}
	
	/**
	 * @return the status message text
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Factory method to create a "good news" status message.
	 * 
	 * @param message the message text
	 * @return the good news status message
	 */
	public static StatusMessage goodNews(String message) {
		return new StatusMessage(Category.GOOD_NEWS, message);
	}

	/**
	 * Factory method to create an error status message.
	 * 
	 * @param message the message text
	 * @return the error status message
	 */
	public static StatusMessage error(String message) {
		return new StatusMessage(Category.ERROR, message);
	}

	/**
	 * Factory method to create an error status message.
	 * Appends the exception object's message if there is one.
	 * 
	 * @param message the message text
	 * @param caught the exception that signaled the error
	 * @return the error status message
	 */
	public static Object error(String message, Throwable caught) {
		if (caught != null && caught.getMessage() != null) {
			message = message + ": " + caught.getMessage();
		}
		return new StatusMessage(Category.ERROR, message);
	}

	/**
	 * Factory method to create a pending operation status message.
	 * 
	 * @param message
	 * @return the pending operation status message
	 */
	public static StatusMessage pending(String message) {
		return new StatusMessage(Category.PENDING, message);
	}
	
	/**
	 * Factory method to create an information status message.
	 * 
	 * @param message the message text
	 * @return the information status message
	 */
	public static StatusMessage information(String message) {
		return new StatusMessage(Category.INFORMATION, message);
	}

	/**
	 * Create a StatusMessage from an {@link OperationResult}.
	 * 
	 * @param result the {@link OperationResult}
	 * @return the StatusMessage
	 */
	public static Object fromOperationResult(OperationResult result) {
		return new StatusMessage(result.isSuccess() ? Category.GOOD_NEWS : Category.ERROR, result.getMessage());
	}
}
