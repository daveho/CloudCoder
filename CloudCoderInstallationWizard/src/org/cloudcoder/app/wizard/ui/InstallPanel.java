package org.cloudcoder.app.wizard.ui;

import java.awt.Component;

import javax.swing.JPanel;

import org.cloudcoder.app.wizard.model.Page;

public class InstallPanel extends JPanel implements IWizardPagePanel {
	private static final long serialVersionUID = 1L;

	@Override
	public Type getType() {
		return Type.INSTALL;
	}
	
	@Override
	public void setPage(Page page) {
		// Nothing to do, the install Page has no values
	}

	@Override
	public ConfigPanel asConfigPanel() {
		throw new IllegalStateException("Not a ConfigPanel");
	}

	@Override
	public Component asComponent() {
		return this;
	}
}
