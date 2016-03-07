package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public class NoopValidator implements IValidator {
	public static final NoopValidator INSTANCE = new NoopValidator();
	
	private NoopValidator() {
		
	}
	
	@Override
	public void validate(Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException {
		// Do nothing
	}
}
