package org.cloudcoder.app.wizard.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.cloudcoder.app.wizard.model.BooleanValue;
import org.cloudcoder.app.wizard.model.IValue;

public class BooleanValueField extends LabeledField<BooleanValue> implements IPageField, UIConstants {
	private static final long serialVersionUID = 1L;
	private JCheckBox checkBox;
	
	public BooleanValueField() {
		this.checkBox = new JCheckBox();
		checkBox.setPreferredSize(new Dimension(FIELD_COMPONENT_WIDTH, FIELD_COMPONENT_HEIGHT));
		add(checkBox);
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Checkbox changed!");
				onChange();
			}
		});
	}
	
	@Override
	public void setValue(BooleanValue value) {
		super.setValue(value);
		checkBox.setSelected(value.getBoolean());
	}

	@Override
	public void markValid() {
		// Nothing to do
	}

	@Override
	public void markInvalid() {
		// Nothing to do, this field can't be invalid
	}

	@Override
	public IValue getCurrentValue() {
		BooleanValue current = getValue().clone();
		current.setBoolean(checkBox.isSelected());
		return current;
	}
	
	@Override
	public void setSelectiveEnablement(boolean enabled) {
		checkBox.setEnabled(enabled);
	}
}
