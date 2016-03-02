package org.cloudcoder.app.wizard.exec;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class InstallationProgress extends Observable {
	private List<IInstallStep> installSteps;
	private int currentStep, currentSubStep;
	public Throwable fatalException;
	
	public InstallationProgress() {
		installSteps = new ArrayList<IInstallStep>();
		currentStep = 0;
		currentSubStep = 0;
	}
	
	/**
	 * @return true if the installation has completed successfully, false otherwise
	 */
	public boolean isFinished() {
		return currentStep >= installSteps.size();
	}
	
	/**
	 * @return true if the installation failed due to a fatal exception, false otherwise
	 */
	public boolean isFatalException() {
		return fatalException != null;
	}
	
	/**
	 * Set a fatal exception, terminating the installation abnormally.
	 * 
	 * @param fatalException the fatal exception to set
	 */
	public void setFatalException(Throwable fatalException) {
		this.fatalException = fatalException;
		notifyObservers();
	}
	
	/**
	 * @return the fatal exception that terminated the installation abnormally
	 */
	public Throwable getFatalException() {
		if (fatalException == null) {
			throw new IllegalStateException();
		}
		return fatalException;
	}
	
	/**
	 * @return currently-executing {@link IInstallStep}
	 */
	public IInstallStep getCurrentStep() {
		return installSteps.get(currentStep);
	}
	
	/**
	 * @return currently-executing {@link IInstallSubStep}
	 */
	public IInstallSubStep getCurrentSubStep() {
		return getCurrentStep().getInstallSubSteps().get(currentSubStep);
	}
	
	private void subStepFinished() {
		currentSubStep++;
		if (currentSubStep >= getCurrentStep().getInstallSubSteps().size()) {
			currentSubStep = 0;
			currentStep++;
		}
	}
	
	/**
	 * Synchronously execute all installation steps/sub-steps
	 * until either the installation finishes or a fatal exception
	 * occurs.
	 */
	public void executeAll() {
		while (!isFinished() && !isFatalException()) {
			notifyObservers(); // Allow UI to update itself
			try {
				getCurrentSubStep().execute();
				subStepFinished();
			} catch (ExecException e) {
				setFatalException(e);
				break;
			}
		}
	}
}
