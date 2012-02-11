package org.cloudcoder.importer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.submitsvc.oop.OOPBuildServiceSubmission;
import org.cloudcoder.app.server.submitsvc.oop.OutOfProcessSubmitService;
import org.cloudcoder.app.server.submitsvc.oop.ServerTask;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.TestCase;

public class ReTest extends UsesDatabase {

	private int submissionReceiptId;

	public ReTest(String configPropertiesFileName, int submissionReceiptId) throws IOException {
		super(configPropertiesFileName);
		this.submissionReceiptId = submissionReceiptId;
	}

	@Override
	public void run() throws Exception {
		// Start a server thread for communicating with the builder
		ServerSocket serverSocket = new ServerSocket(OutOfProcessSubmitService.DEFAULT_PORT);
		ServerTask serverTask = new ServerTask(serverSocket);
		Thread serverThread = new Thread(serverTask);
		serverThread.start();

		// Find the SubmissionReceipt of the submission to retest
		SubmissionReceipt receipt = Database.getInstance().getSubmissionReceipt(submissionReceiptId);
		if (receipt == null) {
			throw new IllegalArgumentException("No submission receipt with id " + submissionReceiptId);
		}
		
		// Find the Problem, TestCases, and the program text
		Problem problem = Database.getInstance().getProblem(receipt.getEvent().getProblemId());
		List<TestCase> testCaseList = Database.getInstance().getTestCasesForProblem(problem.getProblemId());
		Change fullTextChange = Database.getInstance().getChange(receipt.getLastEditEventId());
		if (fullTextChange ==null) {
			System.out.println("Could not find Change with event id=" + receipt.getLastEditEventId());
		}
		if (fullTextChange.getType() != ChangeType.FULL_TEXT) {
			throw new IllegalArgumentException("Submission receipt not linked to a full-text change");
		}
		Submission submission = new Submission(problem, testCaseList, fullTextChange.getText());
		
		// Resubmit the submission to the Builder.
		SubmissionResult result = serverTask.submit(new OOPBuildServiceSubmission(submission));
		
		// Insert new TestResults, using the submission receipt's existing id
		Database.getInstance().insertTestResults(result.getTestResults(), receipt.getId());
		
		// Update the submission receipt with the new status, num tests attempted,
		// and num tests passed (which are the details which we would expect
		// might change on a retest.)
		receipt.setStatus(result.determineSubmissionStatus());
		receipt.setNumTestsAttempted(result.getNumTestsAttempted());
		receipt.setNumTestsPassed(result.getNumTestsPassed());
		Database.getInstance().updateSubmissionReceipt(receipt);
		
		// We're done!
		serverTask.shutdown();
		serverThread.join();
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: " + ReTest.class.getName() + " <config properties> <submission receipt id>");
			System.exit(1);
		}
		
		new ReTest(args[0], Integer.parseInt(args[1])).run();
	}

}
