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

package org.cloudcoder.builder2.cfunction;

import java.util.Properties;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.CommandResult;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProcessStatus;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For each {@link CommandResult} produced by a scaffolded {@link ProblemType#C_FUNCTION}
 * executable, check the result to see if the test passed or failed.
 * Creates an array of {@link TestResult} objects (one for each
 * {@link CommandResult}.
 * 
 * @author David Hovemeyer
 */
public class CheckCFunctionCommandResultsBuildStep implements IBuildStep {
	private static final Logger logger = LoggerFactory.getLogger(CheckCFunctionCommandResultsBuildStep.class);

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		CommandResult[] commandResultList = submission.getArtifact(CommandResult[].class);
		if (commandResultList == null) {
			throw new InternalBuilderException(this.getClass(), "No CommandResult list");
		}
		
		Problem problem = submission.getArtifact(Problem.class);
		if (problem == null) {
			throw new InternalBuilderException(this.getClass(), "No Problem");
		}
		
		TestCase[] testCaseList = submission.getArtifact(TestCase[].class);
		if (testCaseList == null) {
			throw new InternalBuilderException(this.getClass(), "No TestCase list");
		}
		
		SecretSuccessAndFailureCodes codes = submission.getArtifact(SecretSuccessAndFailureCodes.class);
		if (codes == null) {
			throw new InternalBuilderException(this.getClass(), "No SecretSuccessAndFailureCodes");
		}
		
		TestResult[] testResultList = new TestResult[commandResultList.length];
		
		for (int i = 0; i < commandResultList.length; i++) {
			CommandResult commandResult = commandResultList[i];
			if (commandResult == null) {
				// Work around extremely weird issue: null CommandResults when
				// a test process is killed.
				logger.error("Null CommandResult for command {}, treating as timeout", i);
				commandResult = new CommandResult(ProcessStatus.TIMED_OUT, "Test process timed out?");
			}
			
			if (commandResult.getStatus() != ProcessStatus.EXITED) {
				// Abnormal process exit
				testResultList[i] = TestResultUtil.createTestResultForAbnormalExit(commandResult, problem, testCaseList[i]);
			} else {
				// Check exit code
				int exitCode = commandResultList[i].getExitCode();
				if (exitCode == codes.getSuccessCode()) {
					testResultList[i] = TestResultUtil.createTestResultForPassedTest(commandResult, problem, testCaseList[i]);
				} else {
					testResultList[i] = TestResultUtil.createTestResultForFailedAssertion(commandResult, problem, testCaseList[i]);
				}
			}
		}
		
		submission.addArtifact(testResultList);
	}

}
