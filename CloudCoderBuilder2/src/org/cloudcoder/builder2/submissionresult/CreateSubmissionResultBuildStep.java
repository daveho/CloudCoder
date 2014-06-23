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

package org.cloudcoder.builder2.submissionresult;

import java.util.Properties;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;

/**
 * Create a {@link SubmissionResult} containing all {@link TestResult}s,
 * {@link CompilerDiagnostic}s, and other relevant artifacts produced
 * during building/testing of the {@link BuilderSubmission}.
 * 
 * @author David Hovemeyer
 */
public class CreateSubmissionResultBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		SubmissionResult submissionResult = new SubmissionResult();

		// Get the CompilationResult
		CompilationResult compilationResult = submission.getArtifact(CompilationResult.class);
		if (compilationResult == null) {
			// Create a dummy sucess CompilationResult
			compilationResult = new CompilationResult(CompilationOutcome.SUCCESS);
		}
		submissionResult.setCompilationResult(compilationResult);
		
		// Get the TestResult list
		TestResult[] testResultList = submission.requireArtifact(this.getClass(), TestResult[].class);
		submissionResult.setTestResults(testResultList);
		
		// Adding the SubmissionResult artifact completes building/testing for this submission
		submission.addArtifact(submissionResult);
	}

}
