package org.cloudcoder.app.wizard.model;

public class FilenameValue extends StringValue {
	public FilenameValue(String name, String label) {
		super(name, label);
	}
	
	@Override
	public ValueType getValueType() {
		return ValueType.FILENAME;
	}
	
	@Override
	public FilenameValue clone() {
		return (FilenameValue) super.clone();
	}
}
