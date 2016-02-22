package org.cloudcoder.app.wizard.exec;

public class ExecException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExecException(String msg) {
		super(msg);
	}

	public ExecException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
