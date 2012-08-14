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
 * A TestCase for a {@link Problem}.
 * Specifies input(s) and expected output.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class TestCase extends TestCaseData implements Serializable {
	private static final long serialVersionUID = 1L;

	private int testCaseId;
	private int problemId;
	
	/**
	 * Description of fields.
	 */
	public static final ModelObjectSchema<TestCase> SCHEMA = new ModelObjectSchema<TestCase>()
		.add(new ModelObjectField<TestCase, Integer>("test_case_id", Integer.class, 0, ModelObjectIndexType.IDENTITY) {
			public void set(TestCase obj, Integer value) { obj.setTestCaseId(value); }
			public Integer get(TestCase obj) { return obj.getTestCaseId(); }
		})
		.add(new ModelObjectField<TestCase, Integer>("problem_id", Integer.class, 0, ModelObjectIndexType.NON_UNIQUE) {
			public void set(TestCase obj, Integer value) { obj.setProblemId(value); }
			public Integer get(TestCase obj) { return obj.getProblemId(); }
		})
		.addAll(TestCaseData.SCHEMA.getFieldList());
	
	
	public TestCase() {
		
	}
	
	public void setTestCaseId(int id) {
		this.testCaseId = id;
	}
	
	public int getTestCaseId() {
		return testCaseId;
	}
	
	public void setProblemId(int problemId) {
		this.problemId = problemId;
	}
	
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
