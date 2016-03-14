package org.cloudcoder.app.wizard.exec;

public abstract class AbstractCloudService<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		implements ICloudService<InfoType, ServiceType>, InstallationConstants {
	
	public AbstractCloudService() {
		// Create the data directory if it doesn't exist already
		Util.createDataDir();
	}
}
