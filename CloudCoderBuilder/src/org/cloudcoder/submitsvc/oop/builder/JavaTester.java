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

package org.cloudcoder.submitsvc.oop.builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//import org.cloudcoder.app.shared.model.Bob;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTester implements ITester
{
    private static final Logger logger=LoggerFactory.getLogger(JavaTester.class);
    public static final long TIMEOUT_LIMIT=2000;
    
    private static void loadClasses() {
        // TODO:  Huge hack!  Somehow TestResult was not being loaded by
        // the correct classloader because it was first referenced inside the
        // the body of the inner class for IsolatedTask
        // XXX I honestly have no idea at all why this is necessary
        try {
        	// Force TestResult, TestOutcome, and any nested classes referenced
        	// by their static initializers to load
        	TestResult tr = new TestResult();
        	tr.setOutcome(TestOutcome.PASSED);
        	logger.info("test outcome hack: {}", tr.getOutcome().toString());
//            ClassLoader.getSystemClassLoader().loadClass("org.cloudcoder.app.shared.model.TestResult");
//            for (int i=1; i<=6; i++) {
//                ClassLoader.getSystemClassLoader().loadClass("org.cloudcoder.app.shared.model.TestResult$"+i);
//            }
//            ClassLoader.getSystemClassLoader().loadClass("org.cloudcoder.app.shared.model.TestOutcome");
        } catch (Exception e) {
            System.out.println(e);
        }
        System.setSecurityManager(new ThreadGroupSecurityManager(KillableTaskManager.WORKER_THREAD_GROUP));
    }
    
    static {
        loadClasses();
    }
    
    public SubmissionResult testSubmission(Submission submission)
    {
        Problem problem=submission.getProblem();
        final String programText=submission.getProgramText();
        List<TestCase> testCaseList=submission.getTestCaseList();

        // The Test class is the subject of the test
        String testCode = createTestClassSource(programText);
        
        // The Tester class contains the unit tests
        // FIXME: this could be cached
        String testerCode = createTesterClassSource(problem, testCaseList);
        
        InMemoryJavaCompiler compiler=new InMemoryJavaCompiler();
        compiler.addClassFile("Test", testCode);
        compiler.addClassFile("Tester", testerCode);
        if (!compiler.compile()) {
            return new SubmissionResult(compiler.getCompileResult());
        }

        // create a list of tasks to be executed
        List<IsolatedTask<TestResult>> tasks=new ArrayList<IsolatedTask<TestResult>>();
        final Class<?> testerCls=compiler.getClass("Tester");
        //Bob b=new Bob(7);
        //System.out.println(b.getX());
        for (final TestCase t : testCaseList) {
            tasks.add(new IsolatedTaskRunner(testerCls, t));
            /*
            tasks.add(new IsolatedTask<TestResult>() {
                @Override
                public TestResult execute() {
                    try {
                        Method m = testerCls.getMethod(t.getTestCaseName());
                        Boolean result = (Boolean) m.invoke(null);
                        Bob b=new Bob(5);
                        System.out.println(b.getX());
                        if (result) {
                            return new TestResult(TestOutcome.PASSED, "Passed! input=" + t.getInput() + ", output=" + t.getOutput());
                        } else {
                            return new TestResult(TestOutcome.FAILED_ASSERTION, "Failed for input=" + t.getInput() + ", expected=" + t.getOutput());
                        }
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof SecurityException) {
                            logger.warn("Security exception with code: "+programText);
                            return new TestResult(TestOutcome.FAILED_BY_SECURITY_MANAGER, "Security exception while testing submission");
                        } 
                        logger.warn("InvocationTargetException", e);
                        return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, "Failed with "+e.getTargetException().getMessage());
                    } catch (NoSuchMethodException e) {
                        return new TestResult(TestOutcome.INTERNAL_ERROR, "Method not found while testing submission");
                    } catch (IllegalAccessException e) {
                        return new TestResult(TestOutcome.INTERNAL_ERROR, "Illegal access while testing submission");
                    }
                    //TODO: Catch Throwable and report INTERNAL_ERROR for anything else
                }
            });
            */
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

        // merge outcomes with their buffered inputs for stdout/stderr
        List<TestResult> outcomes=TesterUtils.getStdoutStderr(pool);
        SubmissionResult result=new SubmissionResult(new CompilationResult(CompilationOutcome.SUCCESS));
        result.setTestResults(outcomes.toArray(new TestResult[outcomes.size()]));
        logger.info("Sending back to server "+result.getTestResults().length+" results");
        return result;
    }

    private static class IsolatedTaskRunner implements IsolatedTask<TestResult>
    {
        private Class<?> theClass;
        private TestCase testCase;
        
        public IsolatedTaskRunner(Class<?> theClass, TestCase testCase) {
            this.theClass=theClass;
            this.testCase=testCase;
        }

        @Override
        public TestResult execute() {
            try {
                Method m = theClass.getMethod(testCase.getTestCaseName());
                Boolean result = (Boolean) m.invoke(null);
                if (result) {
                    return new TestResult(TestOutcome.PASSED, "Passed! input=" + testCase.getInput() + ", output=" + testCase.getOutput());
                } else {
                    return new TestResult(TestOutcome.FAILED_ASSERTION, "Failed for input=" + testCase.getInput() + ", expected=" + testCase.getOutput());
                }
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof SecurityException) {
                    //logger.warn("Security exception with code: "+programText);
                    return new TestResult(TestOutcome.FAILED_BY_SECURITY_MANAGER, "Security exception while testing submission");
                } 
                logger.warn("InvocationTargetException", e);
                return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION, "Failed with "+e.getTargetException().getMessage());
            } catch (NoSuchMethodException e) {
                return new TestResult(TestOutcome.INTERNAL_ERROR, "Method not found while testing submission");
            } catch (IllegalAccessException e) {
                return new TestResult(TestOutcome.INTERNAL_ERROR, "Illegal access while testing submission");
            }
            //TODO: Catch Throwable and report INTERNAL_ERROR for anything else
        }
        
    }

    /**
     * @param problem
     * @param testCaseList
     * @return
     */
    private String createTesterClassSource(Problem problem,
            List<TestCase> testCaseList) {
        StringBuilder tester = new StringBuilder();
        tester.append("public class Tester {\n");
        tester.append("\tpublic static boolean eq(Object o1, Object o2) { return o1.equals(o2); }\n");
        for (TestCase tc : testCaseList) {
            tester.append("\tpublic static boolean ");
            tester.append(tc.getTestCaseName());
            tester.append("() {\n");
            tester.append("\t\tTest t = new Test();\n");
            tester.append("\t\treturn eq(t." + problem.getTestname() + "(" + tc.getInput() + "), " + tc.getOutput() + ");\n");
            tester.append("\t\t}\n");
        }
        tester.append("}");
        String testerCode = tester.toString();
        return testerCode;
    }

    /**
     * @param programText
     * @return
     */
    private String createTestClassSource(String programText) {
        StringBuilder test = new StringBuilder();
        test.append("public class Test {\n");
        test.append(programText + "\n");
        test.append("}\n");
        String testCode = test.toString();
        return testCode;
    }
    
}
