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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link ITester} implementation which tests single C functions
 * by passing arguments to them and then testing that they
 * return the expected return value.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class CTester implements ITester
{
    private static final boolean KEEP_TEMP_FILES = Boolean.getBoolean("cTester.keepTempFiles");
    private static final Logger logger=LoggerFactory.getLogger(CTester.class);
    public static final long TIMEOUT_LIMIT=2000;

    // A random number generator to generate exit codes for test processes.
    // This makes it hard (but not impossible) for a tested function
    // to game the system by simply exiting with the exit code that
    // indicates success.
    private static final Random rng = new Random();

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

        // The program text is the user's function
        test.append(programText);
        test.append("\n");

        // The eq macro will test the function's return value against
        // the expected return value.
        test.append("#undef eq\n");
        test.append("#define eq(a,b) ((a) == (b))\n");

        // Generate a main() function which can run all of the test cases.
        // argv[1] specifies the test case to execute by name.
        // argv[2] and argv[3] specify the exit values to use to indicate
        // whether or not the tested function's return value matched the
        // expected value.
        test.append("int main(int argc, char ** argv) {\n");
        test.append("  int rcIfEqual = atoi(argv[2]);\n");
        test.append("  int rcIfNotEqual = atoi(argv[3]);\n");
        // Make it a bit harder to steal the exit codes
        test.append("  argv[2] = 0;\n");
        test.append("  argv[3] = 0;\n");

        // Generate calls to execute test cases.
        for (TestCase t : testCaseList) {
            test.append("  if (strncmp(argv[1], \"" +t.getTestCaseName()+"\", "+
                    t.getTestCaseName().length()+")==0) {\n");
            test.append("    return eq("+problem.getTestname()+
                    "("+t.getInput()+"), ("+t.getOutput()+")) ? rcIfEqual : rcIfNotEqual;\n");
            test.append("  }\n");
        }

        // We return 99 if an invalid test case was provided: shouldn't
        // happen in practice.
        test.append("  return 99;\n");
        test.append("}");
        String result=test.toString();
        System.out.println(result);

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
        File workDir = CUtil.makeTempDir("/tmp");
        //logger.debug("Creating temp dir " + workDir);
        try {
            return doTestSubmission(submission, workDir);
        } finally {
            // Clean up
            logger.debug("Keep temp files? "+KEEP_TEMP_FILES);
            if (!KEEP_TEMP_FILES) {
                new DeleteDirectoryRecursively(workDir).delete();
            }
        }
    }

    private SubmissionResult doTestSubmission(Submission submission,File workDir)
    {
        Problem problem=submission.getProblem();
        String programText=submission.getProgramText();
        List<TestCase> testCaseList=submission.getTestCaseList();
        String testerCode=makeCTestFile(problem, testCaseList, programText);

        String programName=CTestCaseExecutor.PROGRAM_NAME;

        Compiler compiler=new Compiler(testerCode, workDir, programName);

        if (!compiler.compile()) {
            logger.warn("Compilation failed");
            return CUtil.createSubmissionResultFromFailedCompile(compiler, prologueLength, epilogueLength);
        }

        logger.info("Compilation successful");
        CompilationResult compilationRes = new CompilationResult(CompilationOutcome.SUCCESS);
        compilationRes.setCompilerDiagnosticList(compiler.getCompilerDiagnosticList());
        compilationRes.adjustDiagnosticLineNumbers(prologueLength, epilogueLength);

        // TODO:  More clever system
        int passRetVal=77;
        int failRetVal=78;
        List<CTestCaseExecutor> testCaseExecutors = new ArrayList<CTestCaseExecutor>();
        for (TestCase testCase : submission.getTestCaseList()) {
            
            CTestCaseExecutor executor = createTestExecutor(workDir, testCase, passRetVal, failRetVal);
            executor.start();
            testCaseExecutors.add(executor);
        }

        // Wait for all TestCaseExecutors to finish,
        // collect TestResults
        List<TestResult> testResultList = new ArrayList<TestResult>();
        for (CTestCaseExecutor executor : testCaseExecutors) {
            executor.join();
            testResultList.add(executor.getTestResult());
        }

        // OK, we have all of our TestResults.
        // Package them up in a SubmissionResult (along with the CompilationResult)
        // and we're done.
        SubmissionResult submissionResult = new SubmissionResult();
        submissionResult.setCompilationResult(new CompilationResult(CompilationOutcome.SUCCESS));
        submissionResult.setTestResults(testResultList.toArray(new TestResult[testResultList.size()]));

        return submissionResult;

    }

    /**
     * @param workDir
     * @param testCase
     * @return
     */
    private CTestCaseExecutor createTestExecutor(File tempDir, TestCase testCase, int passRetVal, int failRetVal)
    {
        CTestCaseExecutor executor=new CFunctionTestCaseExecutor(tempDir, testCase,passRetVal,failRetVal);
        executor.addArgument(testCase.getTestCaseName());
        executor.addArgument(Integer.toString(passRetVal));
        executor.addArgument(Integer.toString(failRetVal));
        return executor;
        
    }

    /**
     * Get the command to execute a specific TestCase.
     * 
     * @param programName  the filename of the test executable
     * @param testCase     the TestCase to execute
     * @param rcIfEqual    the exit code to indicate that the test passed
     * @param rcIfNotEqual the exit code to indicate that the test failed
     * @return the command
     */
    public String[] getTestCommand(String programName, TestCase testCase, int rcIfEqual, int rcIfNotEqual) {
        return new String[] {programName, testCase.getTestCaseName(), String.valueOf(rcIfEqual), String.valueOf(rcIfNotEqual)};
    }
}
