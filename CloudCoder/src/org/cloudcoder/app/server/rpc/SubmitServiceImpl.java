package org.cloudcoder.app.server.rpc;

import org.cloudcoder.app.client.rpc.SubmitService;
import org.cloudcoder.app.shared.model.NetCoderAuthenticationException;
import org.cloudcoder.app.shared.model.TestResult;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SubmitServiceImpl extends RemoteServiceServlet implements SubmitService {
	private static final long serialVersionUID = 1L;

	@Override
	public TestResult[] submit(int problemId, String programText) throws NetCoderAuthenticationException {
		// TODO: check that client is authenticated and has permission to edit the given problem
		
		// For now, a dummy implementation
		
		System.out.println("Submitted code:");
		System.out.println(programText);
		
		return new TestResult[0];
	}
}
