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

package org.cloudcoder.builder2.pythonfunction;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.javasandbox.KillableTaskManager;
import org.cloudcoder.builder2.javasandbox.SandboxUtil;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.StringUtil;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test a {@link ProblemType#PYTHON_FUNCTION} submission, producing a
 * {@link SubmissionResult} as a result artifact.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class TestPythonFunctionBuildStep implements IBuildStep {

	public static final Logger logger=LoggerFactory.getLogger(TestPythonFunctionBuildStep.class);
	public static final long TIMEOUT_LIMIT=2000;

	@Override
	public void execute(BuilderSubmission submission) {
		Tester tester = new Tester();
		SubmissionResult result = tester.testSubmission(submission);
		submission.addArtifact(result);
	}

	/*
	 * DHH: this is basically just a literal cut and paste of the
	 * PythonTester from the original builder.  In the future we
	 * should consider splitting this code into discrete build steps,
	 * especially if there are steps that would be useful for
	 * other testers.
	 */
	private static class Tester {
		private int programTextLength;
		private int prologueLength;
		private int epilogueLength;

		protected static int getIndentationIncrementFromPythonCode(String programText) {
			//TODO: Figure out the indentation scheme of the student submitted programTest
			return 2;
		}

		protected static String indent(int n) {
			StringBuilder b=new StringBuilder();
			for (int i=0; i<n; i++) {
				b.append(' ');
			}
			return b.toString();
		}

		protected String createTestClassSource(Problem problem,
				List<TestCase> testCaseList,
				String programText)
		{
			//TODO: Strip out anything that isn't a function declaration or import statement
			//XXX: If we do that, we disallow global variables, which may be OK
			StringBuilder test = new StringBuilder();
			test.append(programText+"\n");
			programTextLength=StringUtil.countLines(programText);
			int spaces=getIndentationIncrementFromPythonCode(programText);

			for (TestCase t : testCaseList) {
				// each test case is a function that invokes the function being tested
				test.append("def "+t.getTestCaseName()+"():\n");
				/* 
				 * The python functions for individual test cases look like this:
				 * 
				 * def t0():
				 *    _output=plus(2,3)
				 *    _result=(5 == _output)
				 *    return (_result, _output)
				 *    
				 * We return a tuple with a boolean representing whether
				 * the test case passed, and a String containing the 
				 * actual output.  
				 */
				test.append(indent(spaces)+"_output="+problem.getTestname() + 
				        "(" +t.getInput()+ ")\n");
				test.append(indent(spaces)+"_result=("+t.getOutput()+" == _output)\n");
				test.append(indent(spaces)+"return (_result, _output)\n");
			}
			String result=test.toString();
			int totalLen=StringUtil.countLines(result);
			prologueLength=0;
			epilogueLength=totalLen-programTextLength;
			return result;
		}

		public SubmissionResult testSubmission(BuilderSubmission submission)
		{
			final Problem problem=submission.getArtifact(Problem.class);
			if (problem == null) {
				throw new InternalBuilderException(this.getClass(), "No Problem");
			}
			
			ProgramSource[] programSourceList = submission.getArtifact(ProgramSource[].class);
			if (programSourceList == null) {
				throw new InternalBuilderException(this.getClass(), "No ProgramSource list");
			}
			
			if (programSourceList.length > 1) {
				throw new InternalBuilderException(this.getClass(), "Multiple source files not supported");
			}
			
			final String programText=programSourceList[0].getProgramText();
			
			TestCase[] testCaseList_ = submission.getArtifact(TestCase[].class);
			if (testCaseList_ == null) {
				throw new InternalBuilderException(this.getClass(), "No TestCase list");
			}
			
			List<TestCase> testCaseList= Arrays.asList(testCaseList_);

			final String s=createTestClassSource(problem, testCaseList, programText);
			final byte[] sBytes=s.getBytes();

			//Check if the Python code is syntactically correct
			CompilationResult compres=compilePythonScript(s);
			if (compres.getOutcome()!=CompilationOutcome.SUCCESS) {
				compres.adjustDiagnosticLineNumbers(prologueLength, epilogueLength);
				return new SubmissionResult(compres);
			}

			List<IsolatedTask<TestResult>> tasks=new ArrayList<IsolatedTask<TestResult>>();
			for (final TestCase t : testCaseList) {
				// Create a Python interpreter, load True from the interpreter
				// then execute our script.
				// Note that our script will have all statements outside of a function
				// stripped out (except for import statements) so no global variables
				final PythonInterpreter terp=new PythonInterpreter();
				final PyObject True=terp.eval("True");
				// won't throw an exception because we checked it at the top of
				// the method to make sure the code will compile
				terp.execfile(new ByteArrayInputStream(sBytes));
				// pull out the function associated with this particular test case
				final PyFunction func=(PyFunction)terp.get(t.getTestCaseName(), PyFunction.class);

				tasks.add(new IsolatedTask<TestResult>() {
					@Override
					public TestResult execute() {
						return executeTestCase(problem, t, True, func);
					}
				});
			}

			KillableTaskManager<TestResult> pool=new KillableTaskManager<TestResult>(
					tasks, 
					TIMEOUT_LIMIT,
					new KillableTaskManager.TimeoutHandler<TestResult>() {
						@Override
						public TestResult handleTimeout() {
							return TestResultUtil.createResultForTimeout();
						}
					});

			// run each task in a separate thread
			pool.run();

			//merge outcomes with their buffered inputs for stdout/stderr

			List<TestResult> testResults=SandboxUtil.getStdoutStderr(pool);
			SubmissionResult result=new SubmissionResult(new CompilationResult(CompilationOutcome.SUCCESS));
			result.setTestResults(testResults.toArray(new TestResult[testResults.size()]));
			return result;
		}

		/**
		 * @param programText
		 */
		public static CompilationResult compilePythonScript(final String programText) {
			try {
			    logger.info("\n"+programText);
				PythonInterpreter terp=new PythonInterpreter();
				terp.execfile(new ByteArrayInputStream(programText.getBytes()));
				return new CompilationResult(CompilationOutcome.SUCCESS);
			} catch (PySyntaxError e) {

				logger.info("Failed to compile:\n"+programText+"\nwith message");

				//TODO: Convert Python error message or stack trace into a list of
				// CompilerDiagnostics to be sent back to the server
				CompilationResult compres=new CompilationResult(CompilationOutcome.FAILURE);
				List<CompilerDiagnostic> diagnostics=convertPySyntaxError(e);
				compres.setCompilerDiagnosticList(diagnostics.toArray(new CompilerDiagnostic[diagnostics.size()]));
				//compres.setException(e);
				return compres;
			} catch (PyException e) {
				logger.warn("Unexpected PyException (probably compilation failure): ");
				CompilationResult compres=new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
				//compres.setException(e);
				return compres;
			}
		}

		/**
		 * @param e
		 * @return
		 */
		static List<CompilerDiagnostic> convertPySyntaxError(PySyntaxError e) {
			List<CompilerDiagnostic> diagnostics=new LinkedList<CompilerDiagnostic>();

			/*
			 * Based on the source for Jython-2.5.2, here's code the 
			 * value field of a PySyntaxError:
			 * 


        PyObject[] tmp = new PyObject[] {
            new PyString(filename), new PyInteger(line),
            new PyInteger(column), new PyString(text)
        };

        this.value = new PyTuple(new PyString(s), new PyTuple(tmp));

			 * We're going to pull this apart to get out the values we want
			 *
			 */

			PyTuple tuple=(PyTuple)e.value;

			String msg=tuple.get(0).toString();
			PyTuple loc=(PyTuple)tuple.get(1);
			//String filename=(String)loc.get(0);
			int lineNum=(Integer)loc.get(1);
			int colNum=(Integer)loc.get(2);
			//String text=(String)loc.get(3);

			CompilerDiagnostic d=new CompilerDiagnostic(lineNum, lineNum, colNum, colNum, msg);

			diagnostics.add(d);
			return diagnostics;
		}

		/**
		 * @param testCase
		 * @param True
		 * @param func
		 * @return
		 */
		static TestResult executeTestCase(Problem problem, final TestCase testCase, final PyObject True, final PyFunction func) 
		{
			try {
			    // returns a tupe: (result, output)
			    PyObject tuple=func.__call__();
			    PyObject result=tuple.__getitem__(0);
			    String output=tuple.__getitem__(1).toString();
			    logger.trace("Actual output of code submitted for method: "+output);
				if (result!=null && result.equals(True)) {
				    return TestResultUtil.createResultForPassedTest(problem, testCase);
				} else {
					logger.warn("Test case failed result  "+tuple.toString());
					// Message returned to user is created here!
					// TODO: Factor out for Java, Python, Ruby one method for creating output
					return TestResultUtil.createResultForFailedTest(problem, testCase, output);
				}
			} catch (PyException e) {
				if (e.getCause() instanceof SecurityException) {
					logger.warn("Security exception", e.getCause());
					return new TestResult(TestOutcome.FAILED_BY_SECURITY_MANAGER, "Failed for input=" + testCase.getInput() + ", expected=" + testCase.getOutput());
				}
				logger.warn("Exception type was "+e.getClass());
				return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, e.getMessage(), "stdout", "stderr");
			}
		}
	}
}
