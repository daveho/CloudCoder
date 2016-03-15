package org.cloudcoder.app.wizard.model;

public enum InstallationTask {
	INSTALL_CLOUDCODER("Install CloudCoder", "", "bootstrap"),
	ISSUE_AND_INSTALL_SSL_CERTIFICATE("Issue and install SSL certificate", "Ssl", "installSsl");
	
	private final String desc;
	private final String pageSuffix;
	private final String stepName;
	
	private InstallationTask(String desc, String pageSuffix, String stepName) {
		this.desc = desc;
		this.pageSuffix = pageSuffix;
		this.stepName = stepName;
	}
	
	@Override
	public String toString() {
		return desc;
	}
	
	/**
	 * Get {@link Page} suffix used to designate the finished and error
	 * pages for this installation task.
	 * 
	 * @return the {@link Page} suffix
	 */
	public String getPageSuffix() {
		return pageSuffix;
	}

	/**
	 * Get the main step name for this installation task.
	 * 
	 * @return the main step name
	 */
	public String getStepName() {
		return stepName;
	}
}
