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
import java.util.List;
import java.util.Properties;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.SandboxUtil;
import org.cloudcoder.builder2.javasandbox.TimeoutHandler;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
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
		List<CompilerDiagnostic> dynamicCompilerDiagnosticList = SandboxUtil.collectDynamicCompilerDiagnostics(tasks);

		// Merge outcomes with their buffered inputs for stdout/stderr
		List<TestResult> testResults=SandboxUtil.getStdoutStderr(pool);
		
		// Construct a CompilationResult
		CompilationResult compilationResult = SandboxUtil.createDynamicCompilationResult(
				programSource, dynamicCompilerDiagnosticList);
		
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
			List<CompilerDiagnostic> diagnostics=PythonUtil.convertPySyntaxError(e);
			compres.setCompilerDiagnosticList(diagnostics.toArray(new CompilerDiagnostic[diagnostics.size()]));
			return compres;
		} catch (PyException e) {
			// This happens, for example, when an unknown import is specified.
			logger.warn("PyException attempting to compile python code", e);
			CompilationResult compres=new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
			CompilerDiagnostic diag = PythonUtil.pyExceptionToCompilerDiagnostic(e);
			compres.setCompilerDiagnosticList(new CompilerDiagnostic[]{diag});
			return compres;
		}
	}
}
