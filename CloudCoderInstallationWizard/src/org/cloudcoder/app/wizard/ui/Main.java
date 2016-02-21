package org.cloudcoder.app.wizard.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.DocumentFactory;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("CloudCoder installation wizard");
				
				Document document = DocumentFactory.create();
				
				WizardPanel panel = new WizardPanel();
				panel.setDocument(document);
				
				frame.setResizable(false);
				frame.setContentPane(panel);
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // should have a close dialog
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
}
