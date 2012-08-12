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
 * Interface to be implemented by a model class that contains the
 * data for a single test case.  This interface is implemented
 * by various concrete classes: for example, {@link TestCase}, which
 * represents a test case associated with a {@link Problem} in the
 * webapp, and {@link RepoTestCase}, which represents a test case
 * associated with a {@link RepoProblem} in the repository webapp.
 * The main purpose of the interface is to make it possible to
 * write code to import and export "generic" test case data in a
 * way that works with multiple representations (webapp and repository).
 * 
 * @author David Hovemeyer
 */
public interface ITestCaseData {

	/**
	 * Set the test case name.
	 * @param testCaseName the test case name to set
	 */
	public abstract void setTestCaseName(String testCaseName);

	/**
	 * @return the test case name
	 */
	public abstract String getTestCaseName();

	/**
	 * Set the input to the test case.
	 * @param input the input to the test case
	 */
	public abstract void setInput(String input);

	/**
	 * Get the input to the test case.
	 * @return the input to the test case
	 */
	public abstract String getInput();

	/**
	 * Set the expected output of the test case.
	 * @param output the expected output of the test case
	 */
	public abstract void setOutput(String output);

	/**
	 * Get the expected output of the test case.
	 * @return the expected output of the test case
	 */
	public abstract String getOutput();

	/**
	 * Set whether or not the test case is secret.
	 * @param secret true if the test case is secret, false otherwise
	 */
	public abstract void setSecret(boolean secret);

	/**
	 * Determine whether or not the test case is secret.
	 * @return true if test case is secret, false otherwise
	 */
	public abstract boolean isSecret();

}