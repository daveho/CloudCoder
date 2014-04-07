// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013-2014, York College of Pennsylvania
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

package org.cloudcoder.builder2.batch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemAndTestCaseList;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.json.JSONConversion;
import org.cloudcoder.app.shared.model.json.ReflectionFactory;
import org.cloudcoder.builder2.server.Builder2;
import org.cloudcoder.daemon.IOUtil;

/**
 * Front-end for batch-mode testing.
 * 
 * @author David Hovemeyer
 */
public class BatchMain {
	private static class Result {
		final String sourceFile;
		final SubmissionResult submissionResult;
		
		public Result(String sourceFile, SubmissionResult submissionResult) {
			this.sourceFile = sourceFile;
			this.submissionResult = submissionResult;
		}
	}
	
	private ProblemAndTestCaseList exercise;
	private List<String> sourceFileList;
	private LinkedBlockingQueue<String> sourceQueue;
	private LinkedBlockingQueue<Result> resultQueue;
	private Builder2 builder2;
	
	private class Worker implements Runnable {
		volatile boolean done = false;
		@Override
		public void run() {
			while (!done && !Thread.interrupted()) {
				try {
					String sourceFile = sourceQueue.take();
					// Read source text
					try {
						FileReader fileReader = new FileReader(sourceFile);
						String programText;
						try {
							programText = IOUtils.toString(fileReader);
						} finally {
							IOUtil.closeQuietly(fileReader);
						}
						
						// Test the submission
						SubmissionResult result =
								builder2.testSubmission(exercise.getProblem(), exercise.getTestCaseData(), programText);
						
						resultQueue.put(new Result(sourceFile, result));
					} catch (IOException e) {
						System.err.println("Could not read " + sourceFile);
						resultQueue.put(new Result(sourceFile, null));
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}
	
	public BatchMain(ProblemAndTestCaseList exercise, List<String> sourceFileList) {
		this.exercise = exercise;
		this.sourceFileList = sourceFileList;
		this.sourceQueue = new LinkedBlockingQueue<String>();
		this.resultQueue = new LinkedBlockingQueue<BatchMain.Result>();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// Shut log4j up
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new NullAppender());
		
		if (args.length != 2) {
			System.err.println("Usage: java -jar cloudcoderBuilder.jar batch <exercise JSON> <source file list>");
			System.exit(1);
		}
		
		String exerciseJSON = args[0];
		String sourceFileListFilename = args[1];

		ProblemAndTestCaseList exercise = new ProblemAndTestCaseList();
		
		// Read the exercise (Problem and TestCases)
		Reader r = null;
		try {
			r = new BufferedReader(new FileReader(exerciseJSON));
			JSONConversion.readProblemAndTestCaseData(
					exercise,
					ReflectionFactory.forClass(Problem.class),
					ReflectionFactory.forClass(TestCase.class),
					r);
		} finally {
			if (r != null) { // daemon-0.6 doesn't handle null Reader
				IOUtil.closeQuietly(r);
			}
		}
		
		// Read the list of source files to test
		List<String> sourceFileList = new ArrayList<String>();
		BufferedReader r2 = null;
		try {
			r2 = new BufferedReader(new FileReader(sourceFileListFilename));
			while (true) {
				String sourceFile = r2.readLine();
				if (sourceFile == null) {
					break;
				}
				sourceFileList.add(sourceFile);
			}
		} finally {
			IOUtil.closeQuietly(r2);
		}
		
		BatchMain batchMain = new BatchMain(exercise, sourceFileList);
		batchMain.execute();
	}
	
	public void execute() throws IOException, InterruptedException {
		Properties config = new Properties();
		
		// Configuration properties
		// TODO: allow these to be specified via a properties file
		config.setProperty("cloudcoder.submitsvc.oop.easysandbox.enable", "true");
		config.setProperty("cloudcoder.submitsvc.oop.easysandbox.heapsize", "8388608");
		config.setProperty("cloudcoder.builder2.tmpdir", "/tmp");
		
		builder2 = new Builder2(config);
		
		Map<String, String> resultMap = new HashMap<String, String>();
		
		TestCase[] testCaseList = exercise.getTestCaseList();
		
		Worker[] workers = new Worker[/*Runtime.getRuntime().availableProcessors() * 2*/1];
		Thread[] threads = new Thread[workers.length];
		
		try {
			// Start workers
			for (int i = 0; i < workers.length; i++) {
				workers[i] = new Worker();
				threads[i] = new Thread(workers[i]);
				threads[i].start();
			}			
			
			// Add files to source queue
			int submissionCount = 0;
			for (String sourceFile : sourceFileList) {
				sourceQueue.put(sourceFile);
				submissionCount++;
			}
			
			// Wait for finished Results to come back
			int finishCount = 0;
			while (finishCount < submissionCount) {
				Result r = resultQueue.take();
				finishCount++;
				
				String sourceFile = r.sourceFile;
				SubmissionResult result = r.submissionResult;
				
				if (result == null) {
					continue;
				}
				
				// FIXME: For now, just a hard-coded output format summarizing compilation status and test results
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				
				pw.print(sourceFile);
				pw.print(":");
				pw.print(result.getCompilationResult().getOutcome());
				TestResult[] testResults = result.getTestResults();
				boolean allPassed = true;
				for (int i = 0; i < testCaseList.length; i++) {
					pw.print(",");
					pw.print(testCaseList[i].getTestCaseName());
					pw.print("=");
					if (i >= testResults.length) {
						pw.print("false");
					} else {
						boolean passed = testResults[i].getOutcome() == TestOutcome.PASSED;
						pw.print(String.valueOf(passed));
						if (!passed) {
							allPassed = false;
						}
					}
				}
				pw.flush();
				
				resultMap.put(sourceFile, sw.toString());
				
				// If the submission did not pass all tests,
				// write failure report to stderr.
				if (!allPassed) {
					writeFailureReport(sourceFile, result, testCaseList);
				}
			
			}
			
			// Print results, in the order of the source files in the source file list.
			for (String sourceFile : sourceFileList) {
				String result = resultMap.get(sourceFile);
				if (result != null) {
					System.out.println(result);
				}
			}
			
		} finally {
			// Shut down workers and wait for threads to finish
			for (int i = 0; i < workers.length; i++) {
				if (workers[i] != null) {
					workers[i].done = true;
					threads[i].interrupt();
					threads[i].join();
				}
			}
		}
	}

	private static void writeFailureReport(String sourceFile, SubmissionResult result, TestCase[] testCaseList) {
		System.err.println("File: " + sourceFile);
		if (result.getCompilationResult().getOutcome() != CompilationOutcome.SUCCESS){
			System.err.println("Did not compile");
		}
		TestResult[] testResults = result.getTestResults();
		for (int i = 0; i < testResults.length; i++) {
			System.err.println(testCaseList[i].getTestCaseName());
			System.err.println(testResults[i].getStdout());
		}
	}
}
