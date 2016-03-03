package org.cloudcoder.app.wizard.exec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public abstract class AbstractInstallStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		implements IInstallStep<InfoType, ServiceType> {
	private List<IInstallSubStep<InfoType, ServiceType>> subSteps;
	private ImmutableStringValue helpText;
	
	public AbstractInstallStep(String stepName) {
		this.subSteps = new ArrayList<IInstallSubStep<InfoType, ServiceType>>();
		this.helpText = ImmutableStringValue.createHelpText(stepName, "msg", "Help Text");
	}
	
	protected void addSubStep(IInstallSubStep<InfoType, ServiceType> subStep) {
		subSteps.add(subStep);
	}

	public ImmutableStringValue getHelpText() {
		return this.helpText;
	}

	@Override
	public List<IInstallSubStep<InfoType, ServiceType>> getInstallSubSteps() {
		return Collections.unmodifiableList(subSteps);
	}
}
