package org.cloudcoder.app.server.rpc;

import java.util.List;

import org.cloudcoder.app.client.rpc.SubmitService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.submitsvc.DefaultSubmitService;
import org.cloudcoder.app.server.submitsvc.ISubmitService;
import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.Change;
import org.cloudcoder.app.shared.model.ChangeType;
import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.IContainsEvent;
import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionReceipt;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.SubmissionStatus;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SubmitServiceImpl extends RemoteServiceServlet implements SubmitService {
	private static final long serialVersionUID = 1L;
	private static final Logger logger=LoggerFactory.getLogger(SubmitServiceImpl.class);

	@Override
	public SubmissionResult submit(int problemId, String programText) throws NetCoderAuthenticationException {
		// Make sure that client is authenticated and has permission to edit the given problem
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
		Problem problem = Database.getInstance().getProblem(user, problemId);
		if (problem == null) {
			throw new NetCoderAuthenticationException();
		}

		// Insert a full-text change into the database.
		Change fullTextChange = new Change(
				ChangeType.FULL_TEXT,
				0, 0, 0, 0,
				System.currentTimeMillis(),
				user.getId(), problem.getProblemId(),
				programText);
		Database.getInstance().storeChanges(new Change[]{fullTextChange});
		
		// Get test cases.  (TODO: cache them?)
		List<TestCase> testCaseList = Database.getInstance().getTestCasesForProblem(problemId);
		
		ISubmitService submitService = DefaultSubmitService.getInstance();
		try {
			logger.info("Passing submission to submit service...");
			SubmissionResult result = submitService.submit(problem, testCaseList, programText);
			
			// Add a SubmissionReceipt to the database
			SubmissionReceipt receipt = createSubmissionReceipt(fullTextChange, result);
			// TODO: insert the receipt into the database
			Database.getInstance().insertSubmissionReceipt(receipt);
			
			int numResult=0;
			if (result!=null && result.getTestResults()!=null) {
			    numResult=result.getTestResults().length;
			}
			logger.info("Compilation "+result.getCompilationResult()+", received " +
			        numResult+" TestResults");
			return result;
		} catch (SubmissionException e) {
		    logger.error("SubmissionException", e);
			return null; 
		}
	}

	private SubmissionReceipt createSubmissionReceipt(IContainsEvent mostRecentChange, SubmissionResult result) {
		SubmissionReceipt receipt = new SubmissionReceipt();
		receipt.setLastEditEventId(mostRecentChange.getEventId());
		SubmissionStatus status;
		if (result.getCompilationResult().getOutcome() == CompilationOutcome.SUCCESS) {
			// Check to see whether or not all tests passed
			status = SubmissionStatus.TESTS_PASSED;
			for (TestResult testResult : result.getTestResults()) {
				if (testResult.getOutcome() != TestOutcome.PASSED) {
					status = SubmissionStatus.TESTS_FAILED;
					break;
				}
			}
		} else if (result.getCompilationResult().getOutcome() == CompilationOutcome.FAILURE) {
			// Compile error(s)
			status = SubmissionStatus.COMPILE_ERROR;
		} else {
			// Something unexpected prevented compilation and/or testing
			status = SubmissionStatus.BUILD_ERROR;
		}
		receipt.setStatus(status);
		
		return receipt;
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}
}
