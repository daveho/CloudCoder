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
 * A problem that has been assigned in a Course.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class Problem extends ProblemData implements IProblem, ActivityObject
{
	private static final long serialVersionUID = 1L;

	private Integer problemId;
	private Integer courseId;
	private long whenAssigned;
	private long whenDue;
	private boolean visible;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema<Problem> SCHEMA = new ModelObjectSchema<Problem>()
		.addAll(IProblem.SCHEMA.getFieldList())
		.addAll(IProblemData.SCHEMA.getFieldList());
	
	/**
	 * Number of fields.
	 */
	public static final int NUM_FIELDS = SCHEMA.getNumFields();
	
	/**
	 * Constructor.
	 */
	public Problem() {
		
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#getProblemId()
	 */
	@Override
	public Integer getProblemId(){
		return problemId;
	}
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#setProblemId(java.lang.Integer)
	 */
	@Override
	public void setProblemId(Integer id){
		this.problemId = id;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#getCourseId()
	 */
	@Override
	public Integer getCourseId() {
		return courseId;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#setCourseId(java.lang.Integer)
	 */
	@Override
	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#getWhenAssigned()
	 */
	@Override
	public long getWhenAssigned() {
		return whenAssigned;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#getWhenAssignedAsDate()
	 */
	@Override
	public Date getWhenAssignedAsDate() {
		return new Date(whenAssigned);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#setWhenAssigned(long)
	 */
	@Override
	public void setWhenAssigned(long whenAssigned) {
		this.whenAssigned = whenAssigned;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#getWhenDue()
	 */
	@Override
	public long getWhenDue() {
		return whenDue;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#getWhenDueAsDate()
	 */
	@Override
	public Date getWhenDueAsDate() {
		return new Date(whenDue);
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#setWhenDue(long)
	 */
	@Override
	public void setWhenDue(long whenDue) {
		this.whenDue = whenDue;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.IProblem#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public String toString() {
		return getProblemId()+" testName: "+getTestname()+" "+getDescription();
	}

	/**
	 * Copy all data in the given Problem object into this one.
	 * 
	 * @param other another Problem object
	 */
	public void copyFrom(Problem other) {
		super.copyFrom(other);
		this.problemId = other.problemId;
		this.courseId = other.courseId;
		this.whenAssigned = other.whenAssigned;
		this.whenDue = other.whenDue;
		this.visible = other.visible;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Problem)) {
			return false;
		}
		Problem other = (Problem) obj;
		return super.equals(other)
				&& ModelObjectUtil.equals(this.problemId, other.problemId)
				&& ModelObjectUtil.equals(this.courseId, other.courseId)
				&& this.whenAssigned == other.whenAssigned
				&& this.whenDue == other.whenDue
				&& this.visible == other.visible;
	}

	public static Problem createEmpty() {
		Problem empty = new Problem();

		// Problem fields
		empty.problemId = null;
		empty.courseId = null;
		empty.whenAssigned = 0L;
		empty.whenDue = 0L;
		empty.visible = false;
		
		// ProblemData fields
		empty.setProblemType(ProblemType.JAVA_METHOD);
		empty.setTestname("");
		empty.setBriefDescription("");
		empty.setDescription("");
		empty.setSkeleton("");
		empty.setSchemaVersion(ProblemData.CURRENT_SCHEMA_VERSION);
		empty.setAuthorName("");
		empty.setAuthorEmail("");
		empty.setAuthorWebsite("");
		empty.setTimestampUtc(System.currentTimeMillis());
		empty.setLicense(ProblemLicense.NOT_REDISTRIBUTABLE);
		
		return empty;
	}
}
