package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;

public class StringValueNonemptyValidator implements IValidator {
	public static final StringValueNonemptyValidator INSTANCE = new StringValueNonemptyValidator();
	
	@Override
	public void validate(IValue origValue, IValue updatedValue) throws ValidationException {
		if (updatedValue.getString().trim().equals("")) {
			throw new ValidationException(origValue, updatedValue, "Value must be nonempty");
		}
	}
}
