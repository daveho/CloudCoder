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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.model.HealthDataSingleton;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.persist.SnapshotCallback;
import org.cloudcoder.app.server.submitsvc.DefaultSubmitService;
import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;
import org.cloudcoder.app.server.submitsvc.oop.OutOfProcessSubmitService;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SnapshotSelectionCriteria;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make submissions/snapshots drawn from the database available for retesting.
 * Acts as a submission queue, so any number of builders on any
 * number of machines can be used to do the retesting.
 * 
 * @author David Hovemeyer
 */
public class Retest {
	private static final Logger logger = LoggerFactory.getLogger(Retest.class);
	
	private SnapshotSelectionCriteria criteria;
	private Properties config;
	private Map<Integer, ProblemAndTestCaseList> exerciseMap;
	private IdentityHashMap<IFutureSubmissionResult, RetestSnapshot> snapshotMap;
	private File outputDirectory;
	private List<IRetestSubmissionResultVisitor> visitorList;
	
	public Retest() {
		exerciseMap = new HashMap<Integer, ProblemAndTestCaseList>();
		snapshotMap = new IdentityHashMap<IFutureSubmissionResult, RetestSnapshot>();
		visitorList = new LinkedList<IRetestSubmissionResultVisitor>();
	}
	
	public void setCriteria(SnapshotSelectionCriteria criteria) {
		this.criteria = criteria;
	}
	
	public void setConfig(Properties config) {
		this.config = config;
	}
	
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
	public void addVisitor(IRetestSubmissionResultVisitor visitor) {
		visitorList.add(visitor);
	}
	
	public void execute() throws IOException {
		// Initialize OutOfProcessSubmitService
		OutOfProcessSubmitService svc = new OutOfProcessSubmitService();
		svc.initFromConfigProperties(config);
		svc.start();
		OutOfProcessSubmitService.setInstance(svc);
		
		// Pause a bit to allow builders to connect
		System.out.println("Waiting for builders to connect...");
		boolean buildersConnected = false;
		while (!buildersConnected) {
			HealthDataSingleton h = HealthDataSingleton.getInstance();
			if (h.getHealthData().getNumConnectedBuilderThreads() > 0) {
				buildersConnected = true;
			} else {
				try {
					Thread.sleep(1000L);
					System.out.print(".");
					System.out.flush();
				} catch (InterruptedException e) {
					logger.error("Interrupted waiting for builders to connect", e);
				}
			}
		}
		
		// Retrieve snapshots from database
		final List<RetestSnapshot> snapshotList = new ArrayList<RetestSnapshot>();
		Database.getInstance().retrieveSnapshots(criteria, new SnapshotCallback() {
			@Override
			public void onSnapshotFound(int submitEventId, int fullTextChangeId, int courseId, int problemId, int userId, String programText) {
				// FIXME just for testing
				snapshotList.add(new RetestSnapshot(courseId, problemId, userId, submitEventId, fullTextChangeId, programText));
			}
		});
		System.out.println(snapshotList.size() + " snapshots");
		
		// Add snapshots to submission queue, resolving problem/test cases as necessary.
		// Add IFutureSubmissionResults to a list.
		List<IFutureSubmissionResult> futureList = new ArrayList<IFutureSubmissionResult>();
		for (RetestSnapshot snapshot : snapshotList) {
			ProblemAndTestCaseList exercise = findExercise(snapshot.problemId);
			IFutureSubmissionResult future;
			try {
				future = DefaultSubmitService.getInstance().submitAsync(exercise.getProblem(), exercise.getTestCaseData(), snapshot.programText);
				futureList.add(future);
				
				// Map the future to its snapshot
				// FIXME: this could consume a lot of main memory
				snapshotMap.put(future, snapshot);
			} catch (SubmissionException e) {
				logger.error("Error submitting snapshot for retest", e);
			}
		}
		
		// The snapshot list can be cleared now to release memory
		snapshotList.clear();
		
		// Initialize visitors
		outputDirectory.mkdirs();
		for (IRetestSubmissionResultVisitor visitor : visitorList) {
			visitor.init(outputDirectory);
		}
		
		// Wait for all submission results.
		for (IFutureSubmissionResult future : futureList) {
			try {
				SubmissionResult result = waitForSubmissionResult(future);
				RetestSnapshot snapshot = snapshotMap.get(future);
				onSubmissionResult(result, snapshot);
			} catch (SubmissionException e) {
				logger.error("Error testing snapshot (should not happen)", e);
			} catch (InterruptedException e) {
				logger.error("Interrupted waiting for submission result (should not happen)", e);
			}
		}
		
		System.out.print("All snapshots tested...");
		System.out.flush();
		try {
			OutOfProcessSubmitService.getInstance().shutdown();
		} catch (InterruptedException e) {
			logger.error("Interrupted waiting for OutOfProcessBuildService to shutdown", e);
		}
		System.out.println("exiting");
	}

