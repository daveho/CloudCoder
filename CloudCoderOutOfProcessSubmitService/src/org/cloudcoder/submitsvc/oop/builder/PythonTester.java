// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.submitsvc.oop.builder;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonTester implements ITester
{
    public static final Logger logger=LoggerFactory.getLogger(PythonTester.class);
    public static final long TIMEOUT_LIMIT=2000;
    
    static {
        // So far the new system of extracting a PyFunction and passing
        // that and the PythonInterpreter into the KillableThread seems to work.
        // The main concern is that this requires removing any executable code that is
        // not inside a method.
        System.setSecurityManager(
                new ThreadGroupSecurityManager(KillableTaskManager.WORKER_THREAD_GROUP));
    }
    
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
        //TODO: Strip out anything that isn't a function declaration of import statement
        //XXX: If we do that, we disallow global variables, which may be OK
        StringBuilder test = new StringBuilder();
        test.append(programText);
        int spaces=getIndentationIncrementFromPythonCode(programText);
        
        for (TestCase t : testCaseList) {
            // each test case is a function that invokes the function being tested
            test.append("def "+t.getTestCaseName()+"():\n");
            test.append(indent(spaces)+"return "+t.getOutput()+" == "+
                    problem.getTestName()+"("+t.getInput()+")\n");
        }
        return test.toString();
    }
    
    @Override
    public SubmissionResult testSubmission(Submission submission)
    {
        Problem problem=submission.getProblem();
        final String programText=submission.getProgramText();
        List<TestCase> testCaseList=submission.getTestCaseList();
        
        final String s=createTestClassSource(problem, testCaseList, programText);
        final byte[] sBytes=s.getBytes();
        
        //Check if the Python code is syntactically correct
        CompilationResult compres=compilePythonScript(s);
        if (compres.getOutcome()!=CompilationOutcome.SUCCESS) {
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
            // the method
            terp.execfile(new ByteArrayInputStream(sBytes));
            // pull out the function associated with this particular test case
            final PyFunction func=(PyFunction)terp.get(t.getTestCaseName(), PyFunction.class);
            
            tasks.add(new IsolatedTask<TestResult>() {
                @Override
                public TestResult execute() {
                    return executeTestCase(t, True, func);
                }

                
            });
        }
        
        KillableTaskManager<TestResult> pool=new KillableTaskManager<TestResult>(
                tasks, 
                TIMEOUT_LIMIT,
                new KillableTaskManager.TimeoutHandler<TestResult>() {
                    @Override
                    public TestResult handleTimeout() {
                        return new TestResult(TestOutcome.FAILED_FROM_TIMEOUT, 
                                "Took too long!  Check for infinite loops, or recursion without a proper base case");
                    }
                });

        // run each task in a separate thread
        pool.run();

        //merge outcomes with their buffered inputs for stdout/stderr
        
        List<TestResult> testResults=TesterUtils.getStdoutStderr(pool);
        SubmissionResult result=new SubmissionResult(new CompilationResult(CompilationOutcome.SUCCESS));
        result.setTestResults(testResults);
        return result;
    }

    /**
     * @param s
     */
    public static CompilationResult compilePythonScript(final String s) {
        try {
            PythonInterpreter terp=new PythonInterpreter();
            terp.execfile(new ByteArrayInputStream(s.getBytes()));
            return new CompilationResult(CompilationOutcome.SUCCESS);
        } catch (PySyntaxError e) {
            
            logger.info("Failed to compile:\n"+s+"\nwith message: "+e.getMessage());
            
            //TODO: Convert Python error message or stack trace into a list of
            // CompilerDiagnostics to be sent back to the server
            CompilationResult compres=new CompilationResult(CompilationOutcome.FAILURE);
            compres.setException(e);
            return compres;
        } catch (PyException e) {
            logger.warn("Unexpected PyException (probably compilation failure): ");
            CompilationResult compres=new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
            compres.setException(e);
            return compres;
        }
    }
    
    /**
     * @param t
     * @param True
     * @param func
     * @return
     */
    static TestResult executeTestCase(final TestCase t,
            final PyObject True, final PyFunction func) {
        try {
            PyObject r=func.__call__();
            if (r!=null && r.equals(True)) {
                return new TestResult(TestOutcome.PASSED, "Passed! input=" + t.getInput() + ", output=" + t.getOutput());
            } else {
                logger.warn("Test case failed, result is: "+r.getClass());
                return new TestResult(TestOutcome.FAILED_ASSERTION, "Failed for input=" + t.getInput() + ", expected=" + t.getOutput());
            }
        } catch (PyException e) {
            if (e.getCause() instanceof SecurityException) {
                logger.warn("Security exception", e.getCause());
                return new TestResult(TestOutcome.FAILED_BY_SECURITY_MANAGER, "Failed for input=" + t.getInput() + ", expected=" + t.getOutput());
            }
            logger.warn("Exception type was "+e.getClass());
            return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, e.getMessage(), "stdout", "stderr");
        }
    }
}
