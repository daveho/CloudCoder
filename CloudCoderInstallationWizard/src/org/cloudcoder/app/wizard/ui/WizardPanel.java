package org.cloudcoder.app.wizard.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.Page;

public class WizardPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Document document;
	private int currentPage;
	private JButton prevButton;
	private JButton nextButton;
	private JPanel pagePanel;

	public WizardPanel() {
		setPreferredSize(new Dimension(800, 600));

		setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.prevButton = new JButton("<< Previous");
		this.nextButton = new JButton("Next >>");
		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);
		buttonPanel.setPreferredSize(new Dimension(800, 40));
		add(buttonPanel, BorderLayout.PAGE_END);
		
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onNext();
			}
		});
		prevButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onPrevious();
			}
		});
		
		// Panel with CardLayout for displaying the WizardPagePanels
		this.pagePanel = new JPanel();
		pagePanel.setLayout(new CardLayout());
		add(pagePanel, BorderLayout.CENTER);
	}

	protected void onNext() {
		currentPage++;
		changePage();
	}

	protected void onPrevious() {
		currentPage--;
		changePage();
	}

	public void setDocument(Document document) {
		this.document = document;
		
		// Create WizardPagePanels
		for (int i = 0; i < document.getNumPages(); i++) {
			Page p = document.get(i);
			WizardPagePanel pp = new WizardPagePanel();
			pp.setPage(p);
			pagePanel.add(pp, String.valueOf(i));
		}
		
		currentPage = 0;
		changePage();
	}

	private void changePage() {
		CardLayout cl = (CardLayout) pagePanel.getLayout();
		cl.show(pagePanel, String.valueOf(currentPage));
		prevButton.setEnabled(currentPage > 0);
		nextButton.setEnabled(currentPage < document.getNumPages() - 1);
	}
}
