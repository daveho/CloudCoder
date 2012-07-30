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
import java.util.Arrays;

/**
 * Model object representing the registration of a
 * {@link User} in a {@link Course}.
 * 
 * @author David Hovemeyer
 */
public class CourseRegistration implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int courseId;
	private int userId;
	private CourseRegistrationType registrationType;
	private int section;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema SCHEMA = new ModelObjectSchema(Arrays.asList(
			new ModelObjectField("id", Integer.class, 0, ModelObjectIndexType.IDENTITY),
			new ModelObjectField("course_id", Integer.class, 0),
			new ModelObjectField("user_id", Integer.class, 0),
			new ModelObjectField("registration_type", CourseRegistrationType.class, 0),
			new ModelObjectField("section", Integer.class, 0)
	));
	
	/**
	 * Constructor.
	 */
	public CourseRegistration() {
		
	}
	
	/**
	 * Set the unique id.
	 * @param id the unique id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Get the unique id.
	 * @return the unique id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set the course id.
	 * @param courseId the course id
	 */
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	
	/**
	 * Get the course id.
	 * @return the course id
	 */
	public int getCourseId() {
		return courseId;
	}
	
	/**
	 * Set the user id.
	 * @param userId the user id
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	/**
	 * Get the user id.
	 * @return the user id
	 */
	public int getUserId() {
		return userId;
	}
	
	/**
	 * Get the {@link CourseRegistrationType} (i.e., student or instructor).
	 * @return the CourseRegistrationType
	 */
	public CourseRegistrationType getRegistrationType() {
		return registrationType;
	}
	
	/**
	 * Get the integer value of the {@link CourseRegistrationType}.
	 * @return the integer value of the CourseRegistrationType
	 */
	public int getRegistrationTypeAsInt() {
		return registrationType.ordinal();
	}
	
	/**
	 * Set the {@link CourseRegistrationType}.
	 * @param registrationType the CourseRegistrationType
	 */
	public void setRegistrationType(CourseRegistrationType registrationType) {
		this.registrationType = registrationType;
	}
	
	/**
	 * Set the {@link CourseRegistrationType} given its integer value (ordinal).
	 * @param registrationType the integer value of the CourseRegistrationType
	 */
	public void setRegistrationType(int registrationType) {
		this.registrationType = CourseRegistrationType.values()[registrationType];
	}
	
	/**
	 * Set the course section.
	 * @param section the section to set
	 */
	public void setSection(int section) {
		this.section = section;
	}
	
	/**
	 * Get the course section.
	 * @return the section the section to set.
	 */
	public int getSection() {
		return section;
	}
	
}
