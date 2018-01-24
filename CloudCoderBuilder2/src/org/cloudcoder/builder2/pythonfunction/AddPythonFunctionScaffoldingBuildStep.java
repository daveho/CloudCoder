// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012,2018 David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.pythonfunction;

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
 * An {@link IBuildStep} class to add test scaffolding to a {@link ProblemType#PYTHON_FUNCTION}
 * submission.
 * 
 * @author David Hovemeyer
 */
public class AddPythonFunctionScaffoldingBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		// Get original source, problem, and test cases
		ProgramSource[] progSource = submission.requireArtifact(this.getClass(), ProgramSource[].class);
		if (progSource.length != 1) {
			throw new InternalBuilderException(this.getClass(), "Only one python source file is suppored");
		}
		Problem problem = submission.requireArtifact(this.getClass(), Problem.class);
		TestCase[] testCaseList = submission.requireArtifact(this.getClass(), TestCase[].class);

		// Generate the scaffolded source
		ProgramSource scaffoldedSource = addScaffolding(progSource[0], problem, testCaseList);
		
		// Replace the program source with the scaffolded source
		submission.addArtifact(new ProgramSource[]{ scaffoldedSource });
	}

	private ProgramSource addScaffolding(ProgramSource programSource, Problem problem, TestCase[] testCaseList) {
		int prologueLength;
		int epilogueLength;
		int programTextLength;
		
		//TODO: Strip out anything that isn't a function declaration or import statement
		//XXX: If we do that, we disallow global variables, which may be OK
		StringBuilder test = new StringBuilder();
		test.append("import sys\n");
		test.append("import math\n");

		prologueLength = 2; // Keep this up to date with the imports above

		String programText = programSource.getProgramText();
		test.append(programText + "\n");
		programTextLength=StringUtil.countLines(programText);
		int spaces=getIndentationIncrementFromPythonCode(programText);

		// Generate an equality predicate.
		String equalityPredicate = problem.getEqualityPredicate();
		if (equalityPredicate.trim().equals("")) {
			// Generate the default equality predicate.
			// Note the check for floating point values: a delta-based comparison
			// is done rather than requiring strict equality.
			test.append("def eq(_output, _expected):\n");
			test.append(indent(spaces));
			test.append("return (_expected == _output) if (type(_output) != float and type(_expected) != float) else (math.fabs(_output-_expected) < 0.00001)\n");
		} else {
			// Use the Problem's custom equality predicate
			// This is dangerous since the function under test
			// might have used a different indent level.
			// Stupid Python.
			test.append(equalityPredicate);
			test.append("\n");
		}

		for (TestCase t : testCaseList) {
			// each test case is a function that invokes the function being tested
			test.append("def "+t.getTestCaseName()+"():\n");
			
			// 
			// The python functions for individual test cases look like this:
			// 
			// def t0():
			//    _output=plus(2,3)
			//    _expected=<<test case output>>
			//    _result=eq(_output, _expected) 
			//    return (_result, _output)
			//
			// The eq function is the equality predicate, which usually
			// is automatically generated, but could be a custom one
			// if the Problem specifies one.
			// 
			// We return a tuple with a boolean representing whether
			// the test case passed, and a String containing the 
			// actual output.  
			//
			test.append(indent(spaces)+"_output="+problem.getTestname() + 
					"(" +t.getInput()+ ")\n");
			test.append(indent(spaces)+"_expected=" + t.getOutput() + "\n");
			test.append(indent(spaces)+"_result=eq(_output, _expected)\n");
			test.append(indent(spaces)+"return (_result, _output)\n");
		}
		
		// Convert to string, determine epilogue length
		String result=test.toString();
		int totalLen=StringUtil.countLines(result);
		epilogueLength=totalLen-programTextLength;
		
//		System.out.println("Python test scaffolding:");
//		System.out.println(result);
		
		// Done!
		return new ProgramSource(result, prologueLength, epilogueLength);
	}

	private int getIndentationIncrementFromPythonCode(String programText) {
		//TODO: Figure out the indentation scheme of the student submitted programTest
		return 2;
	}

	private String indent(int n) {
		StringBuilder b=new StringBuilder();
		for (int i=0; i<n; i++) {
			b.append(' ');
		}
		return b.toString();
	}
}
