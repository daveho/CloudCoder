package org.cloudcoder.app.wizard.ui;

import javax.swing.JPasswordField;

import org.cloudcoder.app.wizard.model.PasswordValue;

public class PasswordValueField extends StringValueField<PasswordValue> {
	private static final long serialVersionUID = 1L;

	public PasswordValueField() {
		super(new JPasswordField());
	}
}
