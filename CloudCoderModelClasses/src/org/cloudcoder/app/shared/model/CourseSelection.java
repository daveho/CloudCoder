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

import java.io.Serializable;


/**
 * Object added to the {@link Session} when a {@link Course} is selected.
 * May specify a {@link Module} within the course, in which case
 * only {@link Problem}s in that module should be shown.
 * 
 * @author David Hovemeyer
 */
public class CourseSelection implements Serializable {
	private static final long serialVersionUID = 1L;

	private Course course;
	private Module module;
	
	/**
	 * Default constructor.
	 */
	public CourseSelection() {
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param course the {@link Course}
	 * @param module the {@link Module}
	 */
	public CourseSelection(Course course, Module module) {
		this.course = course;
		this.module = module;
	}
	
	/**
	 * Set the course.
	 * @param course the course to set
	 */
	public void setCourse(Course course) {
		this.course = course;
	}
	
	/**
	 * Get the course.
	 * @return the course
	 */
	public Course getCourse() {
		return course;
	}
	
	/**
	 * Set the module.
	 * @param module the module to set.
	 */
	public void setModule(Module module) {
		this.module = module;
	}
	
	/**
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CourseSelection)) {
			return false;
		}
		CourseSelection other = (CourseSelection) obj;
		if (!this.course.equals(other.course)) {
			return false;
		}
		return ModelObjectUtil.equals(this.module, other.module);
	}
	
	@Override
	public int hashCode() {
		return course.getId() * 997 + (module != null ? module.getId() : 0);
	}
}
