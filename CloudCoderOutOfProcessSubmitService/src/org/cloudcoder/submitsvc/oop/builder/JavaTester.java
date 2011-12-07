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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;

public class JavaTester implements ITester
{
    public static final long TIMEOUT_LIMIT=2000;
    
    public List<TestResult> testSubmission(Problem problem, 
            List<TestCase> testCaseList, 
            String programText)
    {
        List<TestResult> testResultList = new ArrayList<TestResult>();

        // The Test class is the subject of the test
        StringBuilder test = new StringBuilder();
        test.append("public class Test {\n");
        test.append(programText + "\n");
        test.append("}\n");
        
        // The Tester class contains the unit tests
        // FIXME: this could be cached
        StringBuilder tester = new StringBuilder();
        tester.append("public class Tester {\n");
        
        tester.append("\tpublic static boolean eq(Object o1, Object o2) { return o1.equals(o2); }\n");
        
        for (TestCase tc : testCaseList) {
            tester.append("\tpublic static boolean ");
            tester.append(tc.getTestCaseName());
            tester.append("() {\n");
            tester.append("\t\tTest t = new Test();\n");
            tester.append("\t\treturn eq(t." + problem.getTestName() + "(" + tc.getInput() + "), " + tc.getOutput() + ");\n");
            tester.append("\t\t}\n");
        }
        tester.append("}");

        String testCode = test.toString();
        String testerCode = tester.toString();
        
        System.out.println("Test code:");
        System.out.println(testCode);
        System.out.println("Tester code:");
        System.out.println(testerCode);
        
        // Compile
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<JavaFileObject> sources = new ArrayList<JavaFileObject>();
        sources.add(MemoryFileManager.makeSource("Test", testCode));
        sources.add(MemoryFileManager.makeSource("Tester", testerCode));
        
        MemoryFileManager fm = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
        // FIXME: should get diagnostics so we can report them
        CompilationTask task = compiler.getTask(null, fm, null, null, null, sources);
        if (!task.call()) {
            // FIXME: proper reporting of failure
            testResultList.add(new TestResult(TestOutcome.INTERNAL_ERROR, "Compile error"));
            return testResultList;
        }
        ClassLoader cl = fm.getClassLoader(StandardLocation.CLASS_OUTPUT);
        
        try {
            final Class<?> testerCls = cl.loadClass("Tester");

            // create a list of tasks to be executed
            List<IsolatedTask<TestResult>> tasks=new ArrayList<IsolatedTask<TestResult>>();

            for (final TestCase t : testCaseList) {
                tasks.add(new IsolatedTask<TestResult>() {
                    @Override
                    public TestResult execute() {
                        try {
                            Method m = testerCls.getMethod(t.getTestCaseName());
                            Boolean result = (Boolean) m.invoke(null);
                            if (result) {
                                return new TestResult(TestOutcome.PASSED, "Passed! input=" + t.getInput() + ", output=" + t.getOutput());
                            } else {
                                return new TestResult(TestOutcome.FAILED_ASSERTION, "Failed for input=" + t.getInput() + ", expected=" + t.getOutput());
                            }
                        } catch (InvocationTargetException e) {
                            return new TestResult(TestOutcome.INTERNAL_ERROR, "Unable to invole test method "+t.getTestCaseName());
                        } catch (NoSuchMethodException e) {
                            return new TestResult(TestOutcome.INTERNAL_ERROR, "Method not found while testing submission");
                        } catch (SecurityException e) {
                            return new TestResult(TestOutcome.INTERNAL_ERROR, "Security exception while testing submission");
                        } catch (IllegalAccessException e) {
                            return new TestResult(TestOutcome.INTERNAL_ERROR, "Illegal access while testing submission");
                        }
                        //TODO: Catch Throwable and report an error
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
                t.setStdout(stdout.get(i));
                t.setStderr(stderr.get(i));
            }
            return outcomes;
        } catch (ClassNotFoundException e) {
            testResultList.add(new TestResult(TestOutcome.INTERNAL_ERROR,"Class not found exception: "+e.getMessage()));
            return testResultList;
        }
    }

    /**
     * @param testCaseList
     * @param testResultList
     * @param cl
     */
    private List<TestResult> testSubmission(List<TestCase> testCaseList, ClassLoader cl) 
    {
        List<TestResult> testResultList=new LinkedList<TestResult>();
        try {
            Class<?> testerCls = cl.loadClass("Tester");
            
            // Compilation succeeded: now for the testing
            for (TestCase tc : testCaseList) {
                Method m = testerCls.getMethod(tc.getTestCaseName());
                try {
                    Boolean result = (Boolean) m.invoke(null);
                    // TODO: capture stdout and stderr
                    //testResultList.add(new TestResult(result ? TestResult.PASSED : TestResult.FAILED_ASSERTION, ));
                    if (result) {
                        testResultList.add(new TestResult(TestOutcome.PASSED, "Passed! input=" + tc.getInput() + ", output=" + tc.getOutput()));
                    } else {
                        testResultList.add(new TestResult(TestOutcome.FAILED_ASSERTION, "Failed for input=" + tc.getInput() + ", expected=" + tc.getOutput()));
                    }
                } catch (InvocationTargetException e) {
//                      throw new IllegalStateException("Invocation target exception while testing submission", e);
                    testResultList.add(new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, "Failed (exception) for input=" + tc.getInput() + ", expected=" + tc.getOutput()));
                }
            }
        } catch (ClassNotFoundException e) {
            testResultList.add(new TestResult(TestOutcome.INTERNAL_ERROR, "Class not found while testing submission"));
        } catch (NoSuchMethodException e) {
            testResultList.add(new TestResult(TestOutcome.INTERNAL_ERROR, "Method not found while testing submission"));
        } catch (SecurityException e) {
            testResultList.add(new TestResult(TestOutcome.INTERNAL_ERROR, "Security exception while testing submission"));
        } catch (IllegalAccessException e) {
            testResultList.add(new TestResult(TestOutcome.INTERNAL_ERROR, "Illegal access while testing submission"));
        }
        return testResultList;
    }
}
