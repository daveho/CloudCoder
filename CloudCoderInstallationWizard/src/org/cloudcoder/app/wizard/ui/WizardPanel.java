package org.cloudcoder.app.wizard.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cloudcoder.app.wizard.model.Document;

public class WizardPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Document document;

	public WizardPanel() {
		setPreferredSize(new Dimension(800, 600));
		setBackground(Color.LIGHT_GRAY);

		setLayout(new BorderLayout());
	}

	public void setDocument(Document document) {
		this.document = document;
		// FIXME: for now, just add a single WizardPagePanel for the first Page
		WizardPagePanel panel = new WizardPagePanel();
		panel.setPage(document.get(1));
		add(panel, BorderLayout.CENTER);
	}
}
