package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.submitsvc.oop.builder.JavaTester;
import org.junit.Test;

public class TestJavaCompileFailure extends GenericTest
{
    @Test
    public void test1() {
        problem=createGenericProblem();
        problem.setProblemType(ProblemType.JAVA_METHOD);
        problem.setTestName("compileWillFail");
        
        tester=new JavaTester();
        
        programText="public int sq(int x)  \n" +
                " if (x==1) return 17; \n" +
                " if (x==2) throw new NullPointerException(); \n" +
                " if (x==3) while (true); \n" +
                " if (x==4) new Thread() { public void run() {} }.start(); \n" +
                " if (x==5) return x*x; \n" +
                " if (x==6) System.exit(1); \n" +
                " return x*x; \n" +
                    "}";
        TestCase t=createTestCase("test1", "1", "1");
        TestResult res=testOneSubmission(problem, t, programText);
        System.out.println("OUT: "+res.getStdout());
        System.out.println("ERR: "+res.getStderr());
    }
}
