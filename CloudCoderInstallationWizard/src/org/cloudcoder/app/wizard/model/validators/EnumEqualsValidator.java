package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

/**
 * Validate field only if a value is set to a particular
 * enumeration value.
 */
public class EnumEqualsValidator implements IValidator {
	private String enumValueName;
	private Enum<?> testVal;
	private IValidator delegate;
	
	public EnumEqualsValidator(String enumValueName, Enum<?> testVal, IValidator delegate) {
		this.enumValueName = enumValueName;
		this.testVal = testVal;
		this.delegate = delegate;
	}
	
	@Override
	public void validate(Document document, Page currentValues, IValue origValue, IValue updatedValue)
			throws ValidationException {
		if (document.getValue(enumValueName).isEnum(testVal)) {
			delegate.validate(document, currentValues, origValue, updatedValue);
		}
	}
}
