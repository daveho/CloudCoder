package org.cloudcoder.app.wizard.exec.aws;

import org.cloudcoder.app.wizard.exec.AbstractInstallStep;
import org.cloudcoder.app.wizard.exec.AbstractInstallSubStep;
import org.cloudcoder.app.wizard.exec.ExecException;
import org.cloudcoder.app.wizard.model.Document;

public class ProvisioningInstallStep extends AbstractInstallStep<AWSInfo, AWSCloudService> {
	private static class LoginSubStep extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			return "Log in to AWS";
		}

		@Override
		public void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.login();
		}
	}
	
	private static class FindOrCreateVPCSubStep extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			return "Find or create cloudcoder PC (virtual private cloud)";
		}

		@Override
		public void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.findOrCreateVpc();
		}
	}
	
	private class LoadOrCreateKeypairSubStep extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			Document document = aws.getDocument();
			return document.getValue("awsKeypair.useExisting").getBoolean()
					? "Loading keypair"
					: "Creating new keypair";
		}
		
		@Override
		public void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.createOrChooseKeypair();
		}
	}
	
	private static class FindOrCreateSecurityGroup extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			return "Finding or creating a security group";
		}
		
		@Override
		public void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.findOrCreateSecurityGroup();
		}
	}
	
	private static class FindUbuntuServerImageSubStep extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			return "Find Ubuntu server image";
		}
		
		@Override
		public void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.findUbuntuServerImage();
		}
	}
	
	private static class CreateWebappElasticIPSubStep extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			return "Create an elastic IP address for the webapp instance";
		}
		
		@Override
		public void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.createWebappElasticIp();
		}
	}
	
	private static class CreateWebappInstanceSubStep extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			return "Create and start webapp instance virtual machine";
		}
		
		@Override
		protected void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.createWebappInstance();
		}
	}
	
	private static class WaitForWebappInstanceToStartSubStep extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			return "Waiting for webapp instance to start";
		}
		
		@Override
		public void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.waitForInstanceToStart();
		}
	}
	
	private static class AssignPublicIpToWebappInstanceSubStep extends AbstractInstallSubStep<AWSInfo, AWSCloudService> {
		@Override
		public String getDescription() {
			return "Assign elastic IP address to webapp instance";
		}
		
		@Override
		public void doExecute(AWSCloudService cloudService) throws ExecException {
			cloudService.assignPublicIpToWebapp();
		}
	}
	
	private AWSCloudService aws;
	
	public ProvisioningInstallStep(AWSCloudService aws) {
		super("provisioningStep");
		this.aws = aws;
		addSubStep(new LoginSubStep());
		addSubStep(new FindOrCreateVPCSubStep());
		addSubStep(new LoadOrCreateKeypairSubStep());
		addSubStep(new FindOrCreateSecurityGroup());
		addSubStep(new FindUbuntuServerImageSubStep());
		addSubStep(new CreateWebappInstanceSubStep());
		addSubStep(new WaitForWebappInstanceToStartSubStep());
		addSubStep(new CreateWebappElasticIPSubStep());
		addSubStep(new AssignPublicIpToWebappInstanceSubStep());
	}

	@Override
	public String getDescription() {
		return "Provisioning network and server resources";
	}
}
