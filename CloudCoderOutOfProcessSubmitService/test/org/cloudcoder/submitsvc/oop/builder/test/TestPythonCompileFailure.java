// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.submitsvc.oop.builder.PythonTester;
import org.junit.Test;

/**
 * @author jaimespacco
 *
 */
public class TestPythonCompileFailure extends GenericTest
{
    @Test
    public void testCompileFailed() throws Exception {
        problem=createGenericProblem();
        problem.setProblemType(ProblemType.PYTHON_FUNCTION);
        problem.setTestName("compileWillFail");
        
        tester=new PythonTester();
        
        programText="def sq(x):\n"+
                "return x*x";
        TestCase t=createTestCase("test1", "1", "1");
        TestResult res=testOneSubmission(problem, t, programText);
        System.out.println("OUT: "+res.getStdout());
        System.out.println("ERR: "+res.getStderr());
    }
}
