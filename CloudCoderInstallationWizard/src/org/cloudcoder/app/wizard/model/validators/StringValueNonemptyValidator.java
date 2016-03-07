package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public class StringValueNonemptyValidator implements IValidator {
	public static final StringValueNonemptyValidator INSTANCE = new StringValueNonemptyValidator();

	private StringValueNonemptyValidator() {
		
	}
	
	@Override
	public void validate(Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException {
		if (updatedValue.getString().trim().equals("")) {
			throw new ValidationException(origValue, updatedValue, "Value must be nonempty");
		}
	}
}
