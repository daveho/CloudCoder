package org.cloudcoder.app.wizard.ui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;

import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public class ImmutableStringValueField extends JEditorPane implements IPageField {
	private static final long serialVersionUID = 1L;
	private HTMLEditorKit kit;
	
	public ImmutableStringValueField() {
		setEditable(false);
		this.kit = new HTMLEditorKit();
		setDocument(kit.createDefaultDocument());
		setEditorKit(kit);
	}
	
	public void setValue(ImmutableStringValue value) {
		setText("<html><body>" + value.getString() + "</body></html>");
	}
	
	@Override
	public Component asComponent() {
		return this;
	}
}
