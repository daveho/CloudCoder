package org.cloudcoder.app.wizard.model;

public class ImmutableStringValue extends StringValue {

	public ImmutableStringValue(String name, String value) {
		super(name);
		super.setString(value);
	}
	
	@Override
	public ValueType getValueType() {
		return ValueType.IMMUTABLE_STRING;
	}

	@Override
	public void setString(String value) {
		throw new IllegalArgumentException();
	}
}