	private SubmissionResult waitForSubmissionResult(IFutureSubmissionResult future) throws SubmissionException, InterruptedException {
		System.out.print("Waiting for submission result...");
		SubmissionResult submissionResult = null;
		while (submissionResult == null) {
			submissionResult = future.waitFor(IFutureSubmissionResult.STANDARD_POLL_WAIT_MS);
			if (submissionResult == null) {
				System.out.print(".");
				System.out.flush();
			}
		}
		System.out.println("done");
		return submissionResult;
	}

	private ProblemAndTestCaseList findExercise(int problemId) {
		ProblemAndTestCaseList exercise = exerciseMap.get(problemId);
		if (exercise == null) {
			Problem problem = Database.getInstance().getProblem(problemId);
			List<TestCase> testCaseList = Database.getInstance().getTestCasesForProblem(problemId);
			exercise = new ProblemAndTestCaseList();
			exercise.setProblem(problem);
			exercise.setTestCaseList(testCaseList);
			exerciseMap.put(problemId, exercise);
		}
		return exercise;
	}

	private void onSubmissionResult(SubmissionResult result, RetestSnapshot snapshot) {
		//System.out.println("Submission result received: " + result.determineSubmissionStatus());

		// Deliver submission result to visitors
		for (IRetestSubmissionResultVisitor visitor : visitorList) {
			visitor.onSubmissionResult(result, snapshot);
		}
	}

	public static void main(String[] args) throws IOException {
		boolean interactiveConfig = false;
		
		for (String arg : args) {
			if (arg.equals("--interactiveConfig")) {
				// Configure interactively rather than using embedded cloudcoder.properties
				interactiveConfig = true;
			} else {
				throw new IllegalArgumentException("Unknown option: " + arg);
			}
		}
		
		Scanner keyboard = new Scanner(System.in);
		Util.configureLogging();
		Properties config = new Properties();
		if (interactiveConfig) {
			Util.readDatabaseProperties(keyboard, config);
			config.setProperty("cloudcoder.submitsvc.oop.host", Util.ask(keyboard, "Submission queue hostname: "));
			config.setProperty("cloudcoder.submitsvc.oop.port", Util.ask(keyboard, "Submission queue port: "));
			config.setProperty("cloudcoder.submitsvc.oop.ssl.useSSL", Util.ask(keyboard, "Use SSL (true/false): "));
			if (Boolean.valueOf(config.getProperty("cloudcoder.submitsvc.oop.ssl.useSSL"))) {
				config.setProperty("cloudcoder.submitsvc.ssl.keystore", Util.ask(keyboard, "Keystore filename: "));
				config.setProperty("cloudcoder.submitsvc.ssl.keystore.password", Util.ask(keyboard, "Keystore password: "));
			}
		} else {
			Util.loadEmbeddedConfig(config, Retest.class.getClassLoader());
		}
		Util.connectToDatabase(config);
		Retest retest = new Retest();
		SnapshotSelectionCriteria criteria = new SnapshotSelectionCriteria();
		criteria.setCourseId(Integer.parseInt(Util.ask(keyboard, "Course id: ")));
		criteria.setProblemId(Integer.parseInt(Util.ask(keyboard, "Problem id: ")));
		criteria.setUserId(Integer.parseInt(Util.ask(keyboard, "User id: ")));
		retest.setCriteria(criteria);
		retest.setConfig(config);
		
		File outputDirectory = new File(Util.ask(keyboard, "Data output directory: "));
		retest.setOutputDirectory(outputDirectory);
		
		// For now the visitors are hard-coded
		retest.addVisitor(new LineCoverageRetestSubmissionResultVisitor());
		
		retest.execute();
	}
}
