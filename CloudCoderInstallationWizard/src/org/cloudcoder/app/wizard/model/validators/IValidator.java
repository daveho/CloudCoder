package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public interface IValidator {
	/**
	 * Validate an {@link IValue}.
	 * 
	 * @param currentValues   the {@link Page} containing the current (UI) values on the page
	 * @param origValue       the original value of the {@link IValue} being validated
	 * @param updatedValue    the updated (UI) value of the {@link IValue} being validated
	 * @throws ValidationException if the validation fails
	 */
	public void validate(Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException;
}
