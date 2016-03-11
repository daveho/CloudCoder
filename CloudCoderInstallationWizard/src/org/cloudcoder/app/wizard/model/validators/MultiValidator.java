package org.cloudcoder.app.wizard.model.validators;

import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

/**
 * Composite {@link IValidator} which applies multiple delegate
 * validators.
 */
public class MultiValidator implements IValidator {
	private List<IValidator> delegates;
	
	public MultiValidator(IValidator... delegates) {
		this.delegates = Arrays.asList(delegates);
	}

	@Override
	public void validate(Document document, Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException {
		for (IValidator delegate : delegates) {
			delegate.validate(document, currentValues, origValue, updatedValue);
		}
	}
}
