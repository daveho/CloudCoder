package org.cloudcoder.app.wizard.exec;

import java.io.File;

public abstract class AbstractCloudService<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		implements ICloudService<InfoType, ServiceType>{
	@Override
	public void createDataDir() {
		// Create ccinstall directory if it doesn't exist
		File dataDir = new File(System.getProperty("user.home"), "ccinstall");
		dataDir.mkdirs();
		if (!dataDir.isDirectory()) {
			System.err.println("Could not create " + dataDir.getAbsolutePath());
			System.exit(1);
		}
		getInfo().setDataDir(dataDir);
	}
}
