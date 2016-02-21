package org.cloudcoder.app.wizard.model;

public interface IValue {
	public ValueType getModelValueType();
	
	public void setString(String value);
	public<T extends Enum<T>> void setEnum(T value);
	public void setBoolean(boolean value);
	
	public String getString();
	public<T extends Enum<T>> T getEnum(Class<T> enumCls);
	public boolean getBoolean();
}
