package org.cloudcoder.submitsvc.oop.builder.test;

import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.submitsvc.oop.builder.CTester;
import org.cloudcoder.submitsvc.oop.builder.JavaTester;
import org.junit.Test;

public class TestJavaCompileFailure extends GenericTest
{
    @Test
    public void testCompileFailed() throws Exception {
        createProblem("compileTest", ProblemType.JAVA_METHOD);
        
        tester=new JavaTester();
        
        setProgramText("public int sq(int x) {\n"+
        "  reutrn x*x;\n"+
        "}");
        
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
