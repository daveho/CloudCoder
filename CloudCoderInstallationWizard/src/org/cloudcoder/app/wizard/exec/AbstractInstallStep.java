package org.cloudcoder.app.wizard.exec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.wizard.model.ImmutableStringValue;

public abstract class AbstractInstallStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		implements IInstallStep<InfoType, ServiceType> {
	private String stepName;
	private List<IInstallSubStep<InfoType, ServiceType>> subSteps;
	private ImmutableStringValue helpText;
	private Map<String, String> prereqMap;
	
	public AbstractInstallStep(String stepName) {
		this.stepName = stepName;
		this.subSteps = new ArrayList<IInstallSubStep<InfoType, ServiceType>>();
		this.helpText = ImmutableStringValue.createHelpText(stepName, "msg", "Help Text");
		this.prereqMap = new HashMap<String, String>();
	}
	
	protected void addSubStep(IInstallSubStep<InfoType, ServiceType> subStep) {
		subSteps.add(subStep);
	}
	
	@Override
	public String getName() {
		return stepName;
	}

	@Override
	public ImmutableStringValue getHelpText() {
		return this.helpText;
	}

	@Override
	public List<IInstallSubStep<InfoType, ServiceType>> getInstallSubSteps() {
		return Collections.unmodifiableList(subSteps);
	}
	
	@Override
	public IInstallSubStep<InfoType, ServiceType> getSubStepByName(String subStepName) {
		for (IInstallSubStep<InfoType, ServiceType> subStep : subSteps) {
			if (subStep.getName().equals(subStepName)) {
				return subStep;
			}
		}
		throw new IllegalArgumentException("No sub-step with name " + subStepName);
	}
	
	@Override
	public void setPrerequisite(IInstallSubStep<InfoType, ServiceType> subStep, String prereqName) {
		prereqMap.put(subStep.getName(), prereqName);
	}
	
	@Override
	public boolean isDependent(IInstallSubStep<InfoType, ServiceType> subStep) {
		return prereqMap.containsKey(subStep.getName());
	}
	
	@Override
	public String getPrerequisiteSubStepName(IInstallSubStep<InfoType, ServiceType> subStep) {
		return prereqMap.get(subStep.getName());
	}
}
