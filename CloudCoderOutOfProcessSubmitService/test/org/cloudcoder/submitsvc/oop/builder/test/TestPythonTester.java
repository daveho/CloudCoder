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
                "  if x==1:\n" +
                "    return 17\n" +
                "  if x==2:\n" +
                "    raise Exception('error')\n"+
                "  if x==3:\n"+
                "    while True:\n" +
                "      pass\n" +
                "  if x==4:\n" +
                "    from subprocess import call\n" +
                "    call(['ls', '-l'])\n" +
                "  if x==5:\n" +
                "    return x*x\n"+
                "  if x==6:\n" +
                "    import socket\n" +
                "    sock = socket.socket()\n" +
                "    sock.connect((localhost, 8888))\n" +
                "  return x*x\n";
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
        addTestCase("test6", "6", "36", TestOutcome.FAILED_BY_SECURITY_MANAGER);
        super.runAllTests();
    }
}
