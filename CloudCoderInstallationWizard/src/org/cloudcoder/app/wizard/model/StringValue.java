package org.cloudcoder.app.wizard.model;

public class StringValue implements IValue {
	private String value;

	@Override
	public ValueType getModelValueType() {
		return ValueType.STRING;
	}

	@Override
	public void setString(String value) {
		this.value = value;
	}

	@Override
	public <T extends Enum<T>> void setEnum(T value) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void setBoolean(boolean value) {
		throw new IllegalArgumentException();
	}

	@Override
	public String getString() {
		return this.value;
	}
	
	@Override
	public boolean getBoolean() {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> T getEnum(Class<T> enumCls) {
		throw new IllegalArgumentException();
	}
}
