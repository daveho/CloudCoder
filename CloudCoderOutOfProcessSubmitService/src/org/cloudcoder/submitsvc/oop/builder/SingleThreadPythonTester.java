// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.cloudcoder.submitsvc.oop.builder;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class SingleThreadPythonTester extends PythonTester
{

    /* (non-Javadoc)
     * @see org.cloudcoder.submitsvc.oop.builder.PythonTester#testSubmission(org.cloudcoder.app.shared.model.Problem, java.util.List, java.lang.String)
     */
    @Override
    public SubmissionResult testSubmission(Submission submission)
    {
        Problem problem=submission.getProblem();
        List<TestCase> testCaseList=submission.getTestCaseList();
        String programText=submission.getProgramText();
        final String s=createTestClassSource(problem, testCaseList, programText);
        final byte[] sBytes=s.getBytes();
        
        //Check if the Python code is syntactically correct
        CompilationResult compres=compilePythonScript(s);
        if (compres.getOutcome()!=CompilationOutcome.SUCCESS) {
            return new SubmissionResult(compres);
        }
        
        List<TestResult> results=new LinkedList<TestResult>();
        final PythonInterpreter terp=new PythonInterpreter();
        final PyObject True=terp.eval("True");
        // won't throw an exception because we checked it at the top of
        // the method
        terp.execfile(new ByteArrayInputStream(sBytes));
        
        for (TestCase t : testCaseList) {
            // pull out the function associated with this particular test case
            PyFunction func=(PyFunction)terp.get(t.getTestCaseName(), PyFunction.class);
            results.add(PythonTester.executeTestCase(t, True, func));
        }
        SubmissionResult result=new SubmissionResult(compres);
        result.setTestResults(results.toArray(new TestResult[results.size()]));
        return result;
    }
    
}
