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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Aggregate multiple {@link LineCoverage} objects (e.g., produced by
 * multiple test cases) and aggregate them into a single object.
 * 
 * @author David Hovemeyer
 */
public class LineCoverageAggregator {
	private SortedMap<Integer, Integer> map;

	/**
	 * Constructor.
	 */
	public LineCoverageAggregator() {
		map = new TreeMap<Integer, Integer>();
	}
	
	/**
	 * Process a single {@link LineCoverage} object.
	 * 
	 * @param lineCoverage a {@link LineCoverage} object to process
	 */
	public void process(LineCoverage lineCoverage) {
		for (LineCoverageRecord record : lineCoverage.getRecordList()) {
			Integer count = map.get(record.getLineNumber());
			map.put(record.getLineNumber(), count == null ? record.getTimesExecuted() : count + record.getTimesExecuted());
		}
	}
	
	/**
	 * @return the aggregate {@link LineCoverage}
	 */
	public LineCoverage getAggregate() {
		LineCoverage aggregate = new LineCoverage();
		aggregate.setTestCaseNumber(-1);
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			aggregate.addRecord(new LineCoverageRecord(entry.getKey(), entry.getValue()));
		}
		return aggregate;
	}
}
