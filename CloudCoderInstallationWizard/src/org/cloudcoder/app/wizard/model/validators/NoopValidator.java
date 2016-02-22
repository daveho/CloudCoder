package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;

public class NoopValidator implements IValidator {
	public static final NoopValidator INSTANCE = new NoopValidator();
	
	@Override
	public void validate(IValue origValue, IValue updatedValue) throws ValidationException {
		// Do nothing
	}
}
