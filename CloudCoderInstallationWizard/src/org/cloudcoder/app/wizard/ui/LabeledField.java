package org.cloudcoder.app.wizard.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cloudcoder.app.wizard.model.DisplayOption;
import org.cloudcoder.app.wizard.model.IValue;

public abstract class LabeledField<E extends IValue> extends JPanel implements UIConstants {
	// Subclasses which edit text can use this to report changes
	protected final class ChangeReportingDocumentListener implements DocumentListener {
		@Override
		public void removeUpdate(DocumentEvent e) {
			onChange();
		}
	
		@Override
		public void insertUpdate(DocumentEvent e) {
			onChange();
		}
	
		@Override
		public void changedUpdate(DocumentEvent e) {
			onChange();
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	private JLabel label;
	private E value;
	private Runnable changeCallback;

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
		int height = UIConstants.FIELD_HEIGHT;
		if (value.hasDisplayOption(DisplayOption.HALF_HEIGHT)) {
			height /= 2;
		}
		return height;
	}
	
	public void setChangeCallback(Runnable callback) {
		this.changeCallback = callback;
	}
	
	protected void onChange() {
		if (changeCallback != null) {
			changeCallback.run();
		}
	}
}
