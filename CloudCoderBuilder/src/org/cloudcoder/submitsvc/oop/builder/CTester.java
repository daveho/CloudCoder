// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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
import java.util.LinkedList;
import java.util.List;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTester implements ITester
{
    private static final Logger logger=LoggerFactory.getLogger(CTester.class);
    public static final long TIMEOUT_LIMIT=2000;

    private int programTextLength;
    private int prologueLength;
    private int epilogueLength;
    
    private String makeCTestFile(Problem problem,
            List<TestCase> testCaseList, String programText)
    {
        prologueLength=3;
        programTextLength=programText.length();

        StringBuilder test=new StringBuilder();
        test.append("#include <string.h>\n");  // 3 lines of prologue
        test.append("#include <stdlib.h>\n");
        test.append("#include <stdio.h>\n");
        test.append(programText);
        test.append("\n");
//        test.append("\t_Bool eq(int a, int b) {\n");
//        test.append("\treturn a == b;\n");
//        test.append("}\n");
        test.append("#undef eq\n");
        test.append("#define eq(a,b) ((a) == (b))\n");
        test.append("int main(int argc, char ** argv) {\n");
        test.append("\tint rcIfEqual = atoi(argv[2]);\n");
        test.append("\tint rcIfNotEqual = atoi(argv[3]);\n");
        for (TestCase t : testCaseList) {
            test.append("  if (strncmp(argv[1], \"" +t.getTestCaseName()+"\", "+
                    t.getTestCaseName().length()+")==0) {\n");
            test.append("    return eq( "+problem.getTestName()+
                    "("+t.getInput()+"), "+t.getOutput()+") ? rcIfEqual : rcIfNotEqual;\n");
            test.append("  }\n");
        }
        test.append("  return 99;\n");
        test.append("}");
        String result=test.toString();
        
        epilogueLength=TesterUtils.countLines(result)-programTextLength-prologueLength;
        
        return result;
    }
    
    private void wait(ProcessRunner[] pool) {
        int numPauses=7;
        for (int i=1; i<=numPauses; i++) {
            if (!pauseAndPoll(TIMEOUT_LIMIT/numPauses, pool)) {
                // we can stop pausing
                return;
            }
        }
    }
    
    private boolean pauseAndPoll(long time, ProcessRunner[] pool) {
        try {
            Thread.sleep(time);
            for (ProcessRunner p : pool) {
                if (p.isRunning()) {
                    return true;
                }
            }
        } catch (InterruptedException e) {
            // should never happen; to be safe, assume a thread may
            // still be running.
            return true;
        }
        // no threads are alive, so we can stop waiting
        return false;
    }
    
    @Override
    public SubmissionResult testSubmission(Submission submission)
    {
        Problem problem=submission.getProblem();
        String programText=submission.getProgramText();
        List<TestCase> testCaseList=submission.getTestCaseList();
        String testerCode=makeCTestFile(problem, testCaseList, programText);
        
        File workDir=new File("builder");
        workDir.mkdirs();
        
        String programName="program";
        
        Compiler compiler=new Compiler(testerCode, workDir, programName);

        if (!compiler.compile()) {
            logger.warn("Compilation failed");
        	return CUtil.createSubmissionResultFromFailedCompile(compiler, prologueLength, epilogueLength);
        }
        
        logger.info("Compilation successful");
        CompilationResult compilationRes = new CompilationResult(CompilationOutcome.SUCCESS);
        compilationRes.setCompilerDiagnosticList(compiler.getCompilerDiagnosticList());
        compilationRes.adjustDiagnosticLineNumbers(prologueLength, epilogueLength);
        
        List<TestResult> results=new LinkedList<TestResult>();
        
        ProcessRunner[] tests=new ProcessRunner[testCaseList.size()];
        
        for (int i=0; i<tests.length; i++) {
            tests[i]=new LimitedProcessRunner();
            //TODO: Use chroot jail
            //Full path to executable is necessary
            tests[i].runAsynchronous(workDir, getTestCommand(workDir.getAbsolutePath()+File.separatorChar+programName, testCaseList.get(i)));
        }
        
        // wait for the timeout limit
        wait(tests);
        
        int index = 0;
        for (ProcessRunner p : tests) {
            TestCase testCase = testCaseList.get(index);
            if (p.isRunning()) {
                p.killProcess();
                results.add(TestResultUtil.createTestResultForTimeout(p, testCase));
            } else {
                //TODO: figure out return code of process killed by ulimit
				if (p.getExitCode()==0) {
                    results.add(TestResultUtil.createTestResultForPassedTest(p, testCase));
                } else if (p.isCoreDump()) {
                    results.add(TestResultUtil.createTestResultForCoreDump(p, testCase));
                } else {
                    results.add(TestResultUtil.createTestResultForFailedAssertion(p, testCase));
                }
            }
            index++;
        }
        SubmissionResult result=new SubmissionResult(
                new CompilationResult(CompilationOutcome.SUCCESS));
        result.setTestResults(results.toArray(new TestResult[results.size()]));
        return result;
    }

	public String[] getTestCommand(String programName, TestCase testCase) {
        return new String[] {programName, testCase.getTestCaseName()};
    }
}
