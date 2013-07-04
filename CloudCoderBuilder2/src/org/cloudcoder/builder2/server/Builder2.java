// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builder2.server;

import java.io.IOException;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.csandbox.EasySandboxSharedLibrary;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.CleanupActionStack;
import org.cloudcoder.builder2.model.ICleanupAction;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.model.Tester;
import org.cloudcoder.builder2.tester.TesterFactory;
import org.cloudcoder.builder2.util.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object responsible for building and testing submissions.
 * The {@link #testSubmission(Problem, List, String)} method is thread-safe.
 * 
 * @author David Hovemeyer
 */
public class Builder2 {
	private static final Logger logger=LoggerFactory.getLogger(Builder2.class);
	
	/**
	 * Constructor.
	 */
	public Builder2() {
	}

	/**
	 * Test a submission.
	 * This method is thread-safe, so it may be called from multiple threads
	 * without synchronization.
	 * 
	 * @param problem	  the {@link Problem}
	 * @param testCaseList  the list of {@link TestCase}s
	 * @param programText   the submitted program text
	 * @return a {@link SubmissionResult} for the submission
	 */
	public SubmissionResult testSubmission(Problem problem, List<TestCase> testCaseList, String programText) {
	   SubmissionResult result;
	   try {
		  // Based on the ProblemType, find a Tester
		  Tester tester = TesterFactory.getTester(problem.getProblemType());
		  if (tester == null) {
			 throw new InternalBuilderException(Builder2Server.class, problem.getProblemType() + " problems not supported yet");
		  }

		  // Create and populate a BuilderSubmission
		  BuilderSubmission submission = new BuilderSubmission();
		  submission.addArtifact(problem);
		  submission.addArtifact(ArrayUtil.toArray(testCaseList, TestCase.class));
		  submission.addArtifact(new ProgramSource[]{new ProgramSource(programText)});

		  try {
			 // Build and test
			 tester.execute(submission);

			 // Get the SubmissionResult
			 result = submission.getArtifact(SubmissionResult.class);
			 if (result == null) {
				throw new InternalBuilderException("Tester did not create a SubmissionResult");
			 }
		  } finally {
			 // Clean up all temporary resources created during building/testing
			 submission.executeAllCleanupActions();
		  }
	   } catch (Throwable e) {
		  CompilationResult compres = new CompilationResult(CompilationOutcome.BUILDER_ERROR);
		  logger.error("Internal error building and testing submission", e);
		  result = new SubmissionResult(compres);
		  result.setTestResults(new TestResult[0]);
	   }

	   logger.info("Sending SubmissionResult back to server");

	   if (result.getTestResults() == null) {
		  logger.error("Null TestResult - should not happen");
		  result.setTestResults(new TestResult[0]);
	   } else {
		  logger.info("{} results", result.getTestResults().length);
	   }

	   return result;
	}
}
