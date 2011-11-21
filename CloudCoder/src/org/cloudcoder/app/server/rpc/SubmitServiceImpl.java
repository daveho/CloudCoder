package org.cloudcoder.app.server.rpc;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.cloudcoder.app.client.rpc.SubmitService;
import org.cloudcoder.app.server.persist.Database;
import org.cloudcoder.app.server.submitsvc.DefaultSubmitService;
import org.cloudcoder.app.server.submitsvc.ISubmitService;
import org.cloudcoder.app.server.submitsvc.SubmissionException;
import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestResult;
import org.cloudcoder.app.shared.model.User;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SubmitServiceImpl extends RemoteServiceServlet implements SubmitService {
	private static final long serialVersionUID = 1L;

	@Override
	public TestResult[] submit(int problemId, String programText) throws NetCoderAuthenticationException {
		// Make sure that client is authenticated and has permission to edit the given problem
		User user = ServletUtil.checkClientIsAuthenticated(getThreadLocalRequest());
		Problem problem = Database.getInstance().getProblem(user, problemId);
		
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
			System.out.println("Passing submission to submit service...");
			List<TestResult> testResultList = submitService.submit(problem, programText);
			System.out.println("  Done, got " + testResultList.size() + " test results");
			return testResultList.toArray(new TestResult[testResultList.size()]);
		} catch (SubmissionException e) {
			return null; 
		}
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}
}
