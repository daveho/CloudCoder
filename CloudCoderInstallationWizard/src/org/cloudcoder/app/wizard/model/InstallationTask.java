package org.cloudcoder.app.wizard.model;

public enum InstallationTask {
	INSTALL_CLOUDCODER("Install CloudCoder", ""),
	ISSUE_AND_INSTALL_SSL_CERTIFICATE("Issue and install SSL certificate", "Ssl");
	
	private final String desc;
	private final String pageSuffix;
	
	private InstallationTask(String desc, String pageSuffix) {
		this.desc = desc;
		this.pageSuffix = pageSuffix;
	}
	
	@Override
	public String toString() {
		return desc;
	}
	
	public String getPageSuffix() {
		return pageSuffix;
	}
}
