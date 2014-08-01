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

package org.cloudcoder.dataanalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.SnapshotCallback;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;

/**
 * Create a CSV matrix of which students attempted/completed
 * selected problems.
 * 
 * @author David Hovemeyer
 */
public class Attempts implements IAnalyzeSnapshots {
	private SnapshotSelectionCriteria criteria;
	private Set<Integer> problems;
	// Map of user ids to maps of problem ids to "best" submission status
	private Map<Integer, Map<Integer, SubmissionStatus>> resultMap;
	private String outputFile;
	private BufferedWriter writer;
	
	public Attempts() {
		this.problems = new HashSet<Integer>();
		this.resultMap = new HashMap<Integer, Map<Integer,SubmissionStatus>>();
	}
	
	@Override
	public void setCriteria(SnapshotSelectionCriteria criteria) {
		this.criteria = criteria;
	}
	
	@Override
	public void setConfig(Properties config) {
		// do nothing
	}
	
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	public void execute() throws IOException {
		try {
			analyzeSnapshots();
			writeOutput();
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public void analyzeSnapshots() throws IOException {
		FileWriter fw = new FileWriter(outputFile);
		this.writer = new BufferedWriter(fw);
		
		SnapshotCallback callback = new SnapshotCallback() {
			private int count = 0;
			
			@Override
			public void onSnapshotFound(int submitEventId,
					int fullTextChangeId, int courseId, int problemId,
					int userId, String programText, SubmissionReceipt receipt) {
				if (!problems.contains(problemId)) {
					problems.add(problemId);
				}
				Map<Integer, SubmissionStatus> statusMap = resultMap.get(userId);
				if (statusMap == null) {
					statusMap = new HashMap<Integer, SubmissionStatus>();
					resultMap.put(userId, statusMap);
				}
				if (!statusMap.containsKey(problemId)) {
					// This is the first submission receipt we've seen for this user/problem
					statusMap.put(problemId, receipt.getStatus());
				} else {
					// See if current submission receipt status is better than current best
					SubmissionStatus best = statusMap.get(problemId);
					if (goodness(receipt.getStatus()) > goodness(best)) {
						// Current submission receipt status is better
						statusMap.put(problemId, receipt.getStatus());
					}
				}
				
				// Progress indicator
				count++;
				if (count % 200 == 0) {
					System.out.print(".");
					System.out.flush();
				}
			}
		};
		Database.getInstance().retrieveSnapshots(criteria, callback);
	}
	
	private static int goodness(SubmissionStatus status) {
		switch (status) {
		case BUILD_ERROR:
			return 2; // student attempted to submit, but there was an internal error
		case COMPILE_ERROR:
			return 3; // didn't compile
		case NOT_STARTED:
			return 0; // worst
		case STARTED:
			return 1; // student at least opened the problem
		case TESTS_FAILED:
			return 4; // code compiled!
		case TESTS_PASSED:
			return 5; // completed!
		default:
			throw new IllegalStateException("Unexpected SubmissionStatus: " + status);
		}
	}
	
	private void writeOutput() throws IOException {
		TreeSet<Integer> problemIdsSorted = new TreeSet<Integer>(problems);
		TreeSet<Integer> userIdsSorted = new TreeSet<Integer>(resultMap.keySet());
		
		writer.write("userId");
		for (Integer problemId : problemIdsSorted) {
			writer.write(",p" + problemId);
		}
		writer.write("\n");
		
		for (Integer userId : userIdsSorted) {
			writer.write(String.valueOf(userId));
			Map<Integer, SubmissionStatus> statusMap = resultMap.get(userId);
			for (Integer problemId : problemIdsSorted) {
				SubmissionStatus best = statusMap.get(problemId);
				writer.write(",");
				writer.write(best == null ? "-1" : String.valueOf(best.ordinal()));
			}
			writer.write("\n");
		}
		writer.flush();
	}
	
	public static void main(String[] args) throws IOException {
		Attempts attempts = new Attempts();
		Scanner keyboard = new Scanner(System.in);

		Util.configureCriteriaAndDatabase(keyboard, attempts, args);
		
		String outputFile = Util.ask(keyboard, "Output CSV filename: ");
		attempts.setOutputFile(outputFile);
		
		System.out.print("Analyzing snapshots...");
		System.out.flush();
		attempts.execute();
		System.out.println("done");
	}
}
