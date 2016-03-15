package org.cloudcoder.app.wizard.exec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

public class InstallationProgress<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		extends Observable {
	private List<IInstallStep<InfoType, ServiceType>> installSteps;
	private int currentStep, currentSubStep;
	private Throwable fatalException;
	private List<IInstallSubStep<InfoType, ServiceType>> succeededSubSteps;
	private List<NonFatalExecException> nonFatalExceptions;
	
	public InstallationProgress() {
		installSteps = new ArrayList<IInstallStep<InfoType, ServiceType>>();
		currentStep = 0;
		currentSubStep = 0;
		succeededSubSteps = new ArrayList<IInstallSubStep<InfoType, ServiceType>>();
		nonFatalExceptions = new ArrayList<NonFatalExecException>();
	}

	public void addInstallStep(IInstallStep<InfoType, ServiceType> step) {
		installSteps.add(step);
	}

	public int getNumSteps() {
		return installSteps.size();
	}

	public int getCurrentStepIndex() {
		return currentStep;
	}

	public int getCurrentSubStepIndex() {
		return currentSubStep;
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
	 * Add a {@link NonFatalExecException}.
	 * 
	 * @param e the {@link NonFatalExecException} to add
	 */
	public void addNonFatalException(NonFatalExecException e) {
		nonFatalExceptions.add(e);
	}
	
	/**
	 * @return list of non-fatal exceptions that occurred (if any)
	 */
	public List<NonFatalExecException> getNonFatalExceptions() {
		return Collections.unmodifiableList(nonFatalExceptions);
	}
	
	/**
	 * @return currently-executing {@link IInstallStep}
	 */
	public IInstallStep<InfoType, ServiceType> getCurrentStep() {
		return installSteps.get(currentStep);
	}
	
	/**
	 * @return currently-executing {@link IInstallSubStep}
	 */
	public IInstallSubStep<InfoType, ServiceType> getCurrentSubStep() {
		return getCurrentStep().getInstallSubSteps().get(currentSubStep);
	}
	
	public IInstallStep<InfoType, ServiceType> getStepByName(String stepName) {
		for (IInstallStep<InfoType, ServiceType> step : installSteps) {
			if (step.getName().equals(stepName)) {
				return step;
			}
		}
		throw new IllegalArgumentException("No step with name " + stepName);
	}
	
	public IInstallSubStep<InfoType, ServiceType> getSubStep(String compositeName) {
		int dot = compositeName.indexOf('.');
		if (dot < 0) {
			throw new IllegalArgumentException("Bad composite name: " + compositeName);
		}
		String stepName = compositeName.substring(0, dot);
		String subStepName = compositeName.substring(dot+1);
		IInstallStep<InfoType, ServiceType> step = getStepByName(stepName);
		IInstallSubStep<InfoType, ServiceType> subStep = step.getSubStepByName(subStepName);
		return subStep;
	}
	
	/**
	 * Mark current {@link IInstallSubStep} as having succeeded or failed.
	 * Note that this method is not called for fatal errors (meaning the
	 * installation can't proceed). 
	 * 
	 * @param succeeded true if the current {@link IInstallSubStep} succeeded,
	 *                  false if it failed
	 */
	public void subStepFinished(boolean succeeded) {
		if (succeeded) {
			// Current sub-step succeeded, yay
			succeededSubSteps.add(getCurrentSubStep());
		}
		
		// Advance to next sub-step
		currentSubStep++;
		if (currentSubStep >= getCurrentStep().getInstallSubSteps().size()) {
			currentSubStep = 0;
			currentStep++;
		}
	}

	public void forceUpdate() {
		setChanged();
		notifyObservers();
	}

	/**
	 * @return total number of sub-steps over all steps
	 */
	public int getTotalSubSteps() {
		int count = 0;
		for (IInstallStep<InfoType, ServiceType> step : installSteps) {
			count += step.getInstallSubSteps().size();
		}
		return count;
	}

	/**
	 * @return the sub-step index relative to the total number of sub-steps
	 */
	public int getCurrentTotalSubStepIndex() {
		int index = 0;
		for (int i = 0; i < currentStep; i++) {
			index += installSteps.get(i).getInstallSubSteps().size();
		}
		index += currentSubStep;
		return index;
	}
	
	public boolean subStepSucceeded(String subStepName) {
		for (IInstallSubStep<InfoType, ServiceType> subStep : succeededSubSteps) {
			if (subStep.getName().equals(subStepName)) {
				return true;
			}
		}
		return false;
	}
}
