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

package org.cloudcoder.importer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.server.submitsvc.oop.OOPBuildServiceSubmission;
import org.cloudcoder.app.server.submitsvc.oop.OutOfProcessSubmitService;
import org.cloudcoder.app.server.submitsvc.oop.ServerTask;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;

/**
 * Re-test one or more submissions.
 * 
 * @author David Hovemeyer
 */
public class ReTest extends UsesDatabase {

	private List<Integer> submissionReceiptIdList;

	public ReTest(String configPropertiesFileName) throws IOException {
		super(configPropertiesFileName);
		this.submissionReceiptIdList = new ArrayList<Integer>();
	}
	
	public void addSubmissionReceiptId(int submissionReceiptId) {
		submissionReceiptIdList.add(submissionReceiptId);
	}

	@Override
	public void run() throws Exception {
		// Start a server thread for communicating with the builder
		ServerSocket serverSocket = new ServerSocket(OutOfProcessSubmitService.DEFAULT_PORT);
		ServerTask serverTask = new ServerTask(serverSocket);
		Thread serverThread = new Thread(serverTask);
		serverThread.start();

		// Retest the submissions
		for (Integer submissionReceiptId : submissionReceiptIdList) {
			try {
				System.out.print("Retesting submission " + submissionReceiptId + "...");
				System.out.flush();
				retestOneSubmission(serverTask, submissionReceiptId);
				System.out.println("done");
			} catch (Exception e) {
				System.out.println("failed, " + e.getMessage());
			}
		}
		
		// We're done!
		serverTask.shutdown();
		serverThread.join();
	}

	private void retestOneSubmission(ServerTask serverTask, int submissionReceiptId) throws SubmissionException {
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
		
		// Delete any old TestResults for submission, and 
		// insert new TestResults, using the submission receipt's existing id
		Database.getInstance().replaceTestResults(result.getTestResults(), receipt.getId());
		
		// Update the submission receipt with the new status, num tests attempted,
		// and num tests passed (which are the details which we would expect
		// might change on a retest.)
		receipt.setStatus(result.determineSubmissionStatus());
		receipt.setNumTestsAttempted(result.getNumTestsAttempted());
		receipt.setNumTestsPassed(result.getNumTestsPassed());
		Database.getInstance().updateSubmissionReceipt(receipt);
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: " + ReTest.class.getName() + " <config properties> <submission receipt id>");
			System.exit(1);
		}
		
		ReTest reTest = new ReTest(args[0]);
		reTest.addSubmissionReceiptId(Integer.parseInt(args[1]));
		reTest.run();
	}

}
