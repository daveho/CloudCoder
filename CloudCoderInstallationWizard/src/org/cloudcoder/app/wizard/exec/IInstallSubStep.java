package org.cloudcoder.app.wizard.exec;

public interface IInstallSubStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>> {
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
