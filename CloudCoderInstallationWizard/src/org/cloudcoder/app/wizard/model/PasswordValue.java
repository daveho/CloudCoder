package org.cloudcoder.app.wizard.model;

public class PasswordValue extends StringValue {

	public PasswordValue(String name, String label) {
		super(name, label);
	}
	
	public PasswordValue(String name, String label, String defValue) {
		super(name, label, defValue);
	}
	
	@Override
	public ValueType getValueType() {
		return ValueType.PASSWORD;
	}
}
