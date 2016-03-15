package org.cloudcoder.app.wizard.exec;

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
			if (!InstallationConstants.PRIVATE_KEY_FILE.exists()) {
				throw new ExecException("Could not file private key file " + InstallationConstants.PRIVATE_KEY_FILE.getAbsolutePath());
			}
			
			// There is really nothing else we need to do at this point:
			// Bootstrap will load the keypair from its well-known
			// location.
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
