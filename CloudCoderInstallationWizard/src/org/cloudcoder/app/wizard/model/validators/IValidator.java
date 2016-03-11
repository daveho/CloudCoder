package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public interface IValidator {
	/**
	 * Validate an {@link IValue}.
	 * 
	 * @param document        the {@link Document}; may be used for checking values
	 *                        on pages other than the current one
	 * @param currentValues   the {@link Page} containing the current (UI) values on the page;
	 *                        note that these may not be the same values as in the
	 *                        corresponding page in the {@link Document}, since they
	 *                        reflect the current UI state
	 * @param origValue       the original value of the {@link IValue} being validated
	 * @param updatedValue    the updated (UI) value of the {@link IValue} being validated
	 * @throws ValidationException if the validation fails
	 */
	public void validate(Document document, Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException;
}
