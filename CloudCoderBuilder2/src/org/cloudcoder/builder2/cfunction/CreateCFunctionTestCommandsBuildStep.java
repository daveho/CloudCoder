package org.cloudcoder.builder2.cfunction;

import java.util.Properties;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.CommandInput;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.NativeExecutable;

/**
 * Create {@link Command}s to execute the scaffolded executable
 * for a {@link ProblemType#C_FUNCTION} submission for each {@link TestCase}.
 * Also creates an empty {@link CommandInput} for each {@link TestCase}.
 * Resulting arrays of {@link Command}s and {@link CommandInput}s are
 * added as submission artifacts.
 *  
 * @author David Hovemeyer
 */
public class CreateCFunctionTestCommandsBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		TestCase[] testCaseList = submission.requireArtifact(this.getClass(), TestCase[].class);
		NativeExecutable nativeExe = submission.requireArtifact(this.getClass(), NativeExecutable.class);
		SecretSuccessAndFailureCodes codes = submission.requireArtifact(this.getClass(), SecretSuccessAndFailureCodes.class);
		
		Command[] commandList = new Command[testCaseList.length];
		CommandInput[] commandInputList = new CommandInput[testCaseList.length];
		
		for (int i = 0; i < testCaseList.length; i++) {
			commandList[i] = nativeExe.toCommand(
					testCaseList[i].getTestCaseName(),      // first arg: test case name
					String.valueOf(codes.getSuccessCode()), // second arg: exit code for test success
					String.valueOf(codes.getFailureCode())  // third arg: exit code for test failure
			);
			
			commandInputList[i] = new CommandInput("");
		}
		
		submission.addArtifact(commandList);
		submission.addArtifact(commandInputList);
	}

}
