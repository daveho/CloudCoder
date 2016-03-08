package org.cloudcoder.app.wizard.exec;


public class BootstrapStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		extends AbstractInstallStep<InfoType, ServiceType> {
	private class EstablishSshConnectivitySubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
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
		@Override
		public String getDescription() {
			return "Running the bootstrap script to install CloudCoder (takes several minutes)";
		}

		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.runBootstrapScript();
		}
	}
	
	public class ConfigureDuckDnsDNSHostnameSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {
		@Override
		public String getDescription() {
			return "Using Duck DNS to configure hostname";
		}
		
		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			bootstrap.configureDuckDnsHostName();
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
		addSubStep(new ConfigureDuckDnsDNSHostnameSubStep());
	}

	@Override
	public String getDescription() {
		return "Bootstrap the webapp instance by installing/configuring the CloudCoder software";
	}
}
