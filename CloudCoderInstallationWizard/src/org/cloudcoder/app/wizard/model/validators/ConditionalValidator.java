package org.cloudcoder.app.wizard.model.validators;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

/**
 * Validate field only if a boolean field is checked (true).
 */
public class ConditionalValidator implements IValidator {
	private String conditionValueName;
	private IValidator delegate;

	/**
	 * Constructor.
	 * 
	 * @param conditionValueName the name of the condition {@link BooleanValue};
	 *                           validation will proceed only if the value named
	 *                           is true
	 * @param delegate the {@link IValidator} which will do the actual validation
	 */
	public ConditionalValidator(String conditionValueName, IValidator delegate) {
		this.conditionValueName = conditionValueName;
		this.delegate = delegate;
	}
	
	@Override
	public void validate(Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException {
		if (currentValues.getValue(conditionValueName).getBoolean()) {
			delegate.validate(currentValues, origValue, updatedValue);
		}
	}
}
