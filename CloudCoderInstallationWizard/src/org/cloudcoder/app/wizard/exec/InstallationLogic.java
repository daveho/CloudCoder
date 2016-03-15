package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.io.output.FileWriterWithEncoding;
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
				executeAll();
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
	
	/**
	 * Synchronously execute all installation steps/sub-steps
	 * until either the installation finishes or a fatal exception
	 * occurs.
	 */
	private void executeAll() {
		while (!progress.isFinished() && !progress.isFatalException()) {
			progress.forceUpdate(); // Allow UI to update itself
			IInstallStep<InfoType, ServiceType> step = progress.getCurrentStep();
			IInstallSubStep<InfoType, ServiceType> subStep = progress.getCurrentSubStep();
			
			// See if this sub-step is dependent on the successful completion
			// of a previous sub-step.
			if (step.isDependent(subStep)) {
				// Find the prerequisite
				String prerequisiteSubstep = step.getPrerequisiteSubStepName(subStep);
				IInstallSubStep<InfoType, ServiceType> prereq = progress.getSubStep(prerequisiteSubstep);
				
				// See if the prerequisite succeeded
				if (!progress.subStepSucceeded(prerequisiteSubstep)) {
					// The prerequisite failed, so this step fails as well.
					// However, we consider this a non-fatal error.
					System.out.printf(
							"Sub-step %s cannot execute because prerequisite %s failed\n",
							subStep.getClass().getSimpleName(),
							prereq.getClass().getSimpleName()
							);
					progress.subStepFinished(false);
					continue;
				}
			}
			
			// Execute the sub-step.
			String subStepClassName = subStep.getClass().getSimpleName();
			try {
				System.out.println("Executing installation sub-step " + subStepClassName);
				subStep.execute(cloudService);
				System.out.println("Sub-step " + subStepClassName + " completed successfully");
				progress.subStepFinished(true);
			} catch (NonFatalExecException e) {
				progress.addNonFatalException(e);
				System.err.println("Sub-step " +
						subStepClassName + " failed with non-fatal exception: " +
						e.getMessage());
				e.printStackTrace(System.err);
				progress.subStepFinished(false);
			} catch (ExecException e) {
				System.err.println("Fatal exception occurred executing sub-step " + subStepClassName);
				e.printStackTrace();
				progress.setFatalException(e);
			} catch (Throwable e) {
				System.err.println("Internal error executing sub-step " + subStepClassName);
				e.printStackTrace();
				progress.setFatalException(e);
			}
		}
		
		// If we finished successfully, generate the report from
		// the report template.
		if (progress.isFinished()) {
			generateReport();
		}
		
		// If the loop terminated, then either the installation finished
		// successfully, or there was a fatal exception.  Let the UI know.
		progress.forceUpdate();
	}

	private void generateReport() {
		// Determine which installation task we were executing
		InstallationTask installTask =
				cloudService.getDocument().getValue("selectTask.installationTask").getEnum(InstallationTask.class);

		// Set values that will be needed to generate the final
		// installation report.
		String stepName = installTask.getStepName();
		setReportValue("db.dnsHostnameConfigured", progress.subStepSucceeded(stepName + ".verifyHostname"));
		setReportValue("db.sslCertInstalled", progress.subStepSucceeded(stepName + ".letsencrypt"));

		// Get the appropriate template, depending on the installation task
		String finishedPageName = "finished" + installTask.getPageSuffix();
		ImmutableStringValue template =
				ImmutableStringValue.createHelpText(finishedPageName, "reporttemplate", "Report template");
		
		// Generate the report
		ProcessTemplate pt = new ProcessTemplate(template, cloudService.getDocument(), cloudService.getInfo());
		String report = pt.generate();
		ImmutableStringValue msg = new ImmutableStringValue("msg", "Message", report);
		
		// Add it to the appropriate "finished" page (replacing the previous dummy text)
		cloudService.getDocument().replaceValue(finishedPageName + ".msg", msg);
		
		// Also save it to a file
		try (Writer fw = new FileWriterWithEncoding(
				new File(InstallationConstants.DATA_DIR, "report.html"), Charset.forName("UTF-8"))) {
			fw.write(msg.getString());
		} catch (IOException e) {
			System.err.println("Could not write report: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	private void setReportValue(String valueName, boolean value) {
		cloudService.getDocument().getValue(valueName).setBoolean(value);
	}
}
