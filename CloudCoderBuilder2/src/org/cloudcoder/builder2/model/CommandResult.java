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

package org.cloudcoder.builder2.model;

import java.util.List;

import org.cloudcoder.app.shared.model.TestOutcome;

/**
 * Artifact representing the result of executing a {@link Command} using
 * the input of a particular {@link TestCase}.
 * 
 * @author David Hovemeyer
 */
public class CommandResult {
	private TestOutcome testOutcome;
	private int exitCode;
	private List<String> stdout;
	private List<String> stderr;
	
	/**
	 * Constructor from a {@link TestOutcome} indicating abnormal completion
	 * of the {@link Command}.
	 * 
	 * @param testOutcome the {@link TestOutcome}
	 */
	public CommandResult(TestOutcome testOutcome) {
		this.testOutcome = testOutcome;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param exitCode the command's exit code
	 * @param stdout   the command's standard output lines
	 * @param stderr   the command's standard error lines
	 */
	public CommandResult(int exitCode, List<String> stdout, List<String> stderr) {
		this.exitCode = exitCode;
		this.stdout = stdout;
		this.stderr = stderr;
	}
	
	/**
	 * Get the {@link TestOutcome}.  This is set only if the command did not
	 * complete normally.
	 * 
	 * @return the {@link TestOutcome}
	 */
	public TestOutcome getTestOutcome() {
		return testOutcome;
	}
	
	/**
	 * @return the command's exit code
	 */
	public int getExitCode() {
		return exitCode;
	}
	
	/**
	 * @return the command's standard output lines
	 */
	public List<String> getStdout() {
		return stdout;
	}
	
	/**
	 * @return the command's standard error lines
	 */
	public List<String> getStderr() {
		return stderr;
	}
}
