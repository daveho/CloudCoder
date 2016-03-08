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
	private List<NonFatalExecException> nonFatalExceptions;
	
	public InstallationProgress() {
		installSteps = new ArrayList<IInstallStep<InfoType, ServiceType>>();
		currentStep = 0;
		currentSubStep = 0;
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
	public void executeAll(ServiceType cloudService) {
		while (!isFinished() && !isFatalException()) {
			forceUpdate(); // Allow UI to update itself
			IInstallSubStep<InfoType, ServiceType> subStep = getCurrentSubStep();
			try {
				System.out.println("Executing installation sub-step " + subStep.getClass().getSimpleName());
				subStep.execute(cloudService);
				System.out.println("Sub-step " + subStep.getClass().getSimpleName() + " completed successfully");
				subStepFinished();
			} catch (NonFatalExecException e) {
				nonFatalExceptions.add(e);
				System.err.println("Sub-step " +
						subStep.getClass().getSimpleName() + " failed with non-fatal exception: " +
						e.getMessage());
				e.printStackTrace(System.err);
				subStepFinished();
			} catch (ExecException e) {
				System.err.println("Fatal exception occurred executing sub-step " + subStep.getClass().getSimpleName());
				e.printStackTrace();
				setFatalException(e);
			}
		}
		// If the loop terminated, then either the installation finished
		// successfully, or there was a fatal exception.  Let the UI know.
		forceUpdate();
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
}
