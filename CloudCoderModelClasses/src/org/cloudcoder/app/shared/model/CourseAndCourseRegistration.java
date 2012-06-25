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
 * A pair containing a {@link Course} and {@link CourseRegistration}.
 * Useful for allowing an authenticated user to find out what courses
 * he/she is registered for, getting registration information for each
 * registered course.  We can use the registration information,
 * for example, to present course admin options for a user who is
 * registered as an instructor for a course.
 * 
 * @author David Hovemeyer
 */
public class CourseAndCourseRegistration implements Serializable {
	private static final long serialVersionUID = 1L;

	private Course course;
	private CourseRegistration courseRegistration;
	
	public CourseAndCourseRegistration() {
		
	}
	
	public void setCourse(Course course) {
		this.course = course;
	}
	
	public Course getCourse() {
		return course;
	}
	
	public void setCourseRegistration(CourseRegistration courseRegistration) {
		this.courseRegistration = courseRegistration;
	}
	
	public CourseRegistration getCourseRegistration() {
		return courseRegistration;
	}
}
