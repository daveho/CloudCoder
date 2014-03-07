// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2014, York College of Pennsylvania
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
 * A TestCase for a {@link Problem}.
 * Specifies input(s) and expected output.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class TestCase extends TestCaseData implements Serializable, ITestCase, IModelObject<TestCase> {
	private static final long serialVersionUID = 1L;

	private int testCaseId;
	private int problemId;
	
	/** {@link ModelObjectField} for test case id. */
	public static final ModelObjectField<TestCase, Integer> TEST_CASE_ID =
			new ModelObjectField<TestCase, Integer>("test_case_id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
		public void set(TestCase obj, Integer value) { obj.setTestCaseId(value); }
		public Integer get(TestCase obj) { return obj.getTestCaseId(); }
	};
	/** {@link ModelObjectField} for problem id. */
	public static final ModelObjectField<TestCase, Integer> PROBLEM_ID =
			new ModelObjectField<TestCase, Integer>("problem_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
		public void set(TestCase obj, Integer value) { obj.setProblemId(value); }
		public Integer get(TestCase obj) { return obj.getProblemId(); }
	};
	
	/**
	 * Description of fields (schema version 0).
	 */
	public static final ModelObjectSchema<TestCase> SCHEMA_V0 = new ModelObjectSchema<TestCase>("test_case")
		.add(TEST_CASE_ID)
		.add(PROBLEM_ID)
		.addAll(ITestCaseData.SCHEMA_V0.getFieldList());
	
	/**
	 * Description of fields (schema version 1).
	 */
	public static final ModelObjectSchema<TestCase> SCHEMA_V1 = ModelObjectSchema.basedOn(SCHEMA_V0)
		.addDeltasFrom(ITestCaseData.SCHEMA_V1)
		.finishDelta();
	
	/**
	 * Description of fields (current schema version).
	 */
	public static final ModelObjectSchema<TestCase> SCHEMA = SCHEMA_V1;
	
	
	public TestCase() {
		
	}
	
	@Override
	public ModelObjectSchema<TestCase> getSchema() {
		return SCHEMA;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCase#setTestCaseId(int)
	 */
	@Override
	public void setTestCaseId(int id) {
		this.testCaseId = id;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCase#getTestCaseId()
	 */
	@Override
	public int getTestCaseId() {
		return testCaseId;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCase#setProblemId(int)
	 */
	@Override
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
	/* (non-Javadoc)
	 * @see org.cloudcoder.app.shared.model.ITestCase#getProblemId()
	 */
	@Override
	public int getProblemId() {
		return problemId;
	}

	/**
	 * Factory method for creating an empty TestCase.
	 * (I.e., where all of the fields are initialized to empty values,
	 * as opposed to null values.)
	 * 
	 * @return an empty TestCase
	 */
	public static TestCase createEmpty() {
		TestCase empty = new TestCase();
		empty.setTestCaseId(-1);
		empty.setProblemId(-1);
		empty.setTestCaseName("");
		empty.setInput("");
		empty.setOutput("");
		empty.setSecret(false);
		return empty;
	}
	
	/**
	 * Copy all data in the given TestCase object into this one.
	 * 
	 * @param other another TestCase object
	 */
	public void copyFrom(TestCase other) {
		super.copyFrom(other);
		this.testCaseId = other.testCaseId;
		this.problemId = other.problemId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof TestCase)) {
			return false;
		}
		TestCase other = (TestCase) obj;
		return super.equals(other)
				&& this.testCaseId == other.testCaseId
				&& this.problemId == other.problemId;
	}
}
