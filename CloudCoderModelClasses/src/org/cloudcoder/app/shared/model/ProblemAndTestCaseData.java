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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An instance of this class represents an importable/exportable
 * problem and test cases.  It contains an SHA-1 hash of the "parent",
 * which is the ProblemAndTestCaseData from which this one was
 * derived.  This allows the provenance of a problem to be represented.
 * 
 * @author David Hovemeyer
 */
public class ProblemAndTestCaseData implements Serializable {
	private static final long serialVersionUID = 1L;

	private ProblemData problemData;
	private List<TestCase> testCaseList;
	private String parentHash;
	
	public ProblemAndTestCaseData() {
		testCaseList = new ArrayList<>();
	}
	
	public void setProblemData(ProblemData problemData) {
		this.problemData = problemData;
	}
	
	public ProblemData getProblemData() {
		return problemData;
	}
	
	public void addTestCase(TestCase testCase) {
		testCaseList.add(testCase);
	}
	
	public List<TestCase> getTestCaseList() {
		return Collections.unmodifiableList(testCaseList);
	}
	
	public void setParentHash(String parentHash) {
		this.parentHash = parentHash;
	}
	
	public String getParentHash() {
		return parentHash;
	}
}
