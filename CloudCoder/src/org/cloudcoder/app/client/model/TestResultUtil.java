// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.app.client.model;

import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Utility methods for extracting information from {@link TestResult}s.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class TestResultUtil {
    private static final RegExp pass=RegExp.compile("Test passed for input \\(([^)]*)\\), expected output=(.*)");
    private static final RegExp fail=RegExp.compile("Test failed for input \\(([^)]*)\\), expected output=(.*), actual output=(.*)");
    private static final RegExp exception=RegExp.compile("Failed for input \\(([^)]*)\\), expected output=(.*), exception (.*)");

    private static String group(RegExp pattern, int num, String text) {
        MatchResult m=pattern.exec(text);
        boolean found=m!=null;
        if (found) {
            return m.getGroup(num);
        }
        return "";
    }
    
    /**
     * Get the input.  If input is null, will try to parse the input from the
     * message.
     * 
     * @param testResult the TestResult
     * @return The input, or an empty string if the input is missing or cannot
     *      be determined.
     */
    public static String getInput(TestResult testResult) {
    	String input = testResult.getInput();
    	
        if (input!=null) {
            return input;
        }
        
    	TestOutcome outcome = testResult.getOutcome();
    	String message = testResult.getMessage();
    	
        if (outcome==TestOutcome.PASSED) {
            return group(pass, 1, message);
        }
        if (outcome==TestOutcome.FAILED_ASSERTION) {
            return group(fail, 1, message);
        }
        if (outcome==TestOutcome.FAILED_WITH_EXCEPTION) {
            //XXX Exception doesn't necessarily work
            return group(exception, 1, message);
        }
        // Nothing else to cover here
        return "";
    }
    
    /**
     * Get the actual output from this test case.  If it's null, try to parse
     * it out of the message.
     * 
     * @param testResult the TestResult
     * @return The actual output for this test case, or an empty string if the actual
     *      output is missing or cannot be determined.
     */
    public static String getActualOutput(TestResult testResult) {
    	String actualOutput = testResult.getActualOutput();
    	
        if (actualOutput!=null) {
            return actualOutput;
        }
        GWT.log("Actual output is null? message=" + testResult.getMessage());
        
        TestOutcome outcome = testResult.getOutcome();
        String message = testResult.getMessage();
        
        if (outcome==TestOutcome.PASSED) {
            return group(fail, 1, message);
        }
        if (outcome==TestOutcome.FAILED_ASSERTION) {
            return group(fail, 3, message);
        }
        return "";
    }
    
    /**
     * Get the expected output for this test case.  If it's null, try to parse
     * it out of the message.
     * 
     * @param testResult the TestResult
     * @return The expected output for this test case, or an empty string if the
     *      expected output is missing or cannot be determined.
     */
    public static String getExpectedOutput(TestResult testResult) {
    	String expectedOutput = testResult.getExpectedOutput();
    	
        if (expectedOutput!=null){
            return expectedOutput;
        }
        
        TestOutcome outcome = testResult.getOutcome();
        String message = testResult.getMessage();
        
        if (outcome==TestOutcome.PASSED) {
            return group(pass, 2, message);
        }
        if (outcome==TestOutcome.FAILED_ASSERTION) {
            return group(fail, 2, message);
        }
        return "";
    }

}
