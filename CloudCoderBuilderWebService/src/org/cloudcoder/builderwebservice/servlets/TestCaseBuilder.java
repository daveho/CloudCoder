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

package org.cloudcoder.builderwebservice.servlets;

import java.util.Map;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.json.JSONUtil;
import org.cloudcoder.webservice.util.BadRequestException;

/**
 * Build a {@link TestCase} from a JSON testcase object found in the
 * {@link Request} object.
 * 
 * @author David Hovemeyer
 */
public class TestCaseBuilder {
	private Object tc_;
	private ProblemType problemType;
	private int index;
	
	/**
	 * Constructor.
	 * 
	 * @param tc          the JSON testcase object
	 * @param problemType the {@link ProblemType}
	 * @param index       the index of the test case
	 */
	public TestCaseBuilder(Object tc, ProblemType problemType, int index) {
		this.tc_ = tc;
		this.problemType = problemType;
		this.index = index;
	}

	/**
	 * Create a {@link TestCase} from the JSON testcase object.
	 * 
	 * @return the {@link TestCase}
	 * @throws BadRequestException if we couldn't create a valid {@link TestCase}
	 */
	public TestCase build() throws BadRequestException {
		Map<?, ?> tc = JSONUtil.expectObject(tc_);
		String input = JSONUtil.expect(String.class, JSONUtil.requiredField(tc, "Input"));
		
		TestCase testCase = new TestCase();
		
		testCase.setTestCaseName("t" + index);
		
		testCase.setInput(input);
		
		// FIXME: we really need to modify the notion of TestCase to remove the requirement that there is expected output
		if (problemType.isOutputLiteral()) {
			// Function/method problem: get a literal value that is syntactically
			// and semantically valid to compare the dynamic return value against.
			// We don't actually care what the result of the comparison is.
			testCase.setOutput(problemType.getLanguage().getLiteralCompareToAnyValue());
		} else {
			// Program problem: use a trivial regexp that matches any output
			testCase.setOutput("^.*$");
		}
		
		testCase.setSecret(false);
		
		testCase.setProblemId(Constants.FAKE_PROBLEM_ID);
		testCase.setTestCaseId(index);
		
		return testCase;
	}

}
