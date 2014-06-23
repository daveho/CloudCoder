// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Object describing the result of compiling and testing a submission.
 * Contains a {@link CompilationResult} and a sequence of {@link TestResult}s.
 * May also contain {@link SubmissionResultAnnotation}s, which are generic
 * key/value pairs describing additional (optional) properties of
 * the submission, such as static analysis results, code coverage results,
 * etc.
 * 
 * @author David Hovemeyer
 */
public class SubmissionResult implements Serializable
{
    public static final long serialVersionUID = 2L; // bumped when support for annotations added 
    
    private TestResult[] testResults;
    private CompilationResult compilationResult;
    private List<SubmissionResultAnnotation> annotationList;
    
    /**
     * Constructor.
     */
    public SubmissionResult() {
    	this.annotationList = new ArrayList<SubmissionResultAnnotation>();
    }
    
    /**
     * Constructor from a {@link CompilationResult}.
     * Useful for a submission which failed to compile.
     * 
     * @param compilationResult the {@link CompilationResult}
     */
    public SubmissionResult(CompilationResult compilationResult) {
    	this();
    	
        this.compilationResult=compilationResult;
        
        // Create empty list of TestResults
        this.testResults = new TestResult[0];
    }
    
    /**
     * @param compileResult the {@link CompilationResult}
     */
    public void setCompilationResult(CompilationResult compileResult) {
        this.compilationResult=compileResult;
    }

    /**
     * @return the {@link CompilationResult}
     */
    public CompilationResult getCompilationResult() {
        return compilationResult;
    }

    /**
     * @param outcomes the array of test results to set
     */
    public void setTestResults(TestResult[] outcomes) {
        this.testResults=outcomes;
    }

    /**
     * @return the array of TestResults
     */
    public TestResult[] getTestResults() {
        return testResults;
    }

    /**
     * @return true if the submission compiled, false otherwise
     */
    public boolean isCompiled() {
        return compilationResult.getOutcome()==CompilationOutcome.SUCCESS;
    }

	/**
	 * @return true if all tests passed, false if at least one
	 *         test did not pass
	 */
	public boolean isAllTestsPassed() {
		return testResults != null && (getNumTestsAttempted() == getNumTestsPassed());
	}

	/**
	 * @return the number of test results which PASSED
	 */
	public int getNumTestsPassed() {
		int numPassed = 0;
		for (TestResult testResult : testResults) {
			if (testResult.getOutcome() == TestOutcome.PASSED) {
				numPassed++;
			}
		}
		return numPassed;
	}

	/**
	 * @return the number of tests attempted
	 */
	public int getNumTestsAttempted() {
		return testResults.length;
	}

	/**
	 * Determine a {@link SubmissionStatus} for this SubmissionResult.
	 * 
	 * @return the SubmissionStatus
	 */
	public SubmissionStatus determineSubmissionStatus() {
		// Determine status
		SubmissionStatus status;
		if (this.getCompilationResult().getOutcome() == CompilationOutcome.SUCCESS) {
			// Check to see whether or not all tests passed
			status = this.isAllTestsPassed() ? SubmissionStatus.TESTS_PASSED : SubmissionStatus.TESTS_FAILED;
		} else if (this.getCompilationResult().getOutcome() == CompilationOutcome.FAILURE) {
			// Compile error(s)
			status = SubmissionStatus.COMPILE_ERROR;
		} else {
			// Something unexpected prevented compilation and/or testing
			status = SubmissionStatus.BUILD_ERROR;
		}
		return status;
	}
	
	/**
	 * Add a {@link SubmissionResultAnnotation}.
	 * 
	 * @param annotation the annotation to add
	 */
	public void addAnnotation(SubmissionResultAnnotation annotation) {
		annotationList.add(annotation);
	}
	
	/**
	 * Get (read-only) list of {@link SubmissionResultAnnotation}s.
	 * 
	 * @return (read-only) list of {@link SubmissionResultAnnotation}s
	 */
	public List<SubmissionResultAnnotation> getAnnotationList() {
		return Collections.unmodifiableList(annotationList);
	}
}
