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
        programText="#include <stdlib.h>\n"+
                "#include <sys/types.h>\n"+
                "#include <sys/socket.h>\n"+
                "int sq(int x) { \n" +
                " int * crash=NULL; \n" +
                " if (x==1) return 17; \n" +
                " if (x==2) *crash=1; \n" +
                " if (x==3) while (1); \n" +
                " if (x==4) system(\"/bin/ls\");\n" + //currently cannot block illegal operations
                " if (x==5) return x*x; \n" + // correct
                " if (x==6) x = socket(AF_INET, SOCK_STREAM, 0);\n" +//currently cannot block illegal operations
                " return x*x; \n" +
                    "}";
        //System.err.println(programText);
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
        //XXX Currently lack a way to make C fail with security problems
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
        //XXX Currently lack a way to make C fail 
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
