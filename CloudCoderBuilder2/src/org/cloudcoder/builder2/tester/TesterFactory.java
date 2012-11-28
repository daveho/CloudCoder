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

package org.cloudcoder.builder2.tester;

import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.builder2.ccompiler.CCompilerBuildStep;
import org.cloudcoder.builder2.cfunction.AddCFunctionScaffoldingBuildStep;
import org.cloudcoder.builder2.cfunction.CheckCFunctionCommandResultsBuildStep;
import org.cloudcoder.builder2.cfunction.CreateCFunctionTestCommandsBuildStep;
import org.cloudcoder.builder2.cfunction.CreateSecretSuccessAndFailureCodesBuildStep;
import org.cloudcoder.builder2.commandrunner.CheckCommandResultsUsingRegexBuildStep;
import org.cloudcoder.builder2.commandrunner.CreateCommandInputsForEachTestCaseBuildStep;
import org.cloudcoder.builder2.commandrunner.ExecuteCommandForEachCommandInputBuildStep;
import org.cloudcoder.builder2.commandrunner.NativeExecutableToCommandForEachCommandInputBuildStep;
import org.cloudcoder.builder2.javacompiler.JavaCompilerBuildStep;
import org.cloudcoder.builder2.javaprogram.JavaProgramToCommandForEachCommandInputBuildStep;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.Tester;
import org.cloudcoder.builder2.submissionresult.CreateSubmissionResultBuildStep;

/**
 * Factory to create {@link Tester} objects.
 * 
 * @author David Hovemeyer
 */
public class TesterFactory {
	/**
	 * Array of {@link IBuildStep}s needed to test a {@link ProblemType#C_PROGRAM}
	 * submission.
	 */
	public static final IBuildStep[] C_PROGRAM_TESTER_STEPS = {
		new CCompilerBuildStep(),
		new CreateCommandInputsForEachTestCaseBuildStep(),
		new NativeExecutableToCommandForEachCommandInputBuildStep(),
		new ExecuteCommandForEachCommandInputBuildStep(),
		new CheckCommandResultsUsingRegexBuildStep(),
		new CreateSubmissionResultBuildStep(),
	};
	
	/**
	 * Array of {@link IBuildStep}s needed to test a {@link ProblemType#C_FUNCTION}
	 * submission.
	 */
	public static final IBuildStep[] C_FUNCTION_TESTER_STEPS = {
		new AddCFunctionScaffoldingBuildStep(),
		new CCompilerBuildStep(),
		new CreateSecretSuccessAndFailureCodesBuildStep(),
		new CreateCFunctionTestCommandsBuildStep(),
		new ExecuteCommandForEachCommandInputBuildStep(),
		new CheckCFunctionCommandResultsBuildStep(),
		new CreateSubmissionResultBuildStep(),
	};
	
	public static final IBuildStep[] JAVA_PROGRAM_TESTER_STEPS = {
		new JavaCompilerBuildStep(),
		new CreateCommandInputsForEachTestCaseBuildStep(),
		new JavaProgramToCommandForEachCommandInputBuildStep(),
		new ExecuteCommandForEachCommandInputBuildStep(),
		new CheckCommandResultsUsingRegexBuildStep(),
		new CreateSubmissionResultBuildStep(),
	};
	
	/**
	 * Create a {@link Tester} with the given list of {@link IBuildStep}s.
	 * 
	 * @param stepList list of {@link IBuildStep}s
	 * @return the {@link Tester}
	 */
	public static Tester createTester(IBuildStep[] stepList) {
		Tester tester = new Tester();
		for (IBuildStep buildStep : stepList) {
			tester.addBuildStep(buildStep);
		}
		return tester;
	}
	
	/**
	 * Map of {@link ProblemType} values to {@link Tester} objects.
	 */
	public static final Map<ProblemType, Tester> PROBLEM_TYPE_TO_TESTER_MAP = new HashMap<ProblemType, Tester>();
	static {
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.C_PROGRAM, createTester(C_PROGRAM_TESTER_STEPS));
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.C_FUNCTION, createTester(C_FUNCTION_TESTER_STEPS));
		PROBLEM_TYPE_TO_TESTER_MAP.put(ProblemType.JAVA_PROGRAM, createTester(JAVA_PROGRAM_TESTER_STEPS));
		// TODO: Testers for other problem types
	}
}
