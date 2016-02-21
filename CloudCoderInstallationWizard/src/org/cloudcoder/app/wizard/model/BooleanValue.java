package org.cloudcoder.app.wizard.model;

public class BooleanValue implements IValue {
	private boolean value;

	@Override
	public ValueType getModelValueType() {
		return ValueType.BOOLEAN;
	}

	@Override
	public void setString(String value) {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> void setEnum(T value) {
		throw new IllegalArgumentException();
	}

	@Override
	public void setBoolean(boolean value) {
		this.value = value;
	}

	@Override
	public String getString() {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> T getEnum(Class<T> enumCls) {
		throw new IllegalArgumentException();
	}

	@Override
	public boolean getBoolean() {
		return this.value;
	}
}
