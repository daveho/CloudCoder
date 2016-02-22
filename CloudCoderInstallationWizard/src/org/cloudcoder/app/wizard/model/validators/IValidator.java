package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;

public interface IValidator {
	public void validate(IValue origValue, IValue updatedValue) throws ValidationException;
}
