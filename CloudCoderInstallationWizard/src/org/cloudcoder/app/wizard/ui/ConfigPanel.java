package org.cloudcoder.app.wizard.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.Page;

public class ConfigPanel extends JPanel implements IWizardPagePanel, UIConstants {
	private static final long serialVersionUID = 1L;

	private JPanel content;
	private Page page;
	private List<IPageField> fields;
	private Runnable changeCallback;
	
	public ConfigPanel() {
		setLayout(new BorderLayout());
		
		this.content = new JPanel();
		BoxLayout boxLayout = new BoxLayout(content, BoxLayout.Y_AXIS);
		content.setLayout(boxLayout);

		fields = new ArrayList<IPageField>();

		// Callback to execute when UI values change -
		// used to update selective enablement.
		this.changeCallback = new Runnable() {
			@Override
			public void run() {
				updateSelectiveEnablement();
			}
		};
		
		JScrollPane scrollPane = new JScrollPane(content);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public Type getType() {
		return Type.CONFIG;
	}
	
	@Override
	public ConfigPanel asConfigPanel() {
		return this;
	}
	
	@Override
	public InstallPanel asInstallPanel() {
		throw new IllegalStateException("Not an InstallPanel");
	}
	
	@Override
	public Component asComponent() {
		return this;
	}
	
	/**
	 * Get a version of the {@link Page} with updated values
	 * from the UI fields.
	 * 
	 * @return {@link Page} containing current UI field values
	 */
	public Page getCurrentValues() {
		Page current = page.clone();
		for (int i = 0; i < page.getNumValues(); i++) {
			current.set(i, fields.get(i).getCurrentValue());
		}
		return current;
	}

	@Override
	public void setPage(Page page) {
		this.page = page;
		for (IValue v : page) {
			IPageField field = PageFieldFactory.createForValue(v);
			fields.add(field);
			Component component = field.asComponent();
			component.setPreferredSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, field.getFieldHeight()));
			component.setMaximumSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, field.getFieldHeight()));
			content.add(component);
			field.setChangeCallback(this.changeCallback);
		}
		updateSelectiveEnablement();
	}
	
	@Override
	public void resyncFields(Page page) {
		for (int i = 0; i < page.getNumValues(); i++) {
			fields.get(i).updateValue(page.get(i));
		}
	}

	public IPageField getField(int index) {
		return fields.get(index);
	}

	public void markAllValid() {
		for (IPageField field : fields) {
			field.markValid();
		}
	}

	private void updateSelectiveEnablement() {
		if (page == null) {
			return;
		}
		Page current = getCurrentValues();
		// Do selective enablement based on current values
		for (int i = 0; i < current.getNumValues(); i++) {
			fields.get(i).setSelectiveEnablement(current.isEnabled(current.get(i).getName()));
		}
	}
}
