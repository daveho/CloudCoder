package org.cloudcoder.app.wizard.exec;


public class BootstrapStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		extends AbstractInstallStep<InfoType, ServiceType> {
	private ServiceType cloudService;
	private Bootstrap bootstrap;
	
	public BootstrapStep(ServiceType cloudService) {
		super("bootstrap");
		this.cloudService = cloudService;
		this.bootstrap = new Bootstrap<InfoType, ServiceType>(cloudService);
		
		// TODO: add sub-steps
	}

	@Override
	public String getDescription() {
		return "Bootstrap the webapp instance by installing/configuring the CloudCoder software";
	}
}
