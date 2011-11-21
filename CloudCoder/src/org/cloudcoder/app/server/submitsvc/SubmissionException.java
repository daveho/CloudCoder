package org.cloudcoder.app.server.submitsvc;

public class SubmissionException extends Exception {
	private static final long serialVersionUID = 1L;

	public SubmissionException(String msg) {
		super(msg);
	}
	
	public SubmissionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
