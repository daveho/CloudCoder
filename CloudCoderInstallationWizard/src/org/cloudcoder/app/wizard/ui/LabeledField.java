package org.cloudcoder.app.wizard.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.cloudcoder.app.wizard.model.IValue;

public abstract class LabeledField<E extends IValue> extends JPanel implements UIConstants {
	private static final long serialVersionUID = 1L;
	
	private JLabel label;
	private E value;

	public LabeledField() {
		label = new JLabel("", SwingConstants.RIGHT);
		label.setPreferredSize(new Dimension(UIConstants.LABEL_WIDTH, UIConstants.FIELD_COMPONENT_HEIGHT));
		add(label);
	}
	
	public void setValue(E value) {
		this.value = value;
		label.setText(value.getLabel());
	}
	
	public E getValue() {
		return value;
	}
	
	public Component asComponent() {
		return this;
	}

	public int getFieldHeight() {
		return UIConstants.FIELD_HEIGHT;
	}
}
