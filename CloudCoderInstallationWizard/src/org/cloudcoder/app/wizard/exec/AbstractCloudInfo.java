package org.cloudcoder.app.wizard.exec;

import java.io.File;

public abstract class AbstractCloudInfo {
	private File dataDir;
	
	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}
	
	public File getDataDir() {
		return dataDir;
	}
}
