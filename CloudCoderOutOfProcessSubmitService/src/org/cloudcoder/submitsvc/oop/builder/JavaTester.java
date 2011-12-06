package org.cloudcoder.submitsvc.oop.builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;

public class JavaTester implements ITester
{
    public List<TestResult> testSubmission(Problem problem, 
            List<TestCase> testCaseList, 
            String programText)
    {
        List<TestResult> testResultList = new ArrayList<TestResult>();

        /*
        // FIXME: fake implementation for now
        TestResult testResult = new TestResult("passed", "You rule, dude", "Hello, world", "Oh yeah");
        testResultList.add(testResult);
        */

        // I *TEST* it!!!

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
        } else {
            ClassLoader cl = fm.getClassLoader(StandardLocation.CLASS_OUTPUT);
            
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
                throw new IllegalStateException("Class not found while testing submission", e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Method not found while testing submission", e);
            } catch (SecurityException e) {
                throw new IllegalStateException("Security exception while testing submission", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Illegal access while testing submission", e);
            }
        }
        
        // TODO: use reflection to call test methods
        
        return testResultList;
    }
}
