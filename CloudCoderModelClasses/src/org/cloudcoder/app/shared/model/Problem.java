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
public class Problem extends ProblemData
{
	private static final long serialVersionUID = 1L;

	private Integer problemId;
	private Integer courseId;
	private long whenAssigned;
	private long whenDue;
	
	/**
	 * Number of fields.
	 */
	public static final int NUM_FIELDS = ProblemData.NUM_FIELDS + 4;

	/**
	 * @return the id
	 */
	public Integer getProblemId(){
		return problemId;
	}
	/**
	 * @param id the id to set
	 */
	public void setProblemId(Integer id){
		this.problemId = id;
	}
	
	public Integer getCourseId() {
		return courseId;
	}
	
	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}
	
	/**
	 * @return the whenAssigned
	 */
	public long getWhenAssigned() {
		return whenAssigned;
	}
	
	/**
	 * Get "when assigned" as a java.util.Date.
	 * 
	 * @return "when assigned" as a java.util.Date
	 */
	public Date getWhenAssignedAsDate() {
		return new Date(whenAssigned);
	}
	
	/**
	 * @param whenAssigned the whenAssigned to set
	 */
	public void setWhenAssigned(long whenAssigned) {
		this.whenAssigned = whenAssigned;
	}
	
	/**
	 * @return the whenDue
	 */
	public long getWhenDue() {
		return whenDue;
	}
	
	/**
	 * Get "when due" as a java.util.Date.
	 * 
	 * @return "when due" as a java.util.Date.
	 */
	public Date getWhenDueAsDate() {
		return new Date(whenDue);
	}
	
	/**
	 * @param whenDue the whenDue to set
	 */
	public void setWhenDue(long whenDue) {
		this.whenDue = whenDue;
	}

	@Override
	public String toString() {
		return getProblemId()+" testName: "+getTestName()+" "+getDescription();
	}
}
