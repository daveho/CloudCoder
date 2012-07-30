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

/**
 * Object summarizing student work on a {@link Problem}.
 * 
 * @author David Hovemeyer
 */
public class ProblemSummary {
	private int problemId;
	private String testName;
	private String briefDescription;
	private int numStudents;
	private int numStarted;
	private int numPassedAtLeastOneTest;
	private int numCompleted;
	
	public ProblemSummary() {
		
	}
	
	public void setProblem(Problem problem) {
		this.problemId = problem.getProblemId();
		this.testName = problem.getTestname();
		this.briefDescription = problem.getBriefDescription();
	}
	
	/**
	 * @param numStudents the numStudents to set
	 */
	public void setNumStudents(int numStudents) {
		this.numStudents = numStudents;
	}
	
	/**
	 * @return the numStudents
	 */
	public int getNumStudents() {
		return numStudents;
	}
	
	/**
	 * @param numStarted the numStarted to set
	 */
	public void setNumStarted(int numStarted) {
		this.numStarted = numStarted;
	}
	
	/**
	 * @return the numStarted
	 */
	public int getNumStarted() {
		return numStarted;
	}
	
	/**
	 * @param numPassedAtLeastOneTest the numPassedAtLeastOneTest to set
	 */
	public void setNumPassedAtLeastOneTest(int numPassedAtLeastOneTest) {
		this.numPassedAtLeastOneTest = numPassedAtLeastOneTest;
	}
	
	/**
	 * @return the numPassedAtLeastOneTest
	 */
	public int getNumPassedAtLeastOneTest() {
		return numPassedAtLeastOneTest;
	}
	
	/**
	 * @param numCompleted the numCompleted to set
	 */
	public void setNumCompleted(int numCompleted) {
		this.numCompleted = numCompleted;
	}
	
	/**
	 * @return the numCompleted
	 */
	public int getNumCompleted() {
		return numCompleted;
	}
}
