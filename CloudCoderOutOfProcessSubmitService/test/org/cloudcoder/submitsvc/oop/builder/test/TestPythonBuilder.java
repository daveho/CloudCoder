package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.submitsvc.oop.builder.PythonTester;
import org.cloudcoder.submitsvc.oop.builder.SingleThreadPythonTester;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPythonBuilder extends GenericTest
{
    
    @BeforeClass
    public static void createJavaTester() {
        
    }
    
    @Before
    public void before() {
        super.before();
        // create a problem
        problem=createGenericProblem();
        problem.setProblemType(ProblemType.PYTHON_FUNCTION);

        // specific to the sq method
        problem.setTestName("sq");
        addTestCase("5", "25", TestOutcome.FAILED_ASSERTION);
        addTestCase("9", "81", TestOutcome.FAILED_WITH_EXCEPTION);
        addTestCase("-1", "1", TestOutcome.PASSED);
        addTestCase("10", "100", TestOutcome.FAILED_FROM_TIMEOUT);
        
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
    public void testSq() {
        // program text will fail two of our test cases
        runAllTests();
    }
    
    @Test
    public void singleThread0() {
        tester=new SingleThreadPythonTester();
        runOneTest(0);
    }
    
    @Test
    public void singleThread1() {
        tester=new SingleThreadPythonTester();
        runOneTest(1);
    }
    
    @Test
    public void singleThread2() {
        tester=new SingleThreadPythonTester();
        runOneTest(2);
    }
}
