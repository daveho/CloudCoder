package org.cloudcoder.app.wizard.exec;


public abstract class AbstractBootstrapStep<
		InfoType extends ICloudInfo,
		ServiceType extends ICloudService<InfoType, ServiceType>>
	extends AbstractInstallStep<InfoType, ServiceType> {

	protected class EstablishSshConnectivitySubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
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
	
	protected class DownloadBootstrapScriptSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
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
	
	protected class UploadBootstrapPropertiesSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
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
	
	protected class RunBootstrapScriptSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
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
	
	protected class ConfigureDuckDnsDNSHostnameSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
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
	
	protected class VerifyHostnameSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		private boolean checkPublicIp;
		
		public VerifyHostnameSubStep(boolean checkPublicIp) {
			super("verifyHostname");
			this.checkPublicIp = checkPublicIp;
		}
		
		@Override
		public String getDescription() {
			return "Verify dynamic DNS hostname";
		}
		
		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.verifyDnsHostname(checkPublicIp);
		}
	}
	
	protected class LetsEncryptSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
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
	
	protected Bootstrap<InfoType, ServiceType> bootstrap;
	
	public AbstractBootstrapStep(String stepName, ServiceType cloudService) {
		super(stepName);
		this.bootstrap = new Bootstrap<InfoType, ServiceType>(cloudService);
	}
}
