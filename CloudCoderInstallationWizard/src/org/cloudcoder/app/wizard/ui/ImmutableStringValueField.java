package org.cloudcoder.app.wizard.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.cloudcoder.app.wizard.model.DisplayOption;
import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public class ImmutableStringValueField extends JPanel implements IPageField, UIConstants {
	private static final long serialVersionUID = 1L;
	private JEditorPane editPane;
	private HTMLEditorKit kit;
	private ImmutableStringValue value;
	
	public ImmutableStringValueField() {
		setLayout(new BorderLayout());
		
		this.editPane = new JEditorPane();
		
		editPane.setEditable(false);
		setBorder(BorderFactory.createLoweredSoftBevelBorder());
		this.kit = new HTMLEditorKit();
		editPane.setDocument(kit.createDefaultDocument());
		editPane.setEditorKit(kit);
		editPane.addHyperlinkListener(new HyperlinkListener() {
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
		
		JScrollPane scrollPane = new JScrollPane(editPane);
		
		add(scrollPane, BorderLayout.CENTER);
	}
	
	public void setValue(ImmutableStringValue value) {
		this.value = value;
		editPane.setText("<html><body>" + value.getString() + "</body></html>");
	}
	
	@Override
	public Component asComponent() {
		return this;
	}
	
	@Override
	public int getFieldHeight() {
		int height = FULL_HELP_TEXT_HEIGHT;
		if (value.hasDisplayOption(DisplayOption.HALF_HEIGHT)) {
			height /= 2;
			height += 20; // tweak for ccAcct page
		} else if (value.hasDisplayOption(DisplayOption.DOUBLE_HEIGHT)) {
			//height *= 2;
			// Double height would be too big, so go with 1.7x height
			height = (height * 1700) / 1000;
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
	
	@Override
	public void updateValue(IValue value) {
		setValue((ImmutableStringValue)value);
	}
}
