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

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.builder2.ccompiler.CCompilerBuildStep;
import org.cloudcoder.builder2.commandrunner.CheckCommandResultsUsingRegexBuildStep;
import org.cloudcoder.builder2.commandrunner.ExecuteCommandBuildStep;
import org.cloudcoder.builder2.commandrunner.NativeExecutableToCommandBuildStep;
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
		new NativeExecutableToCommandBuildStep(),
		new ExecuteCommandBuildStep(),
		new CheckCommandResultsUsingRegexBuildStep(),
		new CreateSubmissionResultBuildStep(),
	};
	
	/**
	 * Create a {@link Tester} with the given list of {@link IBuildStep}s.
	 * 
	 * @param stepList list of {@link IBuildStep}s
	 * @return the {@link Tester}
	 */
	public Tester createTester(IBuildStep[] stepList) {
		Tester tester = new Tester();
		for (IBuildStep buildStep : stepList) {
			tester.addBuildStep(buildStep);
		}
		return tester;
	}
}
