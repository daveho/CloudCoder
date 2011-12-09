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

package org.cloudcoder.submitsvc.oop.builder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.server.submitsvc.oop.WorkerTask;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTester implements ITester
{
    private static final Logger logger=LoggerFactory.getLogger(CTester.class);
    public static final long TIMEOUT_LIMIT=2000;

    private String makeCTestFile(Problem problem,
            List<TestCase> testCaseList, String programText)
    {
        StringBuilder test=new StringBuilder();
        test.append("#include <strings.h>\n");
        test.append(programText);
        test.append("\n");
        test.append("int eq(int a, int b) {\n");
        test.append("return a!=b;\n");
        test.append("}\n");
        test.append("int main(int argc, char ** argv) {\n");
        for (TestCase t : testCaseList) {
            test.append("  if (strncmp(argv[1], \"" +t.getTestCaseName()+"\", "+
                    t.getTestCaseName().length()+")==0) {\n");
            test.append("    return eq( "+problem.getTestName()+
                    "("+t.getInput()+"), "+t.getOutput()+");\n");
            test.append("  }\n");
        }
        test.append("  return 99;\n");
        test.append("}");
        return test.toString();
    }
    
    @Override
    public List<TestResult> testSubmission(Problem problem,
            List<TestCase> testCaseList, String programText)
    {
        String testerCode=makeCTestFile(problem, testCaseList, programText);
        
        File workDir=new File("builder");
        workDir.mkdirs();
        
        String programName="program";
        
        Compiler compiler=new Compiler(testerCode, workDir, programName);
        if (!compiler.compile()) {
            //TODO: get the compiler error into a format that can be sent to the server
            logger.error("Unable to compile");
            return Arrays.asList(new TestResult(TestOutcome.COMPILE_FAILED, 
                    "Unable to compile", 
                    compiler.getStatusMessage(),
                    "stderr"));
        }
        logger.info("Compilation successful");
        
        List<TestResult> results=new LinkedList<TestResult>();
        
        ProcessRunner[] tests=new ProcessRunner[testCaseList.size()];
        
        for (int i=0; i<tests.length; i++) {
            tests[i]=new ProcessRunner();
            tests[i].runAsynchronous(workDir, getTestCommand(workDir.getAbsolutePath()+File.separatorChar+programName, testCaseList.get(i)));
        }
        // wait for the timeout limit
        try {
            Thread.sleep(TIMEOUT_LIMIT);
        } catch (InterruptedException e){
            // Ignore; never happens
        }
        for (ProcessRunner p : tests) {
            if (p.isRunning()) {
                p.killProcess();
                results.add(new TestResult(TestOutcome.FAILED_FROM_TIMEOUT, 
                        "timeout",
                        merge(p.getStdout()),
                        merge(p.getStderr())));
            } else {
                //TODO: try to distinguish error codes?
                //6 means core dump, etc
                if (p.getExitCode()==0) {
                    results.add(new TestResult(TestOutcome.PASSED,
                            p.getStatusMessage(),
                            merge(p.getStdout()),
                            merge(p.getStderr())));
                } else {
                    results.add(new TestResult(TestOutcome.FAILED_ASSERTION,
                            p.getStatusMessage(),
                            merge(p.getStdout()),
                            merge(p.getStderr())));
                }
            }
        }
        return results;
    }
    
    private String merge(List<String> list){
        StringBuilder builder=new StringBuilder();
        for (String s : list) {
            builder.append(s);
            builder.append("\n");
        }
        return builder.toString();
    }
    
    public String[] getTestCommand(String programName, TestCase testCase) {
        return new String[] {programName, testCase.getTestCaseName()};
    }
}
