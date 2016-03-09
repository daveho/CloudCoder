package org.cloudcoder.app.wizard.exec;


public class BootstrapStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		extends AbstractInstallStep<InfoType, ServiceType> {
	private class EstablishSshConnectivitySubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		public EstablishSshConnectivitySubStep() {
			super("establishSsh");
		}
		
		@Override
		public String getDescription() {
			return "Establishing ssh connectivity with webapp instance";
		}
		
		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.establishSshConnectivity();
		}
	}
	
	private class DownloadBootstrapScriptSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		public DownloadBootstrapScriptSubStep() {
			super("downloadBootstrapScript");
		}
		
		@Override
		public String getDescription() {
			return "Downloading bootstrap script on webapp instance";
		}

		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.downloadBootstrapScript();
		}
	}
	
	private class UploadBootstrapPropertiesSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		public UploadBootstrapPropertiesSubStep() {
			super("uploadBootstrapProperties");
		}
		
		@Override
		public String getDescription() {
			return "Upload the configuration properties to the webapp instance";
		}

		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.uploadBootstrapProperties();
		}
	}
	
	private class RunBootstrapScriptSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		public RunBootstrapScriptSubStep() {
			super("runBootstrapScript");
		}
		
		@Override
		public String getDescription() {
			return "Running the bootstrap script to install CloudCoder (takes several minutes)";
		}

		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.runBootstrapScript();
		}
	}
	
	private class ConfigureDuckDnsDNSHostnameSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		public ConfigureDuckDnsDNSHostnameSubStep() {
			super("configureDuckDns");
		}
		
		@Override
		public String getDescription() {
			return "Using Duck DNS to configure hostname";
		}
		
		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.configureDuckDnsHostName();
		}
	}
	
	private class VerifyHostnameSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		public VerifyHostnameSubStep() {
			super("verifyHostname");
		}
		
		@Override
		public String getDescription() {
			return "Verify dynamic DNS hostname";
		}
		
		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.verifyDnsHostname();
		}
	}
	
	private class LetsEncryptSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		public LetsEncryptSubStep() {
			super("letsencrypt");
		}
		
		@Override
		public String getDescription() {
			return "Issue and install Let's Encrypt SSL certificate";
		}
		
		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.letsEncrypt();
		}
	}
	
	private Bootstrap<InfoType, ServiceType> bootstrap;
	
	public BootstrapStep(ServiceType cloudService) {
		super("bootstrap");
		this.bootstrap = new Bootstrap<InfoType, ServiceType>(cloudService);
		
		// Add sub-steps
		addSubStep(new EstablishSshConnectivitySubStep());
		addSubStep(new DownloadBootstrapScriptSubStep());
		addSubStep(new UploadBootstrapPropertiesSubStep());
		addSubStep(new RunBootstrapScriptSubStep());
		
		ConfigureDuckDnsDNSHostnameSubStep duckDnsSubStep = new ConfigureDuckDnsDNSHostnameSubStep();
		addSubStep(duckDnsSubStep);
		
		// Configuring DNS is a prerequisite for verifying DNS (for somewhat obvious reasons)
		VerifyHostnameSubStep verifyDnsSubStep = new VerifyHostnameSubStep();
		addSubStep(verifyDnsSubStep);
		setPrerequisite(verifyDnsSubStep, getName() + "." + duckDnsSubStep.getName());
		
		// Verifying DNS is a prerequisite for using Let's Encrypt to issue/install an SSL cert
		LetsEncryptSubStep letsEncryptSubStep = new LetsEncryptSubStep();
		addSubStep(letsEncryptSubStep);
		setPrerequisite(letsEncryptSubStep, getName() + "." + verifyDnsSubStep.getName());
	}

	@Override
	public String getDescription() {
		return "Bootstrap the webapp instance by installing/configuring the CloudCoder software";
	}
}
