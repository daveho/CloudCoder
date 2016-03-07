package org.cloudcoder.app.wizard.model;

public class EnablePageIfEnumSelected<E extends Enum<E>> implements ISelectivePageEnablement {
	private String valueName;
	private Class<E> enumCls;
	private E value;
	
	public EnablePageIfEnumSelected(String valueName, Class<E> enumCls, E value) {
		this.valueName = valueName;
		this.enumCls = enumCls;
		this.value = value;
	}

	@Override
	public boolean isEnabled(Document document) {
		E selected = document.getValue(valueName).getEnum(enumCls);
		System.out.printf("selected=%s, value=%s\n", selected.name(), value.name());
		return selected == value;
	}
}
