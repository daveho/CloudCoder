package org.cloudcoder.app.wizard.model.validators;

import java.io.File;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public class FileReadableValidator implements IValidator {
	public static final FileReadableValidator INSTANCE = new FileReadableValidator();
	
	private FileReadableValidator() {
	}
	
	@Override
	public void validate(Page currentValues, IValue origValue, IValue updatedValue) throws ValidationException {
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
