package org.cloudcoder.app.wizard.exec;

import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public abstract class AbstractInstallStep {
	private final String stepName;
	private List<IInstallSubStep> subSteps;
	
	public AbstractInstallStep(String stepName) {
		this.stepName = stepName;
	}
	
	protected void addSubSteps(IInstallSubStep... subSteps) {
		this.subSteps = Arrays.asList(subSteps);
	}

	public ImmutableStringValue getHelpText() {
		return ImmutableStringValue.createHelpText(stepName, "msg", "Help Text");
	}

	public List<IInstallSubStep> getInstallSubSteps() {
		return subSteps;
	}
}
