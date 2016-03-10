package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.cloudcoder.app.wizard.model.DisplayOption;
import org.cloudcoder.app.wizard.model.ImmutableStringValue;
import org.cloudcoder.app.wizard.model.validators.NoopValidator;

public class InstallationProgress<InfoType extends ICloudInfo, ServiceType extends ICloudService<InfoType, ServiceType>>
		extends Observable {
	private List<IInstallStep<InfoType, ServiceType>> installSteps;
	private int currentStep, currentSubStep;
	private Throwable fatalException;
	private List<IInstallSubStep<InfoType, ServiceType>> succeededSubSteps;
	private List<NonFatalExecException> nonFatalExceptions;
	
	public InstallationProgress() {
		installSteps = new ArrayList<IInstallStep<InfoType, ServiceType>>();
		currentStep = 0;
		currentSubStep = 0;
		succeededSubSteps = new ArrayList<IInstallSubStep<InfoType, ServiceType>>();
		nonFatalExceptions = new ArrayList<NonFatalExecException>();
	}

	public void addInstallStep(IInstallStep<InfoType, ServiceType> step) {
		installSteps.add(step);
	}

	public int getNumSteps() {
		return installSteps.size();
	}

	public int getCurrentStepIndex() {
		return currentStep;
	}

	public int getCurrentSubStepIndex() {
		return currentSubStep;
	}
	
	/**
	 * @return true if the installation has completed successfully, false otherwise
	 */
	public boolean isFinished() {
		return currentStep >= installSteps.size();
	}
	
	/**
	 * @return true if the installation failed due to a fatal exception, false otherwise
	 */
	public boolean isFatalException() {
		return fatalException != null;
	}
	
	/**
	 * Set a fatal exception, terminating the installation abnormally.
	 * 
	 * @param fatalException the fatal exception to set
	 */
	public void setFatalException(Throwable fatalException) {
		this.fatalException = fatalException;
		notifyObservers();
	}
	
	/**
	 * @return the fatal exception that terminated the installation abnormally
	 */
	public Throwable getFatalException() {
		if (fatalException == null) {
			throw new IllegalStateException();
		}
		return fatalException;
	}
	
	/**
	 * @return list of non-fatal exceptions that occurred (if any)
	 */
	public List<NonFatalExecException> getNonFatalExceptions() {
		return Collections.unmodifiableList(nonFatalExceptions);
	}
	
	/**
	 * @return currently-executing {@link IInstallStep}
	 */
	public IInstallStep<InfoType, ServiceType> getCurrentStep() {
		return installSteps.get(currentStep);
	}
	
	/**
	 * @return currently-executing {@link IInstallSubStep}
	 */
	public IInstallSubStep<InfoType, ServiceType> getCurrentSubStep() {
		return getCurrentStep().getInstallSubSteps().get(currentSubStep);
	}
	
	public IInstallStep<InfoType, ServiceType> getStepByName(String stepName) {
		for (IInstallStep<InfoType, ServiceType> step : installSteps) {
			if (step.getName().equals(stepName)) {
				return step;
			}
		}
		throw new IllegalArgumentException("No step with name " + stepName);
	}
	
	public IInstallSubStep<InfoType, ServiceType> getSubStep(String compositeName) {
		int dot = compositeName.indexOf('.');
		if (dot < 0) {
			throw new IllegalArgumentException("Bad composite name: " + compositeName);
		}
		String stepName = compositeName.substring(0, dot);
		String subStepName = compositeName.substring(dot+1);
		IInstallStep<InfoType, ServiceType> step = getStepByName(stepName);
		IInstallSubStep<InfoType, ServiceType> subStep = step.getSubStepByName(subStepName);
		return subStep;
	}
	
	private void subStepFinished(boolean succeeded) {
		if (succeeded) {
			// Current sub-step succeeded, yay
			succeededSubSteps.add(getCurrentSubStep());
		}
		
		// Advance to next sub-step
		currentSubStep++;
		if (currentSubStep >= getCurrentStep().getInstallSubSteps().size()) {
			currentSubStep = 0;
			currentStep++;
		}
	}
	
	/**
	 * Synchronously execute all installation steps/sub-steps
	 * until either the installation finishes or a fatal exception
	 * occurs.
	 */
	public void executeAll(ServiceType cloudService) {
		while (!isFinished() && !isFatalException()) {
			forceUpdate(); // Allow UI to update itself
			IInstallStep<InfoType, ServiceType> step = getCurrentStep();
			IInstallSubStep<InfoType, ServiceType> subStep = getCurrentSubStep();
			
			// See if this sub-step is dependent on the successful completion
			// of a previous sub-step.
			if (step.isDependent(subStep)) {
				// Find the prerequisite
				String prerequisiteSubstep = step.getPrerequisiteSubStepName(subStep);
				IInstallSubStep<InfoType, ServiceType> prereq = getSubStep(prerequisiteSubstep);
				
				// See if the prerequisite succeeded
				if (!succeededSubSteps.contains(prereq)) {
					// The prerequisite failed, so this step fails as well.
					// However, we consider this a non-fatal error.
					System.out.printf(
							"Sub-step %s cannot execute because prerequisite %s failed\n",
							subStep.getClass().getSimpleName(),
							prereq.getClass().getSimpleName()
							);
					subStepFinished(false);
					continue;
				}
			}
			
			// Execute the sub-step.
			try {
				System.out.println("Executing installation sub-step " + subStep.getClass().getSimpleName());
				subStep.execute(cloudService);
				System.out.println("Sub-step " + subStep.getClass().getSimpleName() + " completed successfully");
				subStepFinished(true);
			} catch (NonFatalExecException e) {
				nonFatalExceptions.add(e);
				System.err.println("Sub-step " +
						subStep.getClass().getSimpleName() + " failed with non-fatal exception: " +
						e.getMessage());
				e.printStackTrace(System.err);
				subStepFinished(false);
			} catch (ExecException e) {
				System.err.println("Fatal exception occurred executing sub-step " + subStep.getClass().getSimpleName());
				e.printStackTrace();
				setFatalException(e);
			}
		}
		
		// If we finished successfully, generate the report from
		// the report template.
		if (isFinished()) {
			ImmutableStringValue template = ImmutableStringValue.createHelpText("finished", "reporttemplate", "Report template");
			ProcessTemplate pt = new ProcessTemplate(template, cloudService.getDocument(), cloudService.getInfo());
			String report = pt.generate();
			ImmutableStringValue msg = new ImmutableStringValue("msg", "Message", report);
			
			// Add it to the "finished" page (replacing the previous dummy text)
			cloudService.getDocument().replaceValue("finished.msg", msg);
			
			// Also save it to a file
			try (Writer fw = new FileWriterWithEncoding(
					new File(cloudService.getInfo().getDataDir(), "report.html"), Charset.forName("UTF-8"))) {
				fw.write(msg.getString());
			} catch (IOException e) {
				System.err.println("Could not write report: " + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
		
		// If the loop terminated, then either the installation finished
		// successfully, or there was a fatal exception.  Let the UI know.
		forceUpdate();
	}

	public void forceUpdate() {
		setChanged();
		notifyObservers();
	}

	/**
	 * @return total number of sub-steps over all steps
	 */
	public int getTotalSubSteps() {
		int count = 0;
		for (IInstallStep<InfoType, ServiceType> step : installSteps) {
			count += step.getInstallSubSteps().size();
		}
		return count;
	}

	/**
	 * @return the sub-step index relative to the total number of sub-steps
	 */
	public int getCurrentTotalSubStepIndex() {
		int index = 0;
		for (int i = 0; i < currentStep; i++) {
			index += installSteps.get(i).getInstallSubSteps().size();
		}
		index += currentSubStep;
		return index;
	}
}
