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
 * A TestCase for a Problem.
 * Specifies input(s) and expected output.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class TestCase implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int problemId;
	private String testCaseName;
	private String input;
	private String output;
	private boolean secret;
	
	public TestCase() {
		
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	public int getProblemId() {
		return problemId;
	}
	
	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}
	
	public String getTestCaseName() {
		return testCaseName;
	}
	
	public void setInput(String input) {
		this.input = input;
	}
	
	public String getInput() {
		return input;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public String getOutput() {
		return output;
	}
	
	/**
	 * @param secret true if the test case is secret, false otherwise
	 */
	public void setSecret(boolean secret) {
		this.secret = secret;
	}
	
	/**
	 * @return true if test case is secret, false otherwise
	 */
	public boolean isSecret() {
		return secret;
	}
}
