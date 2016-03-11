package org.cloudcoder.app.wizard.exec;

public class BootstrapStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		extends AbstractBootstrapStep<InfoType, ServiceType> {
	public BootstrapStep(ServiceType cloudService) {
		super("bootstrap", cloudService);
		
		// Add sub-steps
		addSubStep(new EstablishSshConnectivitySubStep());
		addSubStep(new DownloadBootstrapScriptSubStep());
		addSubStep(new UploadBootstrapPropertiesSubStep());
		addSubStep(new RunBootstrapScriptSubStep());
		
		ConfigureDuckDnsDNSHostnameSubStep duckDnsSubStep = new ConfigureDuckDnsDNSHostnameSubStep();
		addSubStep(duckDnsSubStep);
		
		// Configuring DNS is a prerequisite for verifying DNS (for somewhat obvious reasons)
		VerifyHostnameSubStep verifyDnsSubStep = new VerifyHostnameSubStep(true);
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
