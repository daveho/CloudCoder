package org.cloudcoder.app.wizard.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public class WizardPagePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Page page;
	private List<IPageField> fields;
	
	public WizardPagePanel() {
		fields = new ArrayList<IPageField>();
		setLayout(new FlowLayout());
	}
	
	public void setPage(Page page) {
		this.page = page;
		for (IValue v : page) {
			IPageField field = PageFieldFactory.createForValue(v);
			fields.add(field);
			Component component = field.asComponent();
			component.setPreferredSize(new Dimension(720, field.getFieldHeight()));
			add(component);
		}
	}
}
