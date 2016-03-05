package org.cloudcoder.app.wizard.exec;

/**
 * Execution exception, by default should be considered fatal.
 * However, {@link NonFatalExecException}s should be considered
 * non-fatal.
 */
public class ExecException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExecException(String msg) {
		super(msg);
	}

	public ExecException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
