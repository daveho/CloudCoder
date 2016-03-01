package org.cloudcoder.app.wizard.ui;

import java.awt.Component;

import org.cloudcoder.app.wizard.model.Page;

public interface IWizardPagePanel {
	public enum Type {
		CONFIG,
		INSTALL;
	}
	
	public Type getType();
	public void setPage(Page page);
	public ConfigPanel asConfigPanel();
	public Component asComponent();
}