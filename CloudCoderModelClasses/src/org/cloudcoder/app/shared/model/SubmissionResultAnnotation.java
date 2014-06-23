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

import java.io.Serializable;

/**
 * Generic key/value pair added as an annotation to a {@link SubmissionResult}.
 * Can represent information such static analysis results,
 * code coverage results, etc.
 * 
 * @author David Hovemeyer
 */
public class SubmissionResultAnnotation implements Serializable {
	private static final long serialVersionUID = 1L;

	private String key;
	private String value;
	
	/**
	 * Constructor.
	 */
	public SubmissionResultAnnotation() {
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param key    key name
	 * @param value  value
	 */
	public SubmissionResultAnnotation(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Set the key name.
	 * 
	 * @param key the key name to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * @return the key name
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Set the value.
	 * 
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
