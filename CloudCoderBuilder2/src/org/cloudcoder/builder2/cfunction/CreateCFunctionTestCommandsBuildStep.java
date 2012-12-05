package org.cloudcoder.builder2.cfunction;

import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.CommandInput;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
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
	public void execute(BuilderSubmission submission) {
		TestCase[] testCaseList = submission.getArtifact(TestCase[].class);
		if (testCaseList == null) {
			throw new InternalBuilderException(this.getClass(), "No TestCase list");
		}
		
		NativeExecutable nativeExe = submission.getArtifact(NativeExecutable.class);
		if (nativeExe == null) {
			throw new InternalBuilderException(this.getClass(), "No NativeExecutable");
		}
		
		SecretSuccessAndFailureCodes codes = submission.getArtifact(SecretSuccessAndFailureCodes.class);
		if (codes == null) {
			throw new InternalBuilderException(this.getClass(), "No SecretSuccessAndFailureCodes");
		}
		
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
