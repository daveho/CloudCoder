package org.cloudcoder.app.wizard.ui;

import java.awt.Dimension;
import java.awt.LayoutManager;

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

	public LabeledField(LayoutManager layout) {
		super(layout);
	}

	public LabeledField(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public LabeledField(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public int getFieldHeight() {
		return UIConstants.FIELD_HEIGHT;
	}
}
