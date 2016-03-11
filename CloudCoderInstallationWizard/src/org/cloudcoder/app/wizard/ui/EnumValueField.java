package org.cloudcoder.app.wizard.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.cloudcoder.app.wizard.model.EnumValue;
import org.cloudcoder.app.wizard.model.IValue;

public class EnumValueField<E extends Enum<E>> extends LabeledField<EnumValue<E>> implements IPageField, UIConstants {
	private static final long serialVersionUID = 1L;
	
	private JComboBox<E> comboBox;
	
	public EnumValueField() {
		comboBox = new JComboBox<E>();
		comboBox.setPreferredSize(new Dimension(FIELD_COMPONENT_WIDTH, FIELD_COMPONENT_HEIGHT));
		add(comboBox);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onChange();
			}
		});
	}
	
	@Override
	public void setValue(EnumValue<E> value) {
		super.setValue(value);
		Class<E> enumCls = value.getEnumCls();
		if (comboBox.getItemCount() == 0) {
			E[] members = enumCls.getEnumConstants();
			for (E member : members) {
				comboBox.addItem(member);
			}
		}
		comboBox.setSelectedItem(value.getEnum(enumCls));
	}

	@Override
	public int getFieldHeight() {
		return FIELD_HEIGHT;
	}

	@Override
	public void markValid() {
		// Nothing to do
	}

	@Override
	public void markInvalid() {
		// Nothing to do, this field can't become invalid
	}

	@Override
	public IValue getCurrentValue() {
		EnumValue<E> current = getValue().clone();
		@SuppressWarnings("unchecked")
		E selectedItem = (E)comboBox.getSelectedItem();
		System.out.printf("getCurrentValue, selectedItem %s null\n", selectedItem == null ? "is" : "is not");
		current.setEnum(selectedItem);
		return current;
	}
	
	@Override
	public void setSelectiveEnablement(boolean enabled) {
		comboBox.setEnabled(enabled);
	}
	
	@Override
	public void updateValue(IValue value) {
		@SuppressWarnings("unchecked")
		EnumValue<E> v = (EnumValue<E>)value;
		setValue(v);
	}
	
	public static<T extends Enum<T>> EnumValueField<T> createForEnumClass(Class<T> enumCls) {
		return new EnumValueField<T>();
	}
}
