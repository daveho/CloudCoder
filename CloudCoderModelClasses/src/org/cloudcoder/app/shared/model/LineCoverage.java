// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Line-level code coverage data.  Consists of a sequence
 * of {@link LineCoverageRecord}s.
 * 
 * @author David Hovemeyer
 */
public class LineCoverage {
	private int testCaseNumber;
	private List<LineCoverageRecord> recordList;

	/**
	 * Constructor.
	 */
	public LineCoverage() {
		recordList = new ArrayList<LineCoverageRecord>();
	}

	/**
	 * Set the test case number.
	 * @param testCaseNumber the test case number to set
	 */
	public void setTestCaseNumber(int testCaseNumber) {
		this.testCaseNumber = testCaseNumber;
	}
	
	/**
	 * @return the test case number
	 */
	public int getTestCaseNumber() {
		return testCaseNumber;
	}
	
	/**
	 * Add a {@link LineCoverageRecord}.
	 * 
	 * @param record the {@link LineCoverageRecord} to add
	 */
	public void addRecord(LineCoverageRecord record) {
		recordList.add(record);
	}
	
	/**
	 * @return the list of {@link LineCoverageRecord}s (read-only)
	 */
	public List<LineCoverageRecord> getRecordList() {
		return Collections.unmodifiableList(recordList);
	}
	
	/**
	 * Get the percentage of lines covered.
	 *
	 * @return the percentage of lines covered
	 */
	public double getPercent() {
		if (recordList.size() == 0) {
			return 0.0;
		}
		int coveredLines = 0;
		for (LineCoverageRecord record : recordList) {
			if (record.getTimesExecuted() > 0) {
				coveredLines++;
			}
		}
		return (((double)coveredLines) / recordList.size()) * 100.0;
	}
}
