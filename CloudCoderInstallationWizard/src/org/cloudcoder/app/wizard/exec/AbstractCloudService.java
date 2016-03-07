package org.cloudcoder.app.wizard.exec;

public abstract class AbstractCloudService<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		implements ICloudService<InfoType, ServiceType>, InstallationConstants {
	@Override
	public void createDataDir() {
		Util.createDataDir();
		getInfo().setDataDir(DATA_DIR);
	}
}
