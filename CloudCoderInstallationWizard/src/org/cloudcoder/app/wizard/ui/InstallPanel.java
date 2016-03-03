package org.cloudcoder.app.wizard.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.cloudcoder.app.wizard.exec.InstallationProgress;
import org.cloudcoder.app.wizard.model.Page;

public class InstallPanel extends JPanel implements IWizardPagePanel, Observer, UIConstants {
	private static final long serialVersionUID = 1L;
	
	private JLabel stepDescription;
	private ImmutableStringValueField helpTextField;
	private JLabel subStepDescription;
	// TODO: progress indicator for steps
	// TODO: progress indicator for sub-steps in current step
	
	private InstallationProgress progress;
	
	public InstallPanel() {
		stepDescription = new JLabel();
		stepDescription.setPreferredSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, FIELD_HEIGHT));
		stepDescription.setMaximumSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, FIELD_HEIGHT));
		stepDescription.setHorizontalAlignment(SwingConstants.CENTER);
		add(stepDescription);
		helpTextField = new ImmutableStringValueField();
		helpTextField.setPreferredSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, FULL_HELP_TEXT_HEIGHT/2));
		helpTextField.setMaximumSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, FULL_HELP_TEXT_HEIGHT/2));
		add(helpTextField);
		subStepDescription = new JLabel();
		subStepDescription.setPreferredSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, FIELD_HEIGHT));
		subStepDescription.setMaximumSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, FIELD_HEIGHT));
		subStepDescription.setHorizontalAlignment(SwingConstants.CENTER);
		add(subStepDescription);
	}
	
	public void setProgress(InstallationProgress progress) {
		this.progress = progress;
		progress.addObserver(this);
	}

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
	public InstallPanel asInstallPanel() {
		return this;
	}

	@Override
	public Component asComponent() {
		return this;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		System.out.println("InstallPanel received a notification?");
		// This is a notification from the thread running the installation
		// that progress has been made (or a fatal exception has occurred.)
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				sync();
			}
		});
	}

	protected void sync() {
		System.out.println("Syncing InstallPanel with InstallationProgress...");
		if (progress.isFinished()) {
			// TODO
			System.out.println("Installation finished - should update UI and re-enable next button");
			return;
		}
		if (progress.isFatalException()) {
			// TODO
			System.err.println("A fatal installation exception occurred - should update UI and display error message");
			progress.getFatalException().printStackTrace(System.err);
			return;
		}
		stepDescription.setText(progress.getCurrentStep().getDescription());
		helpTextField.setValue(progress.getCurrentStep().getHelpText());
		subStepDescription.setText(progress.getCurrentSubStep().getDescription());
		// TODO: update progress indicators
	}
}
