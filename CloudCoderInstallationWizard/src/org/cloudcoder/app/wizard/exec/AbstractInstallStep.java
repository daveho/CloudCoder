package org.cloudcoder.app.wizard.exec;

import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public abstract class AbstractInstallStep {
	private List<IInstallSubStep> subSteps;
	private ImmutableStringValue helpText;
	
	public AbstractInstallStep(String stepName) {
		this.helpText = ImmutableStringValue.createHelpText(stepName, "msg", "Help Text");
	}
	
	protected void addSubSteps(IInstallSubStep... subSteps) {
		this.subSteps = Arrays.asList(subSteps);
	}

	public ImmutableStringValue getHelpText() {
		return this.helpText;
	}

	public List<IInstallSubStep> getInstallSubSteps() {
		return subSteps;
	}
}
