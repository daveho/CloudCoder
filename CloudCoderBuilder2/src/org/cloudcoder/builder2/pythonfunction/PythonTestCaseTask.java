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

import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.javasandbox.IsolatedTaskWithCompilerDiagnostic;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;

/**
 * An {@link IsolatedTask} for executing a python test case.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class PythonTestCaseTask implements IsolatedTaskWithCompilerDiagnostic<TestResult> {
	private final TestCase tc;
	private final PyFunction func;
	private final PyObject true1;
	private final Problem problem;
	private CompilerDiagnostic compilerDiagnostic;

	/**
	 * Constructor.
	 * 
	 * @param tc       the {@link TestCase}
	 * @param func     the {@link PyFunction}
	 * @param true1    Python True object
	 * @param problem  the {@link Problem}
	 */
	public PythonTestCaseTask(TestCase tc, PyFunction func, PyObject true1, Problem problem) {
		this.tc = tc;
		this.func = func;
		this.true1 = true1;
		this.problem = problem;
	}

	@Override
	public TestResult execute() {
		return executeTestCase(problem, tc, true1, func);
	}
	
	@Override
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
	private TestResult executeTestCase(Problem problem, final TestCase testCase, final PyObject True, final PyFunction func) {
		try {
		    // returns a tuple: (result, output)
		    PyObject tuple=func.__call__();
		    PyObject result=tuple.__getitem__(0);
		    String output=tuple.__getitem__(1).toString();
		    TestPythonFunctionBuildStep.logger.trace("Actual output of code submitted for method: "+output);
			if (result!=null && result.equals(True)) {
			    return TestResultUtil.createResultForPassedTest(problem, testCase, output);
			} else {
				TestPythonFunctionBuildStep.logger.warn("Test case failed result  "+tuple.toString());
				// Message returned to user is created here!
				// TODO: Factor out for Java, Python, Ruby one method for creating output
				return TestResultUtil.createResultForFailedTest(problem, testCase, output);
			}
		} catch (PyException e) {
			if (e.getCause() instanceof SecurityException) {
				TestPythonFunctionBuildStep.logger.error("Security exception", e.getCause());
				return new TestResult(TestOutcome.FAILED_BY_SECURITY_MANAGER, "Failed for input=" + testCase.getInput() + ", expected=" + testCase.getOutput());
			}
			TestPythonFunctionBuildStep.logger.info("Exception executing Python submission", e);
			
			// Special case: if it's a "compilation" exception, construct a CompilerDiagnostic
			if (PythonUtil.isCompilationException(e)) {
				this.compilerDiagnostic = PythonUtil.pyExceptionToCompilerDiagnostic(e);
				String errorType = PythonUtil.getErrorType(e);
				return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, errorType, "stdout", "stderr");
			}
			
			String msg = PythonUtil.getExceptionMessage(e);
			return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, msg, "stdout", "stderr");
		}
	}
}