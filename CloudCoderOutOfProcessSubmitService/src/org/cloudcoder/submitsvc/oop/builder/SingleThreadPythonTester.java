package org.cloudcoder.submitsvc.oop.builder;

import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class SingleThreadPythonTester extends PythonTester
{

    /* (non-Javadoc)
     * @see org.cloudcoder.submitsvc.oop.builder.PythonTester#testSubmission(org.cloudcoder.app.shared.model.Problem, java.util.List, java.lang.String)
     */
    @Override
    public List<TestResult> testSubmission(
            Problem problem,
            List<TestCase> testCaseList, 
            String programText)
    {
        final PythonInterpreter terp=new PythonInterpreter();
        final PyObject True=terp.eval("True");
        
        String s=createTestClassSource(problem, testCaseList, programText);
        PyCode code=terp.compile(s);
        terp.eval(code);
        
        List<TestResult> results=new LinkedList<TestResult>();
        
        for (TestCase t : testCaseList) {
            PyObject r=terp.eval("Tester()."+t.getTestCaseName()+"()");
            if (r!=null && r.equals(True)) {
                results.add(new TestResult(TestOutcome.PASSED, "Passed! input=" + t.getInput() + ", output=" + t.getOutput()));
            } else {
                results.add(new TestResult(TestOutcome.FAILED_ASSERTION, "Failed for input=" + t.getInput() + ", expected=" + t.getOutput()));
            }
        }
        return results;
    }
    
}
