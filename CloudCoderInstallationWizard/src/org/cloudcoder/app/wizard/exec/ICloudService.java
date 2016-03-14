package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.Document;

public interface ICloudService<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>> {
	public static final String CLOUDCODER_KEYPAIR_NAME = "cloudcoder-keypair";
	
	/**
	 * Get the {@link Document} that stores the user-provided
	 * configuration information.
	 * 
	 * @return the {@link Document}
	 */
	public Document getDocument();
	
	/**
	 * Add {@link IInstallStep}s to the specified {@link InstallationProgress}
	 * object.  This allows the progress object to carry out the installation
	 * in a provider-specific manner.
	 */
	public void addInstallSteps(InstallationProgress<InfoType, ServiceType> progress);
	
	/**
	 * @return the {@link ICloudInfo}
	 */
	public InfoType getInfo();
}
