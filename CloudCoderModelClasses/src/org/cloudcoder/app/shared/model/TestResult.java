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

package org.cloudcoder.app.shared.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A TestResult represents the outcome of a particular
 * {@link TestCase} on a particular {@link Submission}.
 */
public class TestResult implements Serializable
{
    public static final long serialVersionUID=1L;
    
    private int id;
    private int submissionReceiptEventId;
    private TestOutcome outcome;
    private String message;
    private String stdout;
    private String stderr;
    
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema SCHEMA = new ModelObjectSchema(Arrays.asList(
    		new ModelObjectField("id", Integer.class, 0, ModelObjectIndexType.IDENTITY),
    		new ModelObjectField("submission_receipt_event_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE),
    		new ModelObjectField("test_outcome", Integer.class, 0),
    		new ModelObjectField("message", String.class, 100),
    		new ModelObjectField("stdout", String.class, Integer.MAX_VALUE),
    		new ModelObjectField("stderr", String.class, Integer.MAX_VALUE)
	));
    
    public TestResult() {
    	
    }
    
    public TestResult(TestOutcome outcome, String message) {
    	this.id = -1;
    	this.submissionReceiptEventId = -1;
        this.outcome=outcome;
        this.message=message;
    }
    
    public TestResult(TestOutcome outcome, 
            String message, 
            String stdout, 
            String stderr)
    {
        this(outcome,message);
        this.stdout=stdout;
        this.stderr=stderr;
    }
    
    /**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @param submissionReceiptId the submissionReceiptId to set
	 */
	public void setSubmissionReceiptEventId(int submissionReceiptId) {
		this.submissionReceiptEventId = submissionReceiptId;
	}
	
	/**
	 * @return the submissionReceiptId
	 */
	public int getSubmissionReceiptEventId() {
		return submissionReceiptEventId;
	}

    public String toString() {
        return message;
    }

    public static TestResult[] error(String msg) {
        return new TestResult[] {new TestResult(TestOutcome.INTERNAL_ERROR, msg)};
    }
    /**
     * @return the success
     */
    public TestOutcome getOutcome() {
        return outcome;
    }
    /**
     * @param success the success to set
     */
    public void setOutcome(TestOutcome outcome) {
        this.outcome= outcome;
    }
    
    /**
     * @param outcomeIndex
     */
    public void setOutcome(int outcomeIndex) {
        this.outcome=TestOutcome.values()[outcomeIndex];
    }
    
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return the stdout
     */
    public String getStdout() {
        return stdout;
    }
    /**
     * @param stdout the stdout to set
     */
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
    /**
     * @return the stderr
     */
    public String getStderr() {
        return stderr;
    }
    /**
     * @param stderr the stderr to set
     */
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }
}
