
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
public class TestJavaBuilder extends GenericTest
{
    @Before
    public void before() {
        super.before();
        // create a problem
        problem=createGenericProblem();
        problem.setProblemType(ProblemType.JAVA_METHOD);
        problem.setTestName("sq");
        addTestCase("5", "25", TestOutcome.FAILED_ASSERTION);
        addTestCase("9", "81", TestOutcome.FAILED_WITH_EXCEPTION);
        addTestCase("-1", "1", TestOutcome.PASSED);
        addTestCase("10", "100", TestOutcome.FAILED_FROM_TIMEOUT);
        
        tester=new JavaTester();
        programText="public int sq(int x) { \n" +
                " if (x==5) return 17; \n" +
                " if (x==9) throw new NullPointerException(); \n" +
                " if (x==10) while (true); \n" +
                " return x*x; \n" +
                    "}";
    }
    
    @Test
    public void testSq() {
        // program text will fail two of our test cases
        runAllTests();
    }
}
