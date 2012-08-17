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

/**
 * Class to represent the data of a test case, independent of any
 * institution or course.  An instance of TestCaseData represents one
 * of the tests associated with a problem described by a {@link ProblemData}
 * object. 
 * 
 * @author David Hovemeyer
 */
public class TestCaseData implements Serializable, ITestCaseData, IModelObject<TestCaseData> {
	private static final long serialVersionUID = 1L;

	//
	// IMPORTANT: if you add any fields, make sure that you
	// update the copyFrom() method accordingly.
	//
	private String testCaseName;
	private String input;
	private String output;
	private boolean secret;
	
	/** Description of fields. */
	public static final ModelObjectSchema<TestCaseData> SCHEMA = new ModelObjectSchema<TestCaseData>()
			.addAll(ITestCaseData.SCHEMA.getFieldList());
	
	/** Number of fields. */
	public static final int NUM_FIELDS = SCHEMA.getNumFields();

	public TestCaseData() {
		super();
	}
	
	@Override
	public ModelObjectSchema<? extends TestCaseData> getSchema() {
		return SCHEMA;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCaseData#setTestCaseName(java.lang.String)
	 */
	@Override
	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCaseData#getTestCaseName()
	 */
	@Override
	public String getTestCaseName() {
		return testCaseName;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCaseData#setInput(java.lang.String)
	 */
	@Override
	public void setInput(String input) {
		this.input = input;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCaseData#getInput()
	 */
	@Override
	public String getInput() {
		return input;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCaseData#setOutput(java.lang.String)
	 */
	@Override
	public void setOutput(String output) {
		this.output = output;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCaseData#getOutput()
	 */
	@Override
	public String getOutput() {
		return output;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCaseData#setSecret(boolean)
	 */
	@Override
	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCaseData#isSecret()
	 */
	@Override
	public boolean isSecret() {
		return secret;
	}
	
	/**
	 * Copy all data in the given TestCaseData object into this one.
	 * 
	 * @param other another TestCaseData object
	 */
	public void copyFrom(TestCaseData other) {
		this.testCaseName = other.testCaseName;
		this.input = other.input;
		this.output = other.output;
		this.secret = other.secret;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof TestCaseData)) {
			return false;
		}
		TestCaseData other = (TestCaseData) obj;
		return ModelObjectUtil.equals(this.testCaseName, other.testCaseName)
				&& ModelObjectUtil.equals(this.input, other.input)
				&& ModelObjectUtil.equals(this.output, other.output)
				&& this.secret == other.secret;
	}
}