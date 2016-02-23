package org.cloudcoder.app.wizard.model.validators;

import java.io.File;

import org.cloudcoder.app.wizard.model.IValue;

public class FileReadableValidator implements IValidator {
	@Override
	public void validate(IValue origValue, IValue updatedValue) throws ValidationException {
		String fileName = updatedValue.getString();
		File f = new File(fileName);
		if (!f.exists()) {
			throw new ValidationException(origValue, updatedValue, "File " + fileName + " does not exist");
		}
		if (!f.canRead()) {
			throw new ValidationException(origValue, updatedValue, "File " + fileName + " is not readable");
		}
	}
}
