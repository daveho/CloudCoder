package org.cloudcoder.app.wizard.ui;

import java.awt.Component;

import javax.swing.JLabel;

import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public class ImmutableStringValueField extends JLabel implements IPageField {
	private static final long serialVersionUID = 1L;
	
	public ImmutableStringValueField() {
	}
	
	public void setValue(ImmutableStringValue value) {
		setText("<html>" + value.getString() + "</html>");
	}
	
	@Override
	public Component asComponent() {
		return this;
	}
}
