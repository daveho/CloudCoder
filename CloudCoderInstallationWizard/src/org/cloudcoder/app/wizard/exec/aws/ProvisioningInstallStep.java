package org.cloudcoder.app.wizard.exec.aws;

import org.cloudcoder.app.wizard.exec.AbstractInstallStep;
import org.cloudcoder.app.wizard.exec.ExecException;
import org.cloudcoder.app.wizard.exec.IInstallStep;
import org.cloudcoder.app.wizard.exec.IInstallSubStep;
import org.cloudcoder.app.wizard.model.Document;

public class ProvisioningInstallStep extends AbstractInstallStep implements IInstallStep {
	private class LoginSubStep implements IInstallSubStep {
		@Override
		public String getDescription() {
			return "Log in to AWS";
		}

		@Override
		public void execute() throws ExecException {
			aws.login();
		}
	}
	
	private class FindOrCreateVPCSubStep implements IInstallSubStep {
		@Override
		public String getDescription() {
			return "Find or create cloudcoder PC (virtual private cloud)";
		}

		@Override
		public void execute() throws ExecException {
			aws.findOrCreateVpc();
		}
	}
	
	private class LoadOrCreateKeypairSubStep implements IInstallSubStep {
		@Override
		public String getDescription() {
			Document document = aws.getDocument();
			return document.getValue("awsKeypair.useExisting").getBoolean()
					? "Loading keypair"
					: "Creating new keypair";
		}
		
		@Override
		public void execute() throws ExecException {
			aws.createOrChooseKeypair();
		}
	}
	
	private class FindOrCreateSecurityGroup implements IInstallSubStep {
		@Override
		public String getDescription() {
			return "Finding or creating a security group";
		}
		
		@Override
		public void execute() throws ExecException {
			aws.findOrCreateSecurityGroup();
		}
	}
	
	private class FindUbuntuServerImageSubStep implements IInstallSubStep {
		@Override
		public String getDescription() {
			return "Find Ubuntu server image";
		}
		
		@Override
		public void execute() throws ExecException {
			aws.findUbuntuServerImage();
		}
	}
	
	private class CreateWebappElasticIPSubStep implements IInstallSubStep {
		@Override
		public String getDescription() {
			return "Create an elastic IP address for the webapp instance";
		}
		
		@Override
		public void execute() throws ExecException {
			aws.createWebappElasticIp();
		}
	}
	
	private class WaitForWebappInstanceToStartSubStep implements IInstallSubStep {
		@Override
		public String getDescription() {
			return "Waiting for webapp instance to start";
		}
		
		@Override
		public void execute() throws ExecException {
			aws.waitForInstanceToStart();
		}
	}
	
	private class AssignPublicIpToWebappInstanceSubStep implements IInstallSubStep {
		@Override
		public String getDescription() {
			return "Assign elastic IP address to webapp instance";
		}
		
		@Override
		public void execute() throws ExecException {
			aws.assignPublicIpToWebapp();
		}
	}
	
	private AWSCloudService aws;
	private AWSInfo info;
	
	public ProvisioningInstallStep(AWSCloudService aws, AWSInfo info) {
		super("provisioningStep");
		this.aws = aws;
		this.info = info;
		addSubSteps(
				new LoginSubStep(),
				new FindOrCreateVPCSubStep(),
				new LoadOrCreateKeypairSubStep(),
				new FindOrCreateSecurityGroup(),
				new FindUbuntuServerImageSubStep(),
				new CreateWebappElasticIPSubStep(),
				new AssignPublicIpToWebappInstanceSubStep()
		);
	}

	@Override
	public String getDescription() {
		return "Provisioning network and server resources";
	}
}
