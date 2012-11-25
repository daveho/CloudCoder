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

import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * A submission to the builder.
 * It will be processed by a series of {@link IBuildStep}s.
 * 
 * @author David Hovemeyer
 */
public class BuilderSubmission {
	private Problem problem;
	private List<TestCase> testCaseList;
	private String programText;
	private IExecutable executable;
	private IExecutionResult executionResult;
	private SubmissionResult submissionResult;
	private List<ICleanupAction> cleanupActionList;

	/**
	 * Constructor.
	 */
	public BuilderSubmission() {
		cleanupActionList = new ArrayList<ICleanupAction>();
	}
	
	/**
	 * Set the {@link Problem}.
	 * 
	 * @param problem the {@link Problem} to set
	 */
	public void setProblem(Problem problem) {
		this.problem = problem;
	}
	
	/**
	 * Set the list of {@link TestCase}s.
	 * 
	 * @param testCaseList the list of {@link TestCase}s to set
	 */
	public void setTestCaseList(List<TestCase> testCaseList) {
		this.testCaseList = testCaseList;
	}
	
	/**
	 * Set the submitted program text.
	 * 
	 * @param programText the submitted program text to set
	 */
	public void setProgramText(String programText) {
		this.programText = programText;
	}
	
	/**
	 * @return the {@link Problem}
	 */
	public Problem getProblem() {
		return problem;
	}

	/**
	 * @return the list of {@link TestCase}s
	 */
	public List<TestCase> getTestCaseList() {
		return testCaseList;
	}
	
	/**
	 * @return the submitted program text
	 */
	public String getProgramText() {
		return programText;
	}
	
	/**
	 * Set the {@link IExecutable} which resulted from compiling the submission.
	 * 
	 * @param executable the {@link IExecutable} to set
	 */
	public void setExecutable(IExecutable executable) {
		this.executable = executable;
	}
	
	/**
	 * Get the {@link IExecutable} which resulted from compiling the submission.
	 * 
	 * @return the {@link IExecutable}
	 */
	public IExecutable getExecutable() {
		return executable;
	}
	
	/**
	 * Set the {@link IExecutionResult} produced by executing the
	 * submission's {@link IExecutable}.
	 * 
	 * @param executionResult the {@link IExecutionResult} to set
	 */
	public void setExecutionResult(IExecutionResult executionResult) {
		this.executionResult = executionResult;
	}
	
	/**
	 * Get the {@link IExecutionResult} produced by executing the
	 * submission's {@link IExecutable}.
	 * 
	 * @return the {@link IExecutionResult}
	 */
	public IExecutionResult getExecutionResult() {
		return executionResult;
	}
	
	/**
	 * Set the {@link SubmissionResult} for this submission.
	 * This marks building and testing of the submission as complete.
	 * 
	 * @param submissionResult the {@link SubmissionResult} to set
	 */
	public void setSubmissionResult(SubmissionResult submissionResult) {
		this.submissionResult = submissionResult;
	}
	
	/**
	 * Get the {@link SubmissionResult} for this submission.
	 * 
	 * @return the {@link SubmissionResult}
	 */
	public SubmissionResult getSubmissionResult() {
		return submissionResult;
	}
	
	/**
	 * Check whether or not this submission is complete
	 * (has a {@link SubmissionResult}).
	 *  
	 * @return true if this submission is complete, false otherwise
	 */
	public boolean isComplete() {
		return submissionResult != null;
	}

	/**
	 * Add an {@link ICleanupAction} to be executed when building/testing
	 * of this submission is complete.
	 * 
	 * @param action the {@link ICleanupAction}
	 */
	public void addCleanupAction(ICleanupAction action) {
		cleanupActionList.add(action);
	}
	
	/**
	 * Get the list of {@link ICleanupAction}s.
	 * Note: they should be executed in reverse order (last action first, first action last).
	 * 
	 * @return the list of {@link ICleanupAction}s
	 */
	public List<ICleanupAction> getCleanupActionList() {
		return cleanupActionList;
	}
}
