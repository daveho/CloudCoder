package org.cloudcoder.app.wizard.ui;

import java.awt.Dimension;

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
}
