// CloudCloder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
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

/**
 * A status message describing the outcome of an operation
 * or other interesting piece of information the user should
 * know about.
 */
public class StatusMessage {
	public enum Category {
		INFORMATION,
		ERROR,
	}
	
	private Category category;
	private String message;
	
	public StatusMessage(Category category, String message) {
		this.category = category;
		this.message = message;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public String getMessage() {
		return message;
	}
}
