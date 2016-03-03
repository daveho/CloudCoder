package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.Document;

/**
 * Partial implementation of {@link IInstallSubStep} that handles
 * doing a dry run automatically.  Subclasses should implement
 * {@link #doExecute(ICloudService, ICloudInfo)}.
 * 
 * @author David Hovemeyer
 */
public abstract class AbstractInstallSubStep<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
	implements IInstallSubStep<InfoType, ServiceType> {
	@Override
	public void execute(ServiceType cloudService) throws ExecException {
		Document document = cloudService.getDocument();
		if (document.getValue("welcome.dryRun").getBoolean()) {
			// Doing a dry run
			System.out.println("Doing a dry run for install sub-step " + getClass().getSimpleName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println("Unexpected interruption");
				e.printStackTrace();
			}
		} else {
			// Doing a real run
			doExecute(cloudService);
		}
	}
	
	/**
	 * Subclasses implement this to actually carry out the sub-step.
	 * 
	 * @param cloudService the {@link ICloudService}
	 * @throws ExecException
	 */
	protected abstract void doExecute(ServiceType cloudService) throws ExecException;
}
