package org.cloudcoder.app.wizard.exec;

/**
 * For non-fatal execution exceptions.
 */
public class NonFatalExecException extends ExecException {
	private static final long serialVersionUID = 1L;

	public NonFatalExecException(String msg) {
		super(msg);
	}

	public NonFatalExecException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
