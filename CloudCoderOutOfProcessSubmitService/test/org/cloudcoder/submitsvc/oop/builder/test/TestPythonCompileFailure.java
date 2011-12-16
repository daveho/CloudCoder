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

import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.submitsvc.oop.builder.PythonTester;
import org.junit.Test;

/**
 * @author jaimespacco
 *
 */
public class TestPythonCompileFailure extends GenericTest
{
    @Test
    public void testCompileFailedSimple() {
        CompilationResult res=PythonTester.compilePythonScript("def sq(x):\n" +
                "  z=x*x\n" +
                "     return z\n");
        System.out.println(res.getOutcome());
        System.out.println(res.getCompilerDiagnosticList());
    }
    

    public void testCompileFailed() throws Exception {
        createProblem("compileTest", ProblemType.PYTHON_FUNCTION);

        tester=new PythonTester();
        
        setProgramText("def sq(x):\n"+
        "reutrn x*x\n");
        
        
        addTestCase("test1", "1", "1");
        SubmissionResult result=tester.testSubmission(submission);
        CompilationResult compres=result.getCompilationResult();
        for (CompilerDiagnostic d : compres.getCompilerDiagnosticList()) {
            System.out.println(d);
        }
        //System.out.println("OUT: "+compres.getStdout());
        //System.out.println("ERR: "+res.getStderr());
    }
}
