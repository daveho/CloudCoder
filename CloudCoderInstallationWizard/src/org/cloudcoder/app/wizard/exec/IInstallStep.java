package org.cloudcoder.app.wizard.exec;

import java.util.List;

import org.cloudcoder.app.wizard.model.ImmutableStringValue;

/**
 * Interface for an installation step.
 * An installation step consists of one or more {@link IInstallSubStep}s.
 * @author David Hovemeyer
 *
 * @param <E> the implementing class's type
 */
public interface IInstallStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>> {
	/**
	 * @return the unique name of this step
	 */
	public String getName();
	
	/**
	 * @return detailed help text
	 */
	public ImmutableStringValue getHelpText();
	
	/**
	 * @return one-line description of installation step
	 */
	public String getDescription();
	
	/**
	 * @return the sequence of {@link IInstallSubStep}s
	 */
	public List<IInstallSubStep<InfoType, ServiceType>> getInstallSubSteps();
	
	/**
	 * Get named sub-step.
	 * 
	 * @param subStepName the sub-step name
	 * @return the sub-step
	 */
	public IInstallSubStep<InfoType, ServiceType> getSubStepByName(String subStepName);
	
	/**
	 * Set a prerequisite for the execution of a sub-step.
	 * 
	 * @param subStep     the sub-step
	 * @param prereqName  the composite name of the sub-step's prerequisite
	 */
	public void setPrerequisite(IInstallSubStep<InfoType, ServiceType> subStep, String prereqName);
	
	/**
	 * Determine if a sub-step is conditional on the successful
	 * completion of a prerequisite sub-step.
	 * 
	 * @param subStep the sub-step to check
	 * @return true if the sub-step has a prerequisite, false otherwise
	 */
	public boolean isDependent(IInstallSubStep<InfoType, ServiceType> subStep);
	
	/**
	 * Get the full composite name of the given sub-step's prerequisite
	 * sub-step.
	 * 
	 * @param subStep the sub-step with a prerequisite
	 * @return the composite name of the prerequisite sub-step
	 */
	public String getPrerequisiteSubStepName(IInstallSubStep<InfoType, ServiceType> subStep);
}
