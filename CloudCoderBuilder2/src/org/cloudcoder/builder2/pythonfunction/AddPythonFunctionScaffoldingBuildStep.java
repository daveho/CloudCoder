package org.cloudcoder.builder2.pythonfunction;

import java.util.Properties;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.StringUtil;

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

		for (TestCase t : testCaseList) {
			// each test case is a function that invokes the function being tested
			test.append("def "+t.getTestCaseName()+"():\n");
			
			// 
			// The python functions for individual test cases look like this:
			// 
			// def t0():
			//    _output=plus(2,3)
			//    _expected=<<test case output>>
			//    _result=(_expected == _output) if (type(_output) != float and type(_expected) != float) else (math.fabs(_output-_expected) < 0.00001) 
			//    return (_result, _output)
			// 
			// Note the check for floating point values: a delta-based comparison
			// is done rather than requiring strict equality.
			// 
			// We return a tuple with a boolean representing whether
			// the test case passed, and a String containing the 
			// actual output.  
			//
			test.append(indent(spaces)+"_output="+problem.getTestname() + 
					"(" +t.getInput()+ ")\n");
			test.append(indent(spaces)+"_expected=" + t.getOutput() + "\n");
			test.append(indent(spaces)+"_result=(_expected == _output) if (type(_output) != float and type(_expected) != float) else (math.fabs(_output-_expected) < 0.00001)\n");
			test.append(indent(spaces)+"return (_result, _output)\n");
		}
		
		// Convert to string, determine epilogue length
		String result=test.toString();
		int totalLen=StringUtil.countLines(result);
		epilogueLength=totalLen-programTextLength;
		
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
