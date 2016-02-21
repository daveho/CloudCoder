package org.cloudcoder.app.wizard.model;

public class EnumValue<E extends Enum<E>> implements IValue {
	private Class<E> enumCls;
	private int value;

	@Override
	public ValueType getModelValueType() {
		throw new IllegalArgumentException();
	}

	@Override
	public void setString(String value) {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> void setEnum(T value) {
		checkTypes(value.getClass(), enumCls);
		this.value = value.ordinal();
	}

	@Override
	public void setBoolean(boolean value) {
		throw new IllegalArgumentException();
	}

	@Override
	public String getString() {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> T getEnum(Class<T> cls) {
		checkTypes(cls, enumCls);
		return cls.getEnumConstants()[value];
	}

	@Override
	public boolean getBoolean() {
		// TODO Auto-generated method stub
		return false;
	}

	private void checkTypes(Class<?> actual, Class<?> expected) {
		if (actual != expected) {
			throw new IllegalArgumentException(
					"Type mismatch: expected " + expected.getSimpleName()
					+ ", got" + actual.getSimpleName());
		}
	}
}
