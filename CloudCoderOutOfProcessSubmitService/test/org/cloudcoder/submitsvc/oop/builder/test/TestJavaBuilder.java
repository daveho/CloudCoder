/**
 * 
 */
package org.cloudcoder.submitsvc.oop.builder.test;

import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.submitsvc.oop.builder.ITester;
import org.cloudcoder.submitsvc.oop.builder.JavaTester;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author jaimespacco
 *
 */
public class TestJavaBuilder extends GenericTest
{
    @BeforeClass
    public static void createJavaTester() {
        tester=new JavaTester();
    }
    
    @Before
    public void before() {
        super.before();
        // create a problem
        problem=createGenericProblem();
        problem.setProblemType(ProblemType.JAVA_METHOD);
    }
    
    @Test
    public void testSq() {
        problem.setTestName("sq");
        addTestCase("5", "25", TestOutcome.FAILED_ASSERTION);
        addTestCase("9", "81", TestOutcome.FAILED_WITH_EXCEPTION);
        addTestCase("-1", "1", TestOutcome.PASSED);
        addTestCase("10", "100", TestOutcome.PASSED);

        // program text will fail two of our test cases
        String programText="public int sq(int x) { \n" +
                " if (x==5) return 17; \n" +
                " if (x==9) throw new NullPointerException(); \n" +
                " return x*x; \n" +
        		    "}";
        runTests(programText);
    }
}
