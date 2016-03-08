package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public class StringValueEndsInSuffixValidator implements IValidator {
	private String suffix;
	private boolean ignoreCase;

	/**
	 * Constructor.
	 * 
	 * @param suffix      the suffix to test
	 * @param ignoreCase  true if case should be ignored, false otherwise
	 */
	public StringValueEndsInSuffixValidator(String suffix, boolean ignoreCase) {
		this.suffix = suffix;
		this.ignoreCase = ignoreCase;
	}

	@Override
	public void validate(Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException {
		String value = updatedValue.getString();
		String testSuffix = suffix;
		
		if (ignoreCase) {
			value = value.toLowerCase();
			testSuffix = testSuffix.toLowerCase();
		}
		
		if (!value.endsWith(testSuffix)) {
			throw new ValidationException(
					origValue,
					updatedValue,
					"Value " + updatedValue.getString() + " does not end with " + suffix);
		}
	}
}
