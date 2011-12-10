package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.submitsvc.oop.builder.CTester;
import org.junit.Before;
import org.junit.Test;

public class TestCTester extends GenericTest
{
    @Before
    public void before() {
        super.before();
        // create a problem
        problem=createGenericProblem();
        problem.setProblemType(ProblemType.C_FUNCTION);
        problem.setTestName("sq");
        
        tester=new CTester();
        programText="int sq(int x) { \n" +
                " int * crash=NULL; \n" +
                " if (x==5) return 17; \n" +
                " if (x==9) *crash=1; \n" +
                " if (x==10) while (1); \n" +
                " return x*x; \n" +
                    "}";
    }
    
    @Test
    public void test1() {
        addTestCase("test1", "5", "25", TestOutcome.FAILED_ASSERTION);
        runOneTest("test1");
    }
    
    @Test
    public void test2() {
        addTestCase("test2", "9", "81", TestOutcome.FAILED_WITH_EXCEPTION);
        runOneTest("test2");
    }
    
    @Test
    public void test3() {
        addTestCase("test3", "-1", "1", TestOutcome.PASSED);
        runOneTest("test3");
    }
    
    @Test
    public void test4() {
        addTestCase("test4", "10", "100", TestOutcome.FAILED_FROM_TIMEOUT);
        runOneTest("test4");
    }
    
    @Test
    public void runAllTests() {
        addTestCase("test1", "5", "25", TestOutcome.FAILED_ASSERTION);
        addTestCase("test2", "9", "81", TestOutcome.FAILED_WITH_EXCEPTION);
        addTestCase("test3", "-1", "1", TestOutcome.PASSED);
        addTestCase("test4", "10", "100", TestOutcome.FAILED_FROM_TIMEOUT);
        super.runAllTests();
    }
}
