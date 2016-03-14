package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.ImmutableStringValue;
import org.cloudcoder.app.wizard.model.InstallationTask;
import org.cloudcoder.app.wizard.model.Page;

public class InstallationLogic<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>> {
	private Document document;
	private InstallationProgress<InfoType, ServiceType> progress;
	private ServiceType cloudService;
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	public void setProgress(InstallationProgress<InfoType, ServiceType> progress) {
		this.progress = progress;
	}
	
	public void setCloudService(ServiceType cloudService) {
		this.cloudService = cloudService;
	}
	
	public void startInstallation() {
		cloudService.setDocument(document);
		
		InstallationTask installationTask = document.getValue("selectTask.installationTask").getEnum(InstallationTask.class);
		if (installationTask == InstallationTask.INSTALL_CLOUDCODER) {
			// If doing a full install, save Document in a properties file
			saveConfiguration();
		}
		
		// Add installation steps as appropriate for selected InstallationTask
		InstallationTask selectedTask = document.getValue("selectTask.installationTask").getEnum(InstallationTask.class);
		switch (selectedTask) {
		case INSTALL_CLOUDCODER:
			// Full CloudCoder install
			cloudService.addInstallSteps(progress);
			progress.addInstallStep(new BootstrapStep<InfoType, ServiceType>(cloudService));
			break;
		case ISSUE_AND_INSTALL_SSL_CERTIFICATE:
			// Just issue/install Let's Encrypt SSL certificate
			progress.addInstallStep(new InstallSslCertificateStep<InfoType, ServiceType>(cloudService));
			break;
		default:
			throw new IllegalStateException("Unknown installation task: " + selectedTask);
		}
		
		// Start a thread to run the installation.
		// We will create it as a daemon thread, trusting that
		// it will eventually reach a state where the UI will know
		// to continue (either because the installation succeeded
		// or because a fatal exception occurred.)
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				progress.executeAll(cloudService);
			}
		});
		t.setDaemon(true);
		t.start();
	}

	private void saveConfiguration() {
		try {
			try (PrintWriter w = new PrintWriter(new FileWriter(new File(InstallationConstants.DATA_DIR, "ccinstall.properties")))) {
				w.println("# CloudCoder installation wizard saved configuration properties");
				for (int i = 0; i < document.getNumPages(); i++) {
					Page page = document.get(i);
					for (IValue value : page) {
						String propName = page.getPageName() + "." + value.getName();
						if (!(value instanceof ImmutableStringValue)) {
							w.printf("%s=%s\n", propName, value.getPropertyValue());
						}
					}
				}
			}
		} catch (IOException e) {
			// Should this be fatal?
			System.err.println("Error saving installer configuration to file");
			e.printStackTrace();
		}
	}
}
