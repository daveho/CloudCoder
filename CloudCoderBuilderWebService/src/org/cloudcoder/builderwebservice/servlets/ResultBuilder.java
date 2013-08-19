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

import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestResult;
import org.json.simple.JSONArray;

/**
 * Build a JSON response object for a {@link SubmissionResult}.
 * 
 * @author David Hovemeyer
 */
public class ResultBuilder {
	private SubmissionResult submissionResult;
	
	public ResultBuilder(SubmissionResult submissionResult) {
		this.submissionResult = submissionResult;
	}
	
	@SuppressWarnings("unchecked")
	public Map<?, ?> build() {
		Map<String, Object> obj = new HashMap<String, Object>();
		
		CompilationOutcome outcome = submissionResult.getCompilationResult().getOutcome();
		obj.put("CompilationOutcome", outcome.toString());
		
		JSONArray testcaseResponseArray = new JSONArray();
		TestResult[] testResultList = submissionResult.getTestResults();
		if (testResultList == null) {
			testResultList = new TestResult[0];
		}
		
		for (TestResult testResult : testResultList) {
			// TODO: should use a helper class for translation between TestResult and TestcaseResponse
			Map<String, Object> testcaseResponse = new HashMap<String, Object>();
			if (outcome != CompilationOutcome.SUCCESS) {
				testcaseResponse.put("Success", Boolean.FALSE);
				testcaseResponse.put("Error", "Code did not compile");
			} else {
				testcaseResponse.put("Success", Boolean.TRUE);
				testcaseResponse.put("Trace", "");
				testcaseResponse.put("Stdout", testResult.getStdout());
				testcaseResponse.put("ReturnValue", testResult.getActualOutput());
			}
			
			testcaseResponseArray.add(testcaseResponse);
		}
		
		obj.put("Response", testcaseResponseArray);
		
		return obj;
	}
}
