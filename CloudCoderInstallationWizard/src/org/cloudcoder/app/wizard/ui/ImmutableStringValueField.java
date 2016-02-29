package org.cloudcoder.app.wizard.ui;

import java.awt.Component;
import java.awt.Desktop;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.cloudcoder.app.wizard.model.DisplayOption;
import org.cloudcoder.app.wizard.model.DisplayOptions;
import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public class ImmutableStringValueField extends JEditorPane implements IPageField {
	private static final long serialVersionUID = 1L;
	private HTMLEditorKit kit;
	private ImmutableStringValue value;
	
	public ImmutableStringValueField() {
		setEditable(false);
		setBorder(BorderFactory.createLoweredSoftBevelBorder());
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
		this.value = value;
		setText("<html><body>" + value.getString() + "</body></html>");
	}
	
	@Override
	public Component asComponent() {
		return this;
	}
	
	@Override
	public int getFieldHeight() {
		int height = 240;
		if (value.hasDisplayOption(DisplayOption.HALF_HEIGHT)) {
			height /= 2;
		}
		return height;
	}
	
	@Override
	public void markValid() {
		// Nothing to do
	}
	
	@Override
	public void markInvalid() {
		// Nothing to do, this field cannot become invalid
	}
	
	@Override
	public IValue getCurrentValue() {
		return value.clone();
	}
	
	@Override
	public void setChangeCallback(Runnable callback) {
		// Ignore, since this value never changes
	}
	
	@Override
	public void setSelectiveEnablement(boolean enabled) {
		// Ignore, this kind of field is never selectively enabled/disabled
	}
}
