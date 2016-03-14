package org.cloudcoder.app.wizard.exec;

public abstract class AbstractCloudInfo implements ICloudInfo {
	private boolean privateKeyGenerated;
	
	@Override
	public void setPrivateKeyGenerated(boolean b) {
		this.privateKeyGenerated = b;
	}
	
	@Override
	public boolean isPrivateKeyGenerated() {
		return this.privateKeyGenerated;
	}
}
