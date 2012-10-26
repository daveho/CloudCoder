package org.cloudcoder.app.server.persist;

public class PersistenceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PersistenceException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
