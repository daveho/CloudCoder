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

import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.builder2.javasandbox.IsolatedTask;
import org.cloudcoder.builder2.javasandbox.IsolatedTaskWithCompilerDiagnostic;
import org.cloudcoder.builder2.util.TestResultUtil;
import org.jruby.RubyArray;
import org.jruby.embed.InvokeFailedException;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test a {@link ProblemType#RUBY_METHOD} submission on a given 
 * {@link TestCase}, producing a {@link TestResult}.
 * This object should be used in an {@link IsolatedTask}.
 * 
 * @author David Hovemeyer
 */
public class RubyTester implements IsolatedTaskWithCompilerDiagnostic<TestResult> {
	private static Logger logger = LoggerFactory.getLogger(RubyTester.class);
	
	private ScriptingContainer container;
	private Object receiver;
	private Problem problem;
	private TestCase testCase;
	private CompilerDiagnostic compilerDiagnostic;
	
	/**
	 * Constructor.
	 * 
	 * @param container  the {@link ScriptingContainer}
	 * @param receiver   the test scriptlet receiver object
	 * @param problem    the {@link Problem}
	 * @param testCase   the {@link TestCase} to execute
	 */
	public RubyTester(ScriptingContainer container, Object receiver, Problem problem, TestCase testCase) {
		this.container = container;
		this.receiver = receiver;
		this.problem = problem;
		this.testCase = testCase;
	}

	@Override
	public TestResult execute() {
		Object result_;
		try {
			result_ = container.callMethod(receiver, "_test", testCase.getTestCaseName());
		} catch (InvokeFailedException e) {
			logger.info("Failed to invoke Ruby test method", e);
			
			// Do some forensics to see if we can construct a decent diagnostic.
			// In particular, for referring to a nonexistent name, create
			// a CompilerDiagnostic.

			Throwable cause_ = e.getCause();
			String message;
			if (cause_ != null && cause_ instanceof RaiseException) {
				RaiseException cause = (RaiseException) cause_;
				StackTraceElement[] stackTrace = cause.getStackTrace();
				int line = stackTrace[0].getLineNumber();
				compilerDiagnostic = new CompilerDiagnostic(line, line, 1, 1, cause.getMessage());
				message = cause.getMessage();
			} else {
				message = e.getMessage();
			}
			
			return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, message, "stdout", "stderr");
		} catch (Throwable t) {
			logger.info("Unknown exception invoking Ruby test method", t);
			return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, t.getMessage(), "stdout", "stderr");
		}
		
		if (!(result_ instanceof RubyArray)) {
		    return new TestResult(TestOutcome.INTERNAL_ERROR, "_test method did not return an Array result");
        }
        RubyArray arr=(RubyArray)result_;
        
        Boolean testPassed = (Boolean)arr.get(0);
        String output = arr.get(1).toString();
        
		return testPassed
				? TestResultUtil.createResultForPassedTest(problem, testCase, output)
				: TestResultUtil.createResultForFailedTest(problem, testCase, output);
	}

	/**
	 * Return the {@link CompilerDiagnostic}, if any.  For certain types
	 * of runtime errors, such as NameErrors, we create a CompilerDiagnostic
	 * because the error really indicates a "static" bug in the program.
	 * 
	 * @return the {@link CompilerDiagnostic}, or null if there is none
	 */
	public CompilerDiagnostic getCompilerDiagnostic() {
		return compilerDiagnostic;
	}
}
