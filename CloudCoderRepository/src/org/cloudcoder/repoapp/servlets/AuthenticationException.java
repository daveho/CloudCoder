package org.cloudcoder.repoapp.servlets;

public class AuthenticationException extends Exception {
	private static final long serialVersionUID = 1L;

	public AuthenticationException(String msg) {
		super(msg);
	}
	
	public AuthenticationException(String msg, Throwable e) {
		super(msg, e);
	}
}
