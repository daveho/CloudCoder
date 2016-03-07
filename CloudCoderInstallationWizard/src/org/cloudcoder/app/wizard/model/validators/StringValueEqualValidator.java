package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;
import org.cloudcoder.app.wizard.model.StringValue;

/**
 * Validate that a {@link StringValue} is the same
 * as another {@link StringValue} on the same {@link Page}.
 * Useful for password confirmation fields.
 */
public class StringValueEqualValidator implements IValidator {
	private String otherStringValueName;
	
	public StringValueEqualValidator(String otherStringValueName) {
		this.otherStringValueName = otherStringValueName;
	}
	
	@Override
	public void validate(Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException {
		IValue other = currentValues.getValue(otherStringValueName);
		if (!other.getString().equals(updatedValue.getString())) {
			String msg = "Value does not match value of " + other.getLabel() + " field";
			throw new ValidationException(origValue, updatedValue, msg);
		}
	}
}
