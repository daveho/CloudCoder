
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
public class TestJavaTester extends GenericTest//TestSq
{
    @Before
    public void before() {
        createProblem("sq", ProblemType.JAVA_METHOD);
        
        tester=new JavaTester();

        setProgramText("public int sq(int x) { \n" +
                " if (x==1) return 17; \n" +
                " if (x==2) throw new NullPointerException(); \n" +
                " if (x==3) while (true); \n" +
                " if (x==4) new Thread() { public void run() {} }.start(); \n" +
                " if (x==5) return x*x; \n" +
                " if (x==6) System.exit(1); \n" +
                " return x*x; \n" +
                    "}");
    }
    
    @Test
    public void test1() {
        runOneTestCase("test1", "1", "1", TestOutcome.FAILED_ASSERTION);
    }
    
    @Test
    public void test2() {
        runOneTestCase("test2", "2", "4", TestOutcome.FAILED_WITH_EXCEPTION);
    }
    
    @Test
    public void test3() {
        runOneTestCase("test3", "3", "9", TestOutcome.FAILED_FROM_TIMEOUT);
    }
    
    @Test
    public void test4() {
        runOneTestCase("test4", "4", "16", TestOutcome.FAILED_BY_SECURITY_MANAGER);
    }
    
    @Test
    public void test5() {
        runOneTestCase("test5", "5", "25", TestOutcome.PASSED);
    }
    
    @Test
    public void test6() {
        runOneTestCase("test6", "6", "36", TestOutcome.FAILED_BY_SECURITY_MANAGER);
    }
    
    @Test
    public void runAllTests() {
        addTestCaseAndOutcome("test1", "1", "1", TestOutcome.FAILED_ASSERTION);
        addTestCaseAndOutcome("test2", "2", "4", TestOutcome.FAILED_WITH_EXCEPTION);
        addTestCaseAndOutcome("test3", "3", "9", TestOutcome.FAILED_FROM_TIMEOUT);
        addTestCaseAndOutcome("test4", "4", "16", TestOutcome.FAILED_BY_SECURITY_MANAGER);
        addTestCaseAndOutcome("test5", "5", "25", TestOutcome.PASSED);
        addTestCaseAndOutcome("test6", "6", "36", TestOutcome.FAILED_BY_SECURITY_MANAGER);
        super.runAllTests();
    }
}
