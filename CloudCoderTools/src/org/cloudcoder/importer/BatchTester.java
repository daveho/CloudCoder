package org.cloudcoder.importer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.ParseException;

import org.cloudcoder.app.server.submitsvc.oop.OOPBuildServiceSubmission;
import org.cloudcoder.app.server.submitsvc.oop.OutOfProcessSubmitService;
import org.cloudcoder.app.server.submitsvc.oop.ServerTask;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.dom4j.DocumentException;

/**
 * Test a bunch of programs against a CloudCoder problem
 * specified by an XML file. This does really have anything
 * with CloudCoder per se: it's just a convenient way to
 * automatically test a whole bunch of programs.
 * Requires that a Builder is running and connect to the
 * port this process is listening on.
 *  
 * @author David Hovemeyer
 */
public class BatchTester {
	private String problemXml;
	private String fileNameList;
	private ServerTask serverTask;
	
	public BatchTester(String problemXml, String fileNameList) {
		this.problemXml = problemXml;
		this.fileNameList = fileNameList;
	}

	public void execute() throws IOException, DocumentException, ParseException, InterruptedException, SubmissionException {
		FileReader r = new FileReader(problemXml);
		ProblemWithTestCases problemWithTestCases;
		try {
			problemWithTestCases = new ProblemReader().read(r);
		} finally {
			r.close();
		}
		
		// Set fake problem id and course id
		problemWithTestCases.getProblem().setProblemId(1);
		problemWithTestCases.getProblem().setCourseId(1);
		
		// Start a server thread for communicating with the builder
		ServerSocket serverSocket = new ServerSocket(OutOfProcessSubmitService.DEFAULT_PORT);
		this.serverTask = new ServerTask(serverSocket);
		Thread serverThread = new Thread(serverTask);
		serverThread.start();
		
		try {
			testAll(fileNameList, problemWithTestCases);
		} finally {
			serverTask.shutdown();
			serverThread.join();
			serverSocket.close();
		}
	}

	private void testAll(String fileNameList, ProblemWithTestCases problemWithTestCases)
			throws FileNotFoundException, IOException, SubmissionException, InterruptedException {
		BufferedReader reader = new BufferedReader(new FileReader(fileNameList));
		try {
			String fileName;
			while (true) {
				fileName = reader.readLine();
				if (fileName == null) {
					break;
				}
				testOne(problemWithTestCases, fileName);
			}
		} finally {
			reader.close();
		}
	}

	private void testOne(ProblemWithTestCases problemWithTestCases, String fileName) throws IOException, SubmissionException, InterruptedException {
		String programText = readProgram(fileName);
		
		OOPBuildServiceSubmission future = new OOPBuildServiceSubmission(
				new Submission(
						problemWithTestCases.getProblem(),
						problemWithTestCases.getTestCaseList(),
						programText));
		
		serverTask.submit(future);

		SubmissionResult result;
		while (true) {
			result = future.poll();
			if (result != null) {
				break;
			}
			Thread.sleep(200L);
		}
		
		if (fileName.startsWith("./")) {
			fileName = fileName.substring(2);
		}
		
		System.out.println("program: " + fileName);
		if (result.getCompilationResult().getOutcome() != CompilationOutcome.SUCCESS) {
			System.out.println("compiled: false");
			return;
		}
		System.out.println("compiled: true");

		for (int i = 0; i < problemWithTestCases.getTestCaseList().size(); i++) {
			TestCase testCase = problemWithTestCases.getTestCaseList().get(i);
			TestResult testResult = result.getTestResults()[i];

			String outcome;
			switch (testResult.getOutcome()) {
			case PASSED: outcome = "passed"; break;
			case FAILED_WITH_EXCEPTION: outcome = "crashed"; break;
			case FAILED_FROM_TIMEOUT: outcome = "timeout"; break;
			default: outcome = "failed"; break;
			}
			System.out.println(
					"test " +
					testCase.getTestCaseName() +
					":" +
					outcome
					);
			if (testResult.getOutcome() != TestOutcome.PASSED) {
				String output = testResult.getStdout();
				if (!output.endsWith("\n")) {
					output = output + "\n";
				}
				System.out.print(output);
			}
		}
	}

	private String readProgram(String fileName) throws IOException {
		StringBuilder buf = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		try {
			char[] text = new char[65536];
			while (true) {
				int n = reader.read(text, 0, text.length);
				if (n < 0) {
					break;
				}
				buf.append(text, 0, n);
			}
			return buf.toString();
		} finally {
			reader.close();
		}
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println(
					"Usage: " +
					BatchTester.class.getName() +
					" <problem xml> <file with list of program filenames>");
			System.exit(1);
		}
		
		String problemXml = args[0];
		String fileNameList = args[1];

		BatchTester tester = new BatchTester(problemXml, fileNameList);
		tester.execute();
	}
}
