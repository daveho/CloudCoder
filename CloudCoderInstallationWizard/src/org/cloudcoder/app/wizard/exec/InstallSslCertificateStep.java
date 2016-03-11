package org.cloudcoder.app.wizard.exec;

import java.io.File;

public class InstallSslCertificateStep<
		InfoType extends ICloudInfo,
		ServiceType extends ICloudService<InfoType, ServiceType>
	> extends AbstractBootstrapStep<InfoType, ServiceType> {
	
	private class UseExistingKeypairSubStep extends AbstractInstallSubStep<InfoType, ServiceType> {

		public UseExistingKeypairSubStep() {
			super("useExistingKeypair");
		}

		@Override
		public String getDescription() {
			return "Loading keypair";
		}

		@Override
		protected void doExecute(ServiceType cloudService) throws ExecException {
			// Use previously chosen/generated keypair file.
			File privateKeyFile = new File(InstallationConstants.DATA_DIR, "cloudcoder-keypair.pem");
			if (!privateKeyFile.exists()) {
				throw new ExecException("Could not file private key file " + privateKeyFile.getAbsolutePath());
			}
			cloudService.getInfo().setPrivateKeyFile(privateKeyFile);
		}
		
	}

	public InstallSslCertificateStep(ServiceType cloudService) {
		super("installSsl", cloudService);
		
		// Use previously chosen/generated private key file
		addSubStep(new UseExistingKeypairSubStep());
		
		// Establish ssh connectivity
		addSubStep(new EstablishSshConnectivitySubStep());
		
		// Verify DNS hostname
		VerifyHostnameSubStep verifyDnsSubStep = new VerifyHostnameSubStep(false);
		addSubStep(verifyDnsSubStep);
		
		// Issue/install Let's Encrypt SSL certificate
		LetsEncryptSubStep letsEncryptSubStep = new LetsEncryptSubStep();
		addSubStep(letsEncryptSubStep);
		setPrerequisite(letsEncryptSubStep, getName() + "." + verifyDnsSubStep.getName());
	}

	@Override
	public String getDescription() {
		return "Issue and install Let's Encrypt SSL certificate";
	}
}
