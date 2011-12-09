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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class PythonTester implements ITester
{
    public static final long TIMEOUT_LIMIT=2000;
    
    protected static int getSpaceIncrementFromPythonCode(String programText) {
        return 2;
    }
    
    protected static String spaces(int n) {
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
        StringBuilder test = new StringBuilder();
        test.append(programText);
        int spaces=getSpaceIncrementFromPythonCode(programText);
        
        test.append("class Tester:\n");
        test.append(spaces(spaces)+"def __init__(self):\n");
        test.append(spaces(spaces*2)+"pass\n");
        
        for (TestCase t : testCaseList) {
            test.append(spaces(spaces)+"def "+t.getTestCaseName()+"(self):\n");
            test.append(spaces(spaces*2)+"return "+t.getOutput()+" == "+
                    problem.getTestName()+"("+t.getInput()+")\n");
        }
        return test.toString();
    }
    
    @Override
    public List<TestResult> testSubmission(final Problem problem,
            List<TestCase> testCaseList, 
            final String programText)
    {
        final PythonInterpreter terp=new PythonInterpreter();
        final PyObject True=terp.eval("True");
        
        String s=createTestClassSource(problem, testCaseList, programText);
        PyCode code=terp.compile(s);
        terp.eval(code);
        
        List<IsolatedTask<TestResult>> tasks=new ArrayList<IsolatedTask<TestResult>>();

        for (final TestCase t : testCaseList) {
            tasks.add(new IsolatedTask<TestResult>() {
                @Override
                public TestResult execute() {
                    try {
                        PyObject r=terp.eval("Tester()."+t.getTestCaseName()+"()");
                        if (r!=null && r.equals(True)) {
                            return new TestResult(TestOutcome.PASSED, "Passed! input=" + t.getInput() + ", output=" + t.getOutput());
                        } else {
                            return new TestResult(TestOutcome.FAILED_ASSERTION, "Failed for input=" + t.getInput() + ", expected=" + t.getOutput());
                        }
                    } catch (PyException e) {
                        return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, e.getMessage(), "stdout", "stderr");
                    }
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
        List<TestResult> outcomes=pool.getOutcomes();
        Map<Integer,String> stdout=pool.getBufferedStdout();
        Map<Integer,String> stderr=pool.getBufferedStderr();
        for (int i=0; i<outcomes.size(); i++) {
            TestResult t=outcomes.get(i);
            if (t!=null) {
                t.setStdout(stdout.get(i));
                t.setStderr(stderr.get(i));
            }
        }
        return outcomes;
    }
}
