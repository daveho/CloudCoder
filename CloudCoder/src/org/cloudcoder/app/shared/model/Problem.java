// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;

import java.io.Serializable;

//@Entity
//@Table(name="problems")
public class Problem implements Serializable
{
	private static final long serialVersionUID = 1L;

	//	@Id 
//	@GeneratedValue(strategy=GenerationType.AUTO)
//	@Column(name="problem_id")
	private Integer problemId;
	
//	@Column(name="course_id")
	private Integer courseId;
	
	private ProblemType problemType;

//	@Column(name="testname")
	private String testName;

//	@Column(name="brief_description")
	private String briefDescription;
	
//	@Column(name="description")
	private String description;

	public String toString() {
		return getProblemId()+" testName: "+getTestName()+" "+getDescription();
	}

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
	
	public void setProblemType(ProblemType problemType) {
		this.problemType = problemType;
	}
	
	public void setProblemType(int problemType) {
		this.problemType = ProblemType.values()[problemType];
	}
	
	public ProblemType getProblemType() {
		return problemType;
	}
	
	/**
	 * @return the testName
	 */
	public String getTestName(){
		return testName;
	}
	/**
	 * @param testName the testName to set
	 */
	public void setTestName(String testName){
		this.testName = testName;
	}
	
	public void setBriefDescription(String briefDescription) {
		this.briefDescription = briefDescription;
	}
	
	public String getBriefDescription() {
		return briefDescription;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription(){
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description){
		this.description = description;
	}
}
