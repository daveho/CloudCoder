package org.cloudcoder.app.wizard.ui;

import javax.swing.JTabbedPane;

import org.cloudcoder.app.wizard.model.Document;

public class WizardTopLevelPanel extends JTabbedPane {
	private static final long serialVersionUID = 1L;

	private WizardPanel wizardPanel;
	private LogPanel logPanel;
	
	public WizardTopLevelPanel() {
		wizardPanel = new WizardPanel();
		logPanel = new LogPanel();
		
		addTab("Wizard", wizardPanel);
		addTab("Output log", logPanel);
	}
	
	public void setDocument(Document document) {
		wizardPanel.setDocument(document);
	}
}
