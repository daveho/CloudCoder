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

package org.cloudcoder.builder2.pythonfunction;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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
import org.cloudcoder.builder2.javasandbox.SandboxUtil;
import org.cloudcoder.builder2.javasandbox.TimeoutHandler;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
import org.python.core.PyTraceback;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.__builtin__;
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

	public static final Logger logger = LoggerFactory.getLogger(TestPythonFunctionBuildStep.class);
	public static final long TIMEOUT_LIMIT = 2000;

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		SubmissionResult result = testSubmission(submission);
		submission.addArtifact(result);
	}

	/**
	 * An {@link IsolatedTask} for executing a python test case.
	 */
	private class PythonTestCaseTask implements IsolatedTask<TestResult> {
		private final TestCase tc;
		private final PyFunction func;
		private final PyObject true1;
		private final Problem problem;
		private CompilerDiagnostic compilerDiagnostic;

		private PythonTestCaseTask(TestCase tc, PyFunction func, PyObject true1, Problem problem) {
			this.tc = tc;
			this.func = func;
			this.true1 = true1;
			this.problem = problem;
		}

		@Override
		public TestResult execute() {
			return executeTestCase(problem, tc, true1, func);
		}
		
		public CompilerDiagnostic getCompilerDiagnostic() {
			return compilerDiagnostic;
		}

		/**
		 * Execute a single {@link TestCase}.
		 * 
		 * @param testCase the {@link TestCase}
		 * @param True     Python True object
		 * @param func     the generated testcase function
		 * @return the {@link TestResult}
		 */
		public TestResult executeTestCase(Problem problem, final TestCase testCase, final PyObject True, final PyFunction func) {
			try {
			    // returns a tuple: (result, output)
			    PyObject tuple=func.__call__();
			    PyObject result=tuple.__getitem__(0);
			    String output=tuple.__getitem__(1).toString();
			    logger.trace("Actual output of code submitted for method: "+output);
				if (result!=null && result.equals(True)) {
				    return TestResultUtil.createResultForPassedTest(problem, testCase, output);
				} else {
					logger.warn("Test case failed result  "+tuple.toString());
					// Message returned to user is created here!
					// TODO: Factor out for Java, Python, Ruby one method for creating output
					return TestResultUtil.createResultForFailedTest(problem, testCase, output);
				}
			} catch (PyException e) {
				if (e.getCause() instanceof SecurityException) {
					logger.error("Security exception", e.getCause());
					return new TestResult(TestOutcome.FAILED_BY_SECURITY_MANAGER, "Failed for input=" + testCase.getInput() + ", expected=" + testCase.getOutput());
				}
				logger.info("Exception executing Python submission", e);
				
				// Special case: if it's a NameError, construct a CompilerDiagnostic
				if (isNameError(e)) {
					this.compilerDiagnostic = pyExceptionToCompilerDiagnostic(e);
					return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, "NameError", "stdout", "stderr");
				}
				
				String msg = getExceptionMessage(e);
				return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, msg, "stdout", "stderr");
			}
		}
	}

	/**
	 * Test a {@link ProblemType#PYTHON_FUNCTION} submission.
	 * 
	 * @param submission the submission
	 * @return the {@link SubmissionResult}
	 */
	private SubmissionResult testSubmission(BuilderSubmission submission) {
		final Problem problem = submission.requireArtifact(this.getClass(), Problem.class);
		
		ProgramSource[] programSourceList = submission.requireArtifact(TestPythonFunctionBuildStep.class, ProgramSource[].class);
		if (programSourceList.length > 1) {
			throw new InternalBuilderException(TestPythonFunctionBuildStep.class, "Multiple source files not supported");
		}
		ProgramSource programSource = programSourceList[0];
		
		// Because AddPythonFunctionScaffoldingBuildStep has executed previously,
		// the ProgramSource is the scaffolded source code (ready to execute test cases)
		final String programText = programSource.getProgramText();
		
		TestCase[] testCaseList_ = submission.requireArtifact(TestPythonFunctionBuildStep.class, TestCase[].class);
		List<TestCase> testCaseList= Arrays.asList(testCaseList_);

		final byte[] sBytes = programText.getBytes();

		// Check if the Python code is syntactically correct.
		// Because Python is a dynamic language, this will only find invalid syntax,
		// unknown imports, and similar issues.
		CompilationResult compres = compilePythonScript(problem, programSource);
		if (compres.getOutcome() != CompilationOutcome.SUCCESS) {
			compres.adjustDiagnosticLineNumbers(programSource.getPrologueLength(), programSource.getEpilogueLength());
			return new SubmissionResult(compres);
		}
		
		// Create a Python interpreter, load True from the interpreter
		// then execute our script.
		// Note that our script will have all statements outside of a function
		// stripped out (except for import statements) so no global variables
		final PythonInterpreter terp=new PythonInterpreter();
		final PyObject True=terp.eval("True");
		// won't throw an exception because we checked it at the top of
		// the method to make sure the code will compile
		terp.execfile(new ByteArrayInputStream(sBytes));

		// Create PythonTestCaseTasks, one to execute each test case function
		List<PythonTestCaseTask> tasks=new ArrayList<PythonTestCaseTask>();
		for (final TestCase t : testCaseList) {
		    // pull out the function associated with this particular test case
            final PyFunction func=(PyFunction)terp.get(t.getTestCaseName(), PyFunction.class);
			tasks.add(new PythonTestCaseTask(t, func, True, problem));
		}

		// Create a PythonKillableTaskManager to execute the test case tasks
		PythonKillableTaskManager<TestResult> pool=new PythonKillableTaskManager<TestResult>(
				tasks, 
				TIMEOUT_LIMIT,
				new TimeoutHandler<TestResult>() {
					@Override
					public TestResult handleTimeout() {
						return TestResultUtil.createResultForTimeout();
					}
				},
				terp);

		// run each task in a separate thread
		pool.run();
		
		// Collect any CompilerDiagnostics that may have been reported.
		// Because Python is a dynamic language, some errors that would be
		// found before the program runs in static languages,
		// such as references to undefined variables, are only found
		// at runtime.  We use a HashSet to de-duplicate the CompilerDiagnostics,
		// since the same error may be found by multiple tests.
		List<CompilerDiagnostic> dynamicCompilerDiagnosticList = new ArrayList<CompilerDiagnostic>();
		HashSet<CompilerDiagnostic> seen = new HashSet<CompilerDiagnostic>();
		for (PythonTestCaseTask task : tasks) {
			CompilerDiagnostic diag = task.getCompilerDiagnostic();
			if (diag != null && !seen.contains(diag)) {
				seen.add(diag);
				dynamicCompilerDiagnosticList.add(diag);
			}
		}

		// Merge outcomes with their buffered inputs for stdout/stderr
		List<TestResult> testResults=SandboxUtil.getStdoutStderr(pool);
		
		// Construct a CompilationResult
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.SUCCESS);
		int numDynamicCompilerDiagnostics = dynamicCompilerDiagnosticList.size();
		compilationResult.setCompilerDiagnosticList(dynamicCompilerDiagnosticList.toArray(new CompilerDiagnostic[numDynamicCompilerDiagnostics]));
		compilationResult.adjustDiagnosticLineNumbers(programSource.getPrologueLength(), programSource.getEpilogueLength());
		
		// Construct a SubmissionResult
		SubmissionResult result=new SubmissionResult(compilationResult);
		result.setTestResults(testResults.toArray(new TestResult[testResults.size()]));
		return result;
	}

	/**
	 * "Compile" scaffolded python code to detect syntax errors, missing imports, and
	 * other "static" errors.
	 * 
	 * @param problem       the {@link Problem}
	 * @param programSource the scaffolded code
	 * @return the {@link CompilationResult}
	 */
	private CompilationResult compilePythonScript(Problem problem, ProgramSource programSource) {
		String programText = programSource.getProgramText();
		
		try {
		    logger.info("\n"+programText);
			PythonInterpreter terp=new PythonInterpreter();
			terp.execfile(new ByteArrayInputStream(programText.getBytes()));
			
			// Check to see if the test code actually defines the required
			// function.  If it doesn't, report this as a failed compilation
			// (it isn't really, but attempting to execute any of the
			// test methods will accomplish nothing useful.)
			PyFunction func = (PyFunction)terp.get(problem.getTestname(), PyFunction.class);
			if (func == null) {
				CompilationResult compRes = new CompilationResult(CompilationOutcome.FAILURE);
				int line = programSource.getPrologueLength() + 1;
				CompilerDiagnostic diag = new CompilerDiagnostic(line, line, 1, 1, "Required function " + problem.getTestname() + " was not defined");
				compRes.setCompilerDiagnosticList(new CompilerDiagnostic[]{ diag });
				return compRes;
			}

			// Compilation successful, we should be ready to run the tests
			return new CompilationResult(CompilationOutcome.SUCCESS);
		} catch (PySyntaxError e) {
			logger.info("Failed to compile:\n"+programText+"\nwith message");

			CompilationResult compres = new CompilationResult(CompilationOutcome.FAILURE);
			List<CompilerDiagnostic> diagnostics=convertPySyntaxError(e);
			compres.setCompilerDiagnosticList(diagnostics.toArray(new CompilerDiagnostic[diagnostics.size()]));
			return compres;
		} catch (PyException e) {
			// This happens, for example, when an unknown import is specified.
			logger.warn("PyException attempting to compile python code", e);
			CompilationResult compres=new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
			CompilerDiagnostic diag = pyExceptionToCompilerDiagnostic(e);
			compres.setCompilerDiagnosticList(new CompilerDiagnostic[]{diag});
			return compres;
		}
	}

	/**
	 * Convert a PySyntaxError into a list of {@link CompilerDiagnostic}s.
	 * 
	 * @param e the PySyntaxError
	 * @return list of {@link CompilerDiagnostic}s
	 */
	static List<CompilerDiagnostic> convertPySyntaxError(PySyntaxError e) {
		List<CompilerDiagnostic> diagnostics=new LinkedList<CompilerDiagnostic>();

		//
		// Based on the source for Jython-2.5.2, here's code the 
		// value field of a PySyntaxError:
		//
		//   PyObject[] tmp = new PyObject[] {
		//       new PyString(filename), new PyInteger(line),
		//       new PyInteger(column), new PyString(text)
		//   };
		// 
		//   this.value = new PyTuple(new PyString(s), new PyTuple(tmp));
		//
		// We're going to pull this apart to get out the values we want
		//

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
	
	// The following method is originally from:
	//    http://python.6.x6.nabble.com/Getting-PyException-details-from-Java-td1762496.html
	// I (DHH) updated it to work with more recent Jython versions, and
	// refactored to provide explicit methods for extracting the error type
	// and the error message.
	
	/** 
	 * Returns the exception message, akin to java exception's getMessage() 
	 * method (not supported properly in Jython). 
	 * @param pye a python exception instance 
	 * @return a string containing the python exception's message 
	 */ 
	private static String getExceptionMessage(PyException pye) { 
		// derivative of Jython's Py.formatException() method 

		StringBuffer buf = new StringBuffer(128);
		buf.append(getErrorType(pye)); 
		if (pye.value != Py.None) { 
			buf.append(": ");
			buf.append(getErrorMessage(pye)); 
		} 
		return buf.toString(); 
	}

	/**
	 * Get an error message out of a PyException.
	 * 
	 * @param pye the PyException
	 * @return the error message
	 */
	private static String getErrorMessage(PyException pye) {
		if (pye.value == Py.None) {
			return "Unknown error";
		}
		if (__builtin__.isinstance(pye.value, (PyType) Py.SyntaxError)) { 
			return pye.value.__getitem__(0).__str__().toString();
		} else { 
			return pye.value.__str__().toString();
		}
	}

	/**
	 * Get the error type (e.g., "exceptions.NameError") from a PyException.
	 * 
	 * @param pye the PyException
	 * @return the error type
	 */
	private static String getErrorType(PyException pye) {
		if (pye.type instanceof PyType) { 
			return ((PyType) pye.type).fastGetName();
		} else { 
			return pye.type.__str__().toString();
		}
	}
	
	/**
	 * Determine if the error type returned from {@link #getErrorType(PyException)} is a NameError.
	 * 
	 * @param pye a PyException
	 * @return true if the PyException represents a NameError
	 */
	private boolean isNameError(PyException pye) {
		return getErrorType(pye).equals("exceptions.NameError");
	}
	
	/**
	 * Convert a PyException into a {@link CompilerDiagnostic}.
	 * 
	 * @param pye the PyException
	 * @return a {@link CompilerDiagnostic}
	 */
	private CompilerDiagnostic pyExceptionToCompilerDiagnostic(PyException pye) {
		int line;

		// Use the traceback to get the line number where the error occurred.
		PyTraceback top = getTracebackTop(pye.traceback);
		line = top.tb_lineno;
		
		return new CompilerDiagnostic(line, line, 1, 1, getErrorMessage(pye));
	}

	//
	// From experimentation, it seems that a PyTraceback is a stack trace
	// starting with the bottom of the call stack.  Each tb_next link
	// advances to the next higher (inner) stack frame.  This seems backwards
	// to me (seems like you would start with the top and work down),
	// but I'm assuming there's a good reason.  In any case, we assume
	// that the top item on the stack frame identifies the real error.
	//
	private PyTraceback getTracebackTop(PyTraceback traceback) {
		for (;;) {
			if (traceback.tb_next == null || !(traceback.tb_next instanceof PyTraceback)) {
				return traceback;
			}
			traceback = (PyTraceback) traceback.tb_next;
		}
	}
}
