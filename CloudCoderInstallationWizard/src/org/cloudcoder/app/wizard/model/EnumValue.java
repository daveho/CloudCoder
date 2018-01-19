package org.cloudcoder.app.wizard.model;

public class EnumValue<E extends Enum<E>> extends AbstractValue implements IValue {
	private Class<E> enumCls;
	private E value;
	
	public EnumValue(Class<E> enumCls, String name, String label) {
		this(enumCls, name, label, enumCls.getEnumConstants()[0]);
	}
	
	public EnumValue(Class<E> enumCls, String name, String label, E defaultValue) {
		super(name, label);
		this.enumCls = enumCls;
		value = defaultValue; // the first enum member is the default
	}
	
	public Class<E> getEnumCls() {
		return enumCls;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.ENUM_CHOICE;
	}

	@Override
	public void setString(String value) {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> void setEnum(T value) {
		checkTypes(value.getClass(), enumCls);
		this.value = enumCls.getEnumConstants()[value.ordinal()];
	}

	@Override
	public void setBoolean(boolean value) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void setPropertyValue(String propValue) {
		// Find named enumeration
		propValue = propValue.trim();
		for (E member : enumCls.getEnumConstants()) {
			if (member.name().equals(propValue)) {
				value = member;
				return;
			}
		}
		throw new IllegalArgumentException("Enum " + enumCls.getSimpleName() + " has no member " + propValue);
	}
	
	@Override
	public void update(IValue otherValue) {
		if (otherValue.getClass() != this.getClass()) {
			throw new IllegalArgumentException("Cannot update " + this.getClass().getSimpleName() +
					" from " + otherValue.getClass().getSimpleName());
		}
		EnumValue<?> other = (EnumValue<?>)otherValue;
		if (other.enumCls != this.enumCls) {
			throw new IllegalArgumentException("Cannot update " + this.enumCls.getSimpleName() +
					" EnumValue from " + other.enumCls.getSimpleName() + " EnumValue");
		}
		@SuppressWarnings("unchecked")
		E otherEnumValue = (E)other.value;
		this.value = otherEnumValue;
	}

	@Override
	public String getString() {
		throw new IllegalArgumentException();
	}

	@Override
	public <T extends Enum<T>> T getEnum(Class<T> cls) {
		checkTypes(cls, enumCls);
		return cls.getEnumConstants()[value.ordinal()];
	}

	@Override
	public boolean getBoolean() {
		throw new IllegalArgumentException();
	}
	
	@Override
	public String getPropertyValue() {
		// Save the name of the enumeration constant, not the string
		// representation.
		return enumCls.getEnumConstants()[value.ordinal()].name();
	}
	
	@Override
	public boolean isEnum(Enum<?> enumVal) {
		// Note that we don't actually need to use the generic types
		// here because the reference equality will only evaluate
		// to true if enumVal and value are the same enum
		// constant.
		return enumVal == value;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public EnumValue<E> clone() {
		try {
			return (EnumValue<E>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Should not happen");
		}
	}

	private void checkTypes(Class<?> actual, Class<?> expected) {
		if (actual != expected) {
			throw new IllegalArgumentException(
					"Type mismatch: expected " + expected.getSimpleName()
					+ ", got" + actual.getSimpleName());
		}
	}
}
