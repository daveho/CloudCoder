package org.cloudcoder.app.wizard.ui;

import java.awt.Component;
import java.awt.Desktop;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
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
		addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(evt.getURL().toURI());
						} catch (Exception e) {
							System.err.println("Error attempting to browse link: " + e.getMessage());
						}
					}
				}
			}
		});
	}
	
	public void setValue(ImmutableStringValue value) {
		setText("<html><body>" + value.getString() + "</body></html>");
	}
	
	@Override
	public Component asComponent() {
		return this;
	}
	
	@Override
	public int getFieldHeight() {
		// Should this be configurable somehow?
		return 240;
	}
}
