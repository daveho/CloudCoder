package org.cloudcoder.app.wizard.model;

public enum InstallationTask {
	INSTALL_CLOUDCODER("Install CloudCoder"),
	ISSUE_AND_INSTALL_SSL_CERTIFICATE("Issue and install SSL certificate");
	
	private final String desc;
	
	private InstallationTask(String desc) {
		this.desc = desc;
	}
	
	@Override
	public String toString() {
		return desc;
	}
}
