
package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.submitsvc.oop.builder.JavaTester;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jaimespacco
 *
 */
public class TestJavaTester extends GenericTest
{
    @Before
    public void before() {
        super.before();
        // create a problem
        problem=createGenericProblem();
        problem.setProblemType(ProblemType.JAVA_METHOD);
        problem.setTestName("sq");
        
        tester=new JavaTester();
        programText="public int sq(int x) { \n" +
                " if (x==1) return 17; \n" +
                " if (x==2) throw new NullPointerException(); \n" +
                " if (x==3) while (true); \n" +
                " if (x==4) new Thread() { public void run() {} }.start(); \n" +
                " if (x==5) return x*x; \n" +
                " if (x==6) System.exit(1); \n" +
                " return x*x; \n" +
                    "}";
    }
    
    @Test
    public void test1() {
        addTestCase("test1", "1", "1", TestOutcome.FAILED_ASSERTION);
        runOneTest("test1");
    }
    
    @Test
    public void test2() {
        addTestCase("test2", "2", "4", TestOutcome.FAILED_WITH_EXCEPTION);
        runOneTest("test2");
    }
    
    @Test
    public void test3() {
        addTestCase("test3", "3", "9", TestOutcome.FAILED_FROM_TIMEOUT);
        runOneTest("test3");
    }
    
    @Test
    public void test4() {
        addTestCase("test4", "4", "16", TestOutcome.FAILED_BY_SECURITY_MANAGER);
        runOneTest("test4");
    }
    
    @Test
    public void test5() {
        addTestCase("test5", "5", "25", TestOutcome.PASSED);
        runOneTest("test5");
    }
    
    @Test
    public void test6() {
        addTestCase("test6", "6", "36", TestOutcome.FAILED_BY_SECURITY_MANAGER);
        runOneTest("test6");
    }
    
    @Test
    public void runAllTests() {
        addTestCase("test1", "1", "1", TestOutcome.FAILED_ASSERTION);
        addTestCase("test2", "2", "4", TestOutcome.FAILED_WITH_EXCEPTION);
        addTestCase("test3", "3", "9", TestOutcome.FAILED_FROM_TIMEOUT);
        addTestCase("test4", "4", "16", TestOutcome.FAILED_BY_SECURITY_MANAGER);
        addTestCase("test5", "5", "25", TestOutcome.PASSED);
        super.runAllTests();
    }
}
