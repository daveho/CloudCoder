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

package org.cloudcoder.builder2.rubymethod;

import java.util.Properties;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.StringUtil;

/**
 * Build step to add test scaffolding to a {@link ProblemType#RUBY_METHOD} submission.
 * Replaces the {@link ProgramSource} submission artifact with one that appends
 * the submitted Ruby method and also appends a <code>_test</code> method which takes
 * a test case name as a parameter and returns true or false (passed/failed).
 * 
 * @author David Hovemeyer
 */
public class AddRubyMethodScaffoldingBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		Problem problem = submission.requireArtifact(this.getClass(), Problem.class);
		ProgramSource[] programSourceList = submission.requireArtifact(this.getClass(), ProgramSource[].class);
		
		if (programSourceList.length != 1) {
			throw new InternalBuilderException(this.getClass(), "Only a single source file is supported");
		}
		String programText = programSourceList[0].getProgramText();
		if (!programText.endsWith("\n")) {
			programText = programText + "\n";
		}
		int origNumLines = StringUtil.countLines(programText);
		
		TestCase[] testCaseList = submission.requireArtifact(this.getClass(), TestCase[].class);
		
		StringBuilder buf = new StringBuilder();
		
		// Add method defined in user submission
		buf.append(programText);
		buf.append("\n");

		// Add _test method to execute test with given testname
		// and return a boolean result (passed/failed)
		buf.append("def _test(testname)\n");
		
		// Add cases for each testcase name
		boolean first = true;
		for (TestCase testCase : testCaseList) {
		    /*
def _test(testname)
  if testname == "t0"
    _output=plus(2,3)
    _result=_output == (5)
    return Array.[](_result, _output)
  elsif testname == "t1"
    _output=plus(3,2)
    _result=_output == (5)
    return Array.[](_result, _output)
  end
end
		     */
			buf.append("  ");
			buf.append(first ? "if" : "elsif");
			buf.append(" testname == \"");
			buf.append(testCase.getTestCaseName());
			buf.append("\"\n");
			buf.append("    _output=");
			buf.append(problem.getTestname()+"("+testCase.getInput()+")\n");
			buf.append("_result=(_output == ("+testCase.getOutput()+"))\n");
			buf.append("    return Array.[](_result, _output)\n");
			
			first = false;
		}
		buf.append("  end\n"); // end if
		
		buf.append("end\n"); // end _test method
		
		String scaffoldedProgramText = buf.toString();
		//System.out.println(scaffoldedProgramText);
		
		int scaffoldedNumLines = StringUtil.countLines(scaffoldedProgramText);
		
		int epilogueLength = scaffoldedNumLines - origNumLines;
		
		ProgramSource scaffoldedProgramSource = new ProgramSource(scaffoldedProgramText, 0, epilogueLength);
		submission.addArtifact(new ProgramSource[]{scaffoldedProgramSource});
	}

}
