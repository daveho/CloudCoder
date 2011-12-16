package org.cloudcoder.app.server.rpc;

import java.util.List;

import org.cloudcoder.app.client.rpc.SubmitService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.submitsvc.DefaultSubmitService;
import org.cloudcoder.app.server.submitsvc.ISubmitService;
import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
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
		List<TestCase> testCaseList = Database.getInstance().getTestCasesForProblem(problemId);
		
//		// For now, a dummy implementation
//		
//		System.out.println("Submitted code:");
//		System.out.println(programText);
//		
//		//return new TestResult[0];
//		TestResult aResult = new TestResult("passed", "You rule, dude", "Hello, world", "Oh yeah");
//		
//		return new TestResult[]{ aResult };
		
		ISubmitService submitService = DefaultSubmitService.getInstance();
		try {
			logger.info("Passing submission to submit service...");
			SubmissionResult result = submitService.submit(problem, testCaseList, programText);
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
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}
}
