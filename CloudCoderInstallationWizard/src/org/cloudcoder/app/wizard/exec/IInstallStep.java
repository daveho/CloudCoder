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
}
