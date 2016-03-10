package org.cloudcoder.app.wizard.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
	
	private InstallationProgress<?,?> progress;
	private JProgressBar overallProgressBar;
	private JProgressBar subStepProgressBar;
	
	private static final Font STEP_DESCRIPTION_LABEL_FONT = new Font(Font.DIALOG, Font.PLAIN, 16);
	
	public InstallPanel() {
		stepDescription = new JLabel();
		stepDescription.setFont(STEP_DESCRIPTION_LABEL_FONT);
		setComponentSize(stepDescription, FIELD_HEIGHT);
		stepDescription.setHorizontalAlignment(SwingConstants.CENTER);
		add(stepDescription);
		
		helpTextField = new ImmutableStringValueField();
		setComponentSize(helpTextField, FULL_HELP_TEXT_HEIGHT/2);
		add(helpTextField);
		
		subStepDescription = new JLabel();
		subStepDescription.setFont(STEP_DESCRIPTION_LABEL_FONT);
		setComponentSize(subStepDescription, FIELD_HEIGHT);
		subStepDescription.setHorizontalAlignment(SwingConstants.CENTER);
		add(subStepDescription);
		
		JLabel stepProgressLabel = new JLabel("Overall progress:");
		setComponentSize(stepProgressLabel, FIELD_HEIGHT);
		add(stepProgressLabel);
		
		overallProgressBar = new JProgressBar();
		overallProgressBar.setStringPainted(true);
		setComponentSize(overallProgressBar, FIELD_HEIGHT);
		add(overallProgressBar);
		
		JLabel subStepProgressLabel = new JLabel("Progress for current step:");
		setComponentSize(subStepProgressLabel, FIELD_HEIGHT);
		add(subStepProgressLabel);
		
		subStepProgressBar = new JProgressBar();
		subStepProgressBar.setStringPainted(true);
		setComponentSize(subStepProgressBar, FIELD_HEIGHT);
		add(subStepProgressBar);
	}

	private void setComponentSize(Component comp, int fieldHeight) {
		comp.setPreferredSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, fieldHeight));
		comp.setMaximumSize(new Dimension(SINGLE_COMPONENT_FIELD_WIDTH, fieldHeight));
	}
	
	public void setProgress(InstallationProgress<?,?> progress) {
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
	public void resyncFields(Page page) {
		// Nothing to do, this page doesn't display document fields
	}
	
	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("InstallPanel received a notification?");
		
		// This is a notification from the thread running the installation
		// that progress has been made (or a fatal exception has occurred.)
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				syncProgress();
			}
		});
	}

	protected void syncProgress() {
		//System.out.println("Syncing InstallPanel with InstallationProgress...");
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
		
		overallProgressBar.setMinimum(0);
		int overallTotal = progress.getTotalSubSteps();
		int overallIndex = progress.getCurrentTotalSubStepIndex();
		overallProgressBar.setMaximum(overallTotal);
		overallProgressBar.setValue(overallIndex);
		overallProgressBar.setString(overallIndex + "/" + overallTotal);
		
		subStepProgressBar.setMinimum(0);
		int curStepTotal = progress.getCurrentStep().getInstallSubSteps().size();
		int curStepIndex = progress.getCurrentSubStepIndex()+1;
		subStepProgressBar.setMaximum(curStepTotal);
		subStepProgressBar.setValue(curStepIndex);
		subStepProgressBar.setString(curStepIndex + "/" + curStepTotal);
	}
}
