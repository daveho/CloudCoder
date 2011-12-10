package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.submitsvc.oop.builder.PythonTester;
import org.junit.Before;
import org.junit.Test;

public class TestPythonTester extends GenericTest
{
    
    @Before
    public void before() {
        super.before();
        // create a problem
        problem=createGenericProblem();
        problem.setProblemType(ProblemType.PYTHON_FUNCTION);
        problem.setTestName("sq");
         
        tester=new PythonTester();
        programText="def sq(x):\n" +
                "  if x==5:\n" +
                "    return 17\n" +
                "  if x==9:\n" +
                "    raise Exception('error')\n"+
                "  if x==10:\n"+
                "    while True:\n" +
                "      pass\n" +
                "  return x*x\n";
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
