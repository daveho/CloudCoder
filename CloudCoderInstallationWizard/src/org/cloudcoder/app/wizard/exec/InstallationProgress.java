package org.cloudcoder.app.wizard.exec;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class InstallationProgress extends Observable {
	private List<IInstallStep> installSteps;
	private int currentStep, currentSubStep;
	
	public InstallationProgress() {
		installSteps = new ArrayList<IInstallStep>();
		currentStep = 0;
		currentSubStep = 0;
	}
	
	public boolean isFinished() {
		return currentStep >= installSteps.size();
	}
	
	public IInstallStep getCurrentStep() {
		return installSteps.get(currentStep);
	}
	
	public IInstallSubStep getCurrentSubStep() {
		return getCurrentStep().getInstallSubSteps().get(currentSubStep);
	}
	
	public void subStepFinished() {
		currentSubStep++;
		if (currentSubStep >= getCurrentStep().getInstallSubSteps().size()) {
			currentSubStep = 0;
			currentStep++;
		}
		notifyObservers();
	}
}
