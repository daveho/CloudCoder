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

import java.util.Date;

/**
 * Interface describing getters/setters for {@link Problem}.
 * Classes that use this interface instead of directly using Problem
 * can be more flexible because they permit the use of adapter
 * objects.
 * 
 * @author David Hovemeyer
 */
public interface IProblem extends IProblemData {

	/**
	 * @return the id
	 */
	public abstract Integer getProblemId();

	/**
	 * @param id the id to set
	 */
	public abstract void setProblemId(Integer id);

	public abstract Integer getCourseId();

	public abstract void setCourseId(Integer courseId);

	/**
	 * @return the whenAssigned
	 */
	public abstract long getWhenAssigned();

	/**
	 * Get "when assigned" as a java.util.Date.
	 * 
	 * @return "when assigned" as a java.util.Date
	 */
	public abstract Date getWhenAssignedAsDate();

	/**
	 * @param whenAssigned the whenAssigned to set
	 */
	public abstract void setWhenAssigned(long whenAssigned);

	/**
	 * @return the whenDue
	 */
	public abstract long getWhenDue();

	/**
	 * Get "when due" as a java.util.Date.
	 * 
	 * @return "when due" as a java.util.Date.
	 */
	public abstract Date getWhenDueAsDate();

	/**
	 * @param whenDue the whenDue to set
	 */
	public abstract void setWhenDue(long whenDue);

	/**
	 * Set whether or not this Problem is visible to students.
	 * 
	 * @param visible true if this Problem is visible to students, false otherwise
	 */
	public abstract void setVisible(boolean visible);

	/**
	 * @return true if this Problem is visible to students, false otherwise
	 */
	public abstract boolean isVisible();

	/** {@link ModelObjectField} for problem id. */
	public static final ModelObjectField<IProblem, Integer> PROBLEM_ID =
			new ModelObjectField<IProblem, Integer>("problem_id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(IProblem obj, Integer value) { obj.setProblemId(value); }
		public Integer get(IProblem obj) { return obj.getProblemId(); }
	};
	/** {@link ModelObjectField} for course id. */
	public static final ModelObjectField<IProblem, Integer> COURSE_ID =
			new ModelObjectField<IProblem, Integer>("course_id", Integer.class, 0) {
		public void set(IProblem obj, Integer value) { obj.setCourseId(value); }
		public Integer get(IProblem obj) { return obj.getCourseId(); }
	};
	/** {@link ModelObjectField} for assigned date/time. */
	public static final ModelObjectField<IProblem, Long> WHEN_ASSIGNED =
			new ModelObjectField<IProblem, Long>("when_assigned", Long.class, 0) {
		public void set(IProblem obj, Long value) { obj.setWhenAssigned(value); }
		public Long get(IProblem obj) { return obj.getWhenAssigned(); }
	};
	/** {@link ModelObjectField} for due date/time. */
	public static final ModelObjectField<IProblem, Long> WHEN_DUE =
			new ModelObjectField<IProblem, Long>("when_due", Long.class, 0) {
		public void set(IProblem obj, Long value) { obj.setWhenDue(value); }
		public Long get(IProblem obj) { return obj.getWhenDue(); }
	};
	/** {@link ModelObjectField} for visibility to students. */
	public static final ModelObjectField<IProblem, Boolean> VISIBLE =
			new ModelObjectField<IProblem, Boolean>("visible", Boolean.class, 0) {
		public void set(IProblem obj, Boolean value) { obj.setVisible(value); }
		public Boolean get(IProblem obj) { return obj.isVisible(); }
	};
	
	/** Description of fields. */
	public static final ModelObjectSchema<IProblem> SCHEMA = new ModelObjectSchema<IProblem>()
			.add(PROBLEM_ID)
			.add(COURSE_ID)
			.add(WHEN_ASSIGNED)
			.add(WHEN_DUE)
			.add(VISIBLE);
}