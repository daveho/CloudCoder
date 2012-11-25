package org.cloudcoder.builder2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A Tester executes a series of {@link IBuildStep}s on a
 * {@link BuilderSubmission}.
 * 
 * @author David Hovemeyer
 */
public class Tester {
	private List<IBuildStep> buildStepList;
	
	/**
	 * Constructor.
	 */
	public Tester() {
		buildStepList = new ArrayList<IBuildStep>();
	}
	
	/**
	 * Add an {@link IBuildStep}.
	 * 
	 * @param buildStep the {@link IBuildStep} to add
	 */
	public void addBuildStep(IBuildStep buildStep) {
		buildStepList.add(buildStep);
	}
	
	/**
	 * Execute the Tester on a {@link BuilderSubmission}.
	 * 
	 * @param submission the {@link BuilderSubmission} to build/test
	 */
	public void execute(BuilderSubmission submission) {
		for (IBuildStep buildStep : buildStepList) {
			buildStep.execute(submission);
			if (submission.isComplete()) {
				break;
			}
		}
		
		if (!submission.isComplete()) {
			throw new InternalBuilderException("Executed all build steps but submission is not complete");
		}
	}
}
