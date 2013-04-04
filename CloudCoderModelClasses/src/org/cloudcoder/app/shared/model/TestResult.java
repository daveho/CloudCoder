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

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * A TestResult represents the outcome of a particular
 * {@link TestCase} on a particular {@link Submission}.
 */
public class TestResult implements Serializable, IModelObject<TestResult>
{
    public static final long serialVersionUID=1L;
    
    private int id;
    private int submissionReceiptEventId;
    private TestOutcome outcome;
    private String message;
    private String stdout;
    private String stderr;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    
    public static final ModelObjectField<TestResult, Integer> ID=new ModelObjectField<TestResult, Integer>("id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
        public void set(TestResult obj, Integer value) { obj.setId(value); }
        public Integer get(TestResult obj) { return obj.getId(); }
    };
    public static final ModelObjectField<TestResult, Integer> SUBMISSON_RECEIPT_EVENT_ID=new ModelObjectField<TestResult, Integer>("submission_receipt_event_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
        public void set(TestResult obj, Integer value) { obj.setSubmissionReceiptEventId(value); }
        public Integer get(TestResult obj) { return obj.getSubmissionReceiptEventId(); }
    };
    public static final ModelObjectField<TestResult, TestOutcome> TEST_OUTCOME=new ModelObjectField<TestResult, TestOutcome>("test_outcome", TestOutcome.class, 0) {
        public void set(TestResult obj, TestOutcome value) { obj.setOutcome(value); }
        public TestOutcome get(TestResult obj) { return obj.getOutcome(); }
    };
    public static final ModelObjectField<TestResult, String> MESSAGE=new ModelObjectField<TestResult, String>("message", String.class, 100) {
        public void set(TestResult obj, String value) { obj.setMessage(value); }
        public String get(TestResult obj) { return obj.getMessage(); }
    };
    public static final ModelObjectField<TestResult, String> STDOUT=new ModelObjectField<TestResult, String>("stdout", String.class, Integer.MAX_VALUE) {
        public void set(TestResult obj, String value) { obj.setStdout(value); }
        public String get(TestResult obj) { return obj.getStdout(); }
    };
    public static final ModelObjectField<TestResult, String> STDERR=new ModelObjectField<TestResult, String>("stderr", String.class, Integer.MAX_VALUE) {
        public void set(TestResult obj, String value) { obj.setStderr(value); }
        public String get(TestResult obj) { return obj.getStderr(); }
    };
    public static final ModelObjectField<TestResult, String> INPUT=new ModelObjectField<TestResult, String>("input", String.class, 100) {
            public void set(TestResult obj, String value) { obj.setInput(value); }
            public String get(TestResult obj) { return obj.getInput(); }
    };
    public static final ModelObjectField<TestResult, String> EXPECTED_OUTPUT=new ModelObjectField<TestResult, String>("expected", String.class, 100) {
        public void set(TestResult obj, String value) { obj.setExpectedOutput(value); }
        public String get(TestResult obj) { return obj.getExpectedOutput(); }
    };
    public static final ModelObjectField<TestResult, String> ACTUAL_OUTPUT=new ModelObjectField<TestResult, String>("actual", String.class, 100) {
        public void set(TestResult obj, String value) { obj.setActualOutput(value); }
        public String get(TestResult obj) { return obj.getActualOutput(); }
    };
    
	/**
	 * Description of fields (schema version 0).
	 */
	public static final ModelObjectSchema<TestResult> SCHEMA_V0 = new ModelObjectSchema<TestResult>("test_result")
		.add(ID)
		.add(SUBMISSON_RECEIPT_EVENT_ID)
		.add(TEST_OUTCOME)
		.add(MESSAGE)
		.add(STDOUT)
		.add(STDERR);
	
	/**
	 * Description of fields (schema version 1).
	 */
	public static final ModelObjectSchema<TestResult> SCHEMA_V1 = ModelObjectSchema.basedOn(SCHEMA_V0)
	        .addAfter(STDERR, INPUT)
            .addAfter(INPUT, EXPECTED_OUTPUT)
            .addAfter(EXPECTED_OUTPUT, ACTUAL_OUTPUT)
            .finishDelta();
	
	public static final ModelObjectSchema<TestResult> SCHEMA = SCHEMA_V1;
    
    public TestResult() {
    	
    }
    
    @Override
    public ModelObjectSchema<TestResult> getSchema() {
    	return SCHEMA;
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
    
    public TestResult(TestOutcome outcome,
        String input,
        String actualOutput,
        String expectedOutput,
        String stdout,
        String stderr)
    {
        this.outcome=outcome;
        this.input=input;
        this.actualOutput=actualOutput;
        this.expectedOutput=expectedOutput;
        this.stdout=stdout;
        this.stderr=stderr;
    }
    
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
     * @return The input, or an empty string if the input is missing or cannot
     *      be determined.
     */
    public String getInput() {
        if (input!=null) {
            return input;
        }
        if (getOutcome()==TestOutcome.PASSED) {
            return group(pass, 1, getMessage());
        }
        if (getOutcome()==TestOutcome.FAILED_ASSERTION) {
            return group(fail, 1, getMessage());
        }
        if (getOutcome()==TestOutcome.FAILED_WITH_EXCEPTION) {
            //XXX Exception doesn't necessarily work
            return group(exception, 1, getMessage());
        }
        // Nothing else to cover here
        return "";
    }
    /**
     * Get the actual output from this test case.  If it's null, try to parse
     * it out of the message.
     * @return The actual output for this test case, or an empty string if the actual
     *      output is missing or cannot be determined.
     */
    public String getActualOutput() {
        if (actualOutput!=null) {
            return actualOutput;
        }
        if (getOutcome()==TestOutcome.PASSED) {
            return group(fail, 1, getMessage());
        }
        if (getOutcome()==TestOutcome.FAILED_ASSERTION) {
            return group(fail, 3, getMessage());
        }
        return "";
    }
    /**
     * Get the expected output for this test case.  If it's null, try to parse
     * it out of the message.
     * @return The expected output for this test case, or an empty string if the
     *      expected output is missing or cannot be determined.
     */
    public String getExpectedOutput() {
        if (expectedOutput!=null){
            return expectedOutput;
        }
        if (getOutcome()==TestOutcome.PASSED) {
            return group(pass, 2, getMessage());
        }
        if (getOutcome()==TestOutcome.FAILED_ASSERTION) {
            return group(fail, 2, getMessage());
        }
        return "";
    }
    
    /**
     * @param input The input to set
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * @param expectedOutput The expected output to set
     */
    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    /**
     * @param actualOutput The actual output to set
     */
    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
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
