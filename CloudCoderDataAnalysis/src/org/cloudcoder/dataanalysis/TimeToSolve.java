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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.model.WorkSession;

/**
 * Analyze work sessions to estimate time to solve for problems
 * matching a {@link SnapshotSelectionCriteria}.
 * 
 * @author David Hovemeyer
 */
public class TimeToSolve {
	private static class Progress {
		boolean solved;
		long timeSpent;
	}

	// map of user ids to maps of problem ids to Progress objects
	private Map<Integer, Map<Integer, Progress>> progressMap;
	// map of user ids to maps of problem ids to lists of SubmissionReceipts
	private Map<Integer, Map<Integer, List<SubmissionReceipt>>> submissionReceiptMap;
	// Set of all problems
	private Set<Integer> allProblemsIds;
	private Properties config;
	private int separation;
	private SnapshotSelectionCriteria criteria;
	private String outputFile;
	private int count;
	
	public TimeToSolve() {
		this.progressMap = new HashMap<Integer, Map<Integer, Progress>>();
		this.submissionReceiptMap = new HashMap<Integer, Map<Integer, List<SubmissionReceipt>>>();
		this.allProblemsIds = new HashSet<Integer>();
	}

	public void setCriteria(SnapshotSelectionCriteria criteria) {
		this.criteria = criteria;
	}
	
	public void setSeparation(int separation) {
		this.separation = separation;
	}
	
	private void setConfig(Properties config) {
		this.config = config;
	}
	
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	private void execute() throws IOException {
		Util.connectToDatabase(config);
		
		System.out.print("Getting work sessions...");
		System.out.flush();
		List<WorkSession> sessions = Database.getInstance().findWorkSessions(criteria, separation);
		System.out.println("done");
		
		System.out.print("Analyzing work sessions...");
		System.out.flush();
		for (WorkSession session : sessions) {
			Progress progress = getProgress(session.getUserId(), session.getProblemId());
			if (progress.solved) {
				// problem was solved in a previous work session
				continue;
			}
			long timeToSolve = findTimeToSolve(session);
			if (timeToSolve < 0L) {
				// Problem wasn't solved in this work session.
				// Tally the time spent in case it is solved in a future session.
				progress.timeSpent += session.getTotalTime();
			} else {
				// Problem was solved in this session.
				progress.timeSpent += timeToSolve;
				progress.solved = true;
			}
			count++;
			if (count % 200 == 0) {
				System.out.print(".");
				System.out.flush();
			}
		}
		System.out.println("done");
		
		writeOutput();
		System.out.println("Wrote output to " + outputFile);
	}

	private long findTimeToSolve(WorkSession session) {
		List<SubmissionReceipt> receipts = getSubmissionReceipts(session.getUserId(), session.getProblemId());
		for (SubmissionReceipt r : receipts) {
			if (r.getEventId() >= session.getStartEventId() && r.getEventId() <= session.getEndEventId() &&
					r.getStatus() == SubmissionStatus.TESTS_PASSED) {
				// Solved!
				long tts = r.getEvent().getTimestamp() - session.getStartTime();
				if (tts < 0L) {
					throw new IllegalStateException("Negative time to solve for session?");
				}
				return tts;
			}
		}
		// Problem was not solved in this session
		return -1L;
	}

	private Progress getProgress(int userId, int problemId) {
		allProblemsIds.add(problemId);
		Map<Integer, Progress> studentProgressMap = progressMap.get(userId);
		if (studentProgressMap == null) {
			studentProgressMap = new HashMap<Integer, TimeToSolve.Progress>();
			progressMap.put(userId, studentProgressMap);
		}
		Progress progress = studentProgressMap.get(problemId);
		if (progress == null) {
			progress = new Progress();
			studentProgressMap.put(problemId, progress);
		}
		return progress;
	}

	private List<SubmissionReceipt> getSubmissionReceipts(int userId, int problemId) {
		Map<Integer, List<SubmissionReceipt>> studentSubmissionReceiptMap = submissionReceiptMap.get(userId);
		if (studentSubmissionReceiptMap == null) {
			studentSubmissionReceiptMap = new HashMap<Integer, List<SubmissionReceipt>>();
			submissionReceiptMap.put(userId, studentSubmissionReceiptMap);
		}
		List<SubmissionReceipt> receipts = studentSubmissionReceiptMap.get(problemId);
		if (receipts == null) {
			Problem problem = new Problem();
			problem.setProblemId(problemId);
			User user = new User();
			user.setId(userId);
			receipts = Arrays.asList(Database.getInstance().getAllSubmissionReceiptsForUser(problem, user));
			// sort by event id
			Collections.sort(receipts, new Comparator<SubmissionReceipt>() {
				@Override
				public int compare(SubmissionReceipt o1, SubmissionReceipt o2) {
					return o1.getEventId() - o2.getEventId();
				}
			});
			studentSubmissionReceiptMap.put(problemId, receipts);
		}
		return receipts;
	}
	
	private void writeOutput() throws IOException {
		BufferedWriter w = null;
		try {
			FileWriter fw = new FileWriter(outputFile);
			w = new BufferedWriter(fw);
			
			w.write("userId");
			
			List<Integer> problems = new ArrayList<Integer>();
			problems.addAll(allProblemsIds);
			Collections.sort(problems);
			
			for (Integer p : problems) {
				w.write(",p" + p);
			}
			w.write("\n");
			
			List<Integer> users = new ArrayList<Integer>();
			users.addAll(progressMap.keySet());
			Collections.sort(users);
			for (Integer u : users) {
				Map<Integer, Progress> studentProgressMap = progressMap.get(u);
				w.write(String.valueOf(u));
				for (Integer p : problems) {
					long val = -1L;
					Progress progress = studentProgressMap.get(p);
					if (progress != null && progress.solved) {
						val = progress.timeSpent;
					}
					w.write(",");
					w.write(String.valueOf(val));
				}
				w.write("\n");
			}
			
			w.flush();
		} finally {
			IOUtils.closeQuietly(w);
		}
	}
	
	public static void main(String[] args) throws IOException {
		boolean interactive = false;
		for (String arg : args) {
			if (arg.equals("--interactiveConfig")) {
				interactive = true;
			} else {
				throw new IllegalArgumentException("Unknown option: " + arg);
			}
		}
		
		Util.configureLogging();
		
		TimeToSolve t = new TimeToSolve();
		
		Scanner keyboard = new Scanner(System.in);
		SnapshotSelectionCriteria criteria = Util.getSnapshotSelectionCriteria(keyboard);
		t.setCriteria(criteria);
		int separation = Integer.parseInt(Util.ask(keyboard, "Separation in seconds: "));
		t.setSeparation(separation);
		Properties config = new Properties();
		if (interactive) {
			Util.readDatabaseProperties(keyboard, config);
		} else {
			Util.loadEmbeddedConfig(config, TimeToSolve.class.getClassLoader());
		}
		t.setConfig(config);
		String outputFile = Util.ask(keyboard, "Name of output file: ");
		t.setOutputFile(outputFile);
		t.execute();
	}
}
