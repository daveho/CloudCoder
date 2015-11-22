// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
 * Information required to create a {@link Course}.
 * Specifically, it's the course info, the initial instructor username,
 * and the initial section number.
 * 
 * @author David Hovemeyer
 */
public class CourseCreationSpec {
	private Course course;
	private String username;
	private int section;

	/**
	 * Constructor.
	 */
	public CourseCreationSpec() {
		
	}
	
	/**
	 * Set the {@link Course}.
	 * 
	 * @param course the {@link Course}
	 */
	public void setCourse(Course course) {
		this.course = course;
	}
	
	/**
	 * @return the {@link Course}
	 */
	public Course getCourse() {
		return course;
	}
	
	/**
	 * Set the initial instructor username.
	 * 
	 * @param username the initial instructor username
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @return the initial instructor username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Set the initial section number.
	 * 
	 * @param section the initial section number
	 */
	public void setSection(int section) {
		this.section = section;
	}
	
	/**
	 * @return the initial section number
	 */
	public int getSection() {
		return section;
	}
}
