package org.cloudcoder.app.wizard.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public class WizardPagePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Page page;
	private List<IPageField> fields; 
	
	public WizardPagePanel() {
		fields = new ArrayList<IPageField>();
		setBackground(Color.LIGHT_GRAY);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	public void setPage(Page page) {
		this.page = page;
		for (IValue v : page) {
			IPageField field = PageFieldFactory.createForValue(v);
			fields.add(field);
			add(field.asComponent());
		}
	}
}
