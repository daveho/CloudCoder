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

import java.io.File;

import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO:  Create a test harness that captures the return value of the function
 * and prints it out, for better output testing.
 * 
 * @author jaimespacco
 *
 */
public class CFunctionTestCaseExecutor extends CTestCaseExecutor implements Runnable
{
    private static Logger logger = LoggerFactory.getLogger(CFunctionTestCaseExecutor.class);
    
    private static final int MAX_TEST_EXECUTOR_JOIN_ATTEMPTS = 10;

    private int passedReturnValue;
    private int failedReturnValue;
    
    /**
     * Constructor.
     * 
     * @param tempDir    directory in which the test executable will run
     * @param testCase   the {@link TestCase} to use as test input/expected output
     */
    public CFunctionTestCaseExecutor(File tempDir, TestCase testCase, 
        int passedTestCaseReturnVal, int failedTestCaseReturnValue)
    {
        super(tempDir, testCase);
        this.passedReturnValue=passedTestCaseReturnVal;
        this.failedReturnValue=failedTestCaseReturnValue;
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.submitsvc.oop.builder.CTestCaseExecutor#createTestResult(org.cloudcoder.submitsvc.oop.builder.ProcessRunner)
     */
    @Override
    protected TestResult createTestResult(ProcessRunner processRunner)
    {
        String stdout=processRunner.getStdout();
        String stderr=processRunner.getStderr();
        
        
        String inputs="("+testCase.getInput()+")";
        
        if (processRunner.getExitCode()==passedReturnValue) {
            return new TestResult(TestOutcome.PASSED, 
                    "Success with inputs: "+inputs+" and output: "+testCase.getOutput(),
                    stdout, 
                    stderr);
        } else if (processRunner.getExitCode()==failedReturnValue){
            return new TestResult(TestOutcome.FAILED_ASSERTION, 
                    "Failed with inputs: "+inputs+
                        ",\n expected output: "+testCase.getOutput()+ 
                        ",\n your output did not match",
                    stdout, 
                    stderr);
        } else {
            return new TestResult(TestOutcome.FAILED_WITH_EXCEPTION,
                    "Crashed with inputs: "+inputs+
                        ",\n expected output: "+testCase.getOutput()+ 
                        ",\n your output crashed with exit code: " +processRunner.getExitCode(),
                    stdout, 
                    stderr);
        }
    }
    

}
