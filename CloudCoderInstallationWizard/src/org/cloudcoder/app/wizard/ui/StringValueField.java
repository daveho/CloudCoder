package org.cloudcoder.app.wizard.ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JTextField;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.StringValue;

public class StringValueField<E extends StringValue> extends LabeledField<E> implements IPageField, UIConstants {
	private static final long serialVersionUID = 1L;
	
	private JTextField textField;
	
	public StringValueField() {
		this(new JTextField());
	}
	
	protected StringValueField(JTextField tf) {
		textField = tf;
		textField.setPreferredSize(new Dimension(FIELD_COMPONENT_WIDTH, FIELD_COMPONENT_HEIGHT));
		add(textField);
		textField.getDocument().addDocumentListener(new ChangeReportingDocumentListener());
		markValid();
	}
	
	@Override
	public void setValue(E value) {
		super.setValue(value);
		textField.setText(value.getString());
	}

	@Override
	public void markValid() {
		textField.setBackground(Color.WHITE);
	}
	
	@Override
	public void markInvalid() {
		textField.setBackground(INVALID_FIELD_BG);
	}
	
	@Override
	public IValue getCurrentValue() {
		IValue cur = getValue().clone();
		cur.setString(textField.getText());
		return cur;
	}
	
	@Override
	public void setSelectiveEnablement(boolean enabled) {
		textField.setEnabled(enabled);
	}
}
