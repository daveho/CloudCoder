package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;

public class ValidationException extends Exception {
	private static final long serialVersionUID = 1L;

	private final IValue origValue;
	private final IValue invalidValue;

	public ValidationException(IValue origValue, IValue invalidValue, String msg) {
		super(msg);
		this.origValue = origValue;
		this.invalidValue = invalidValue;
	}

	public IValue getOrigValue() {
		return origValue;
	}
	
	public IValue getInvalidValue() {
		return invalidValue;
	}
}
