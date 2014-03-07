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

/**
 * Interface to be implemented by a model class that contains the
 * data for a single test case.  This interface is implemented
 * by various concrete classes: for example, {@link TestCase}, which
 * represents a test case associated with a {@link Problem} in the
 * webapp, and {@link RepoTestCase}, which represents a test case
 * associated with a {@link RepoProblem} in the repository webapp.
 * The main purpose of the interface is to make it possible to
 * write code to import and export "generic" test case data in a
 * way that works with multiple representations (webapp and repository).
 * 
 * @author David Hovemeyer
 */
public interface ITestCaseData {

	/**
	 * Set the test case name.
	 * @param testCaseName the test case name to set
	 */
	public abstract void setTestCaseName(String testCaseName);

	/**
	 * @return the test case name
	 */
	public abstract String getTestCaseName();

	/**
	 * Set the input to the test case.
	 * @param input the input to the test case
	 */
	public abstract void setInput(String input);

	/**
	 * Get the input to the test case.
	 * @return the input to the test case
	 */
	public abstract String getInput();

	/**
	 * Set the expected output of the test case.
	 * @param output the expected output of the test case
	 */
	public abstract void setOutput(String output);

	/**
	 * Get the expected output of the test case.
	 * @return the expected output of the test case
	 */
	public abstract String getOutput();

	/**
	 * Set whether or not the test case is secret.
	 * @param secret true if the test case is secret, false otherwise
	 */
	public abstract void setSecret(boolean secret);

	/**
	 * Determine whether or not the test case is secret.
	 * @return true if test case is secret, false otherwise
	 */
	public abstract boolean isSecret();

	/** {@link ModelObjectField} for test case name. */
	public static final ModelObjectField<ITestCaseData, String> TEST_CASE_NAME =
		new ModelObjectField<ITestCaseData, String>("test_case_name", String.class, 40) {
			public void set(ITestCaseData obj, String value) { obj.setTestCaseName(value); }
			public String get(ITestCaseData obj) { return obj.getTestCaseName(); }
		};
	
	/** {@link ModelObjectField} for input (schema version 0). */
	public static final ModelObjectField<ITestCaseData, String> INPUT_V0 =
		new ModelObjectField<ITestCaseData, String>("input", String.class, 255) {
			public void set(ITestCaseData obj, String value) { obj.setInput(value); }
			public String get(ITestCaseData obj) { return obj.getInput(); }
		};
		
	/** {@link ModelObjectField} for input (schema version 1). */
	public static final ModelObjectField<ITestCaseData, String> INPUT =
		new ModelObjectField<ITestCaseData, String>("input", String.class, 1023) {
			public void set(ITestCaseData obj, String value) { obj.setInput(value); }
			public String get(ITestCaseData obj) { return obj.getInput(); }
		};

	/** {@link ModelObjectField} for output (schema version 0). */
	public static final ModelObjectField<ITestCaseData, String> OUTPUT_V0 =
		new ModelObjectField<ITestCaseData, String>("output", String.class, 255) {
			public void set(ITestCaseData obj, String value) { obj.setOutput(value); }
			public String get(ITestCaseData obj) { return obj.getOutput(); }
		};

	/** {@link ModelObjectField} for output (schema version 0). */
	public static final ModelObjectField<ITestCaseData, String> OUTPUT =
		new ModelObjectField<ITestCaseData, String>("output", String.class, 1023) {
			public void set(ITestCaseData obj, String value) { obj.setOutput(value); }
			public String get(ITestCaseData obj) { return obj.getOutput(); }
		};

	/** {@link ModelObjectField} for secret. */
	public static final ModelObjectField<ITestCaseData, Boolean> SECRET =
		new ModelObjectField<ITestCaseData, Boolean>("secret", Boolean.class, 0) {
			public void set(ITestCaseData obj, Boolean value) { obj.setSecret(value); }
			public Boolean get(ITestCaseData obj) { return obj.isSecret(); }
		};
	
	/**
	 * Description of fields (schema version 0).
	 */
	public static final ModelObjectSchema<ITestCaseData> SCHEMA_V0 = new ModelObjectSchema<ITestCaseData>("test_case_data")
		.add(TEST_CASE_NAME)
		.add(INPUT_V0)
		.add(OUTPUT_V0)
		.add(SECRET);
	
	/**
	 * Description of fields (schema version 1).
	 */
	public static final ModelObjectSchema<ITestCaseData> SCHEMA_V1 = ModelObjectSchema.basedOn(SCHEMA_V0)
		.increaseFieldSize(INPUT)
		.increaseFieldSize(OUTPUT)
		.finishDelta();
	
	/**
	 * Description of fields (latest schema version).
	 */
	public static final ModelObjectSchema<ITestCaseData> SCHEMA = SCHEMA_V1;
}