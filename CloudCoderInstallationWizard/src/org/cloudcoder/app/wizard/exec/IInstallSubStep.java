package org.cloudcoder.app.wizard.exec;

public interface IInstallSubStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>> {
	/**
	 * @return the name of the substep, will be unique within the overall {@link IInstallStep}
	 */
	public String getName();
	
	/**
	 * @return one-line description of the sub step
	 */
	public String getDescription();
	
	/**
	 * Execute synchronously.
	 * 
	 * @param cloudService the {@link ICloudService}
	 * @throws ExecException
	 */
	public void execute(ServiceType cloudService) throws ExecException;
}
