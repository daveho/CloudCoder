package org.cloudcoder.builder2.commandrunner;

import java.util.Properties;

import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.CommandExecutionPreferences;
import org.cloudcoder.builder2.model.CommandLimit;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.WrapperMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a {@link CommandExecutionPreferences} object to sandbox
 * executed commands.  This is useful for sandboxing commands
 * to run C and C++ test executables.
 * 
 * @author David Hovemeyer
 */
public class CreateLimitedCommandExecutionPreferencesBuildStep implements IBuildStep {
	private static Logger logger = LoggerFactory.getLogger(CreateLimitedCommandExecutionPreferencesBuildStep.class);

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		CommandExecutionPreferences prefs = new CommandExecutionPreferences();
		
		// Configure process resource limits and output limits
		prefs.setLimit(CommandLimit.CPU_TIME_SEC, 5);
		prefs.setLimit(CommandLimit.FILE_SIZE_KB, 0);
		prefs.setLimit(CommandLimit.OUTPUT_LINE_MAX_CHARS, 400);
		prefs.setLimit(CommandLimit.OUTPUT_MAX_BYTES, 4000);
		prefs.setLimit(CommandLimit.OUTPUT_MAX_LINES, 40);
		prefs.setLimit(CommandLimit.PROCESSES, 0);
		prefs.setLimit(CommandLimit.STACK_SIZE_KB, 256);
		prefs.setLimit(CommandLimit.VM_SIZE_KB, 32768);
		
		// Use the native exe processs wrapper rather than the bash script
		prefs.setWrapperMode(WrapperMode.NATIVE_EXE);
		
		// Configure EasySandbox.
		// Note that the default is not to enable it: however, we expect
		// that almost all users will enable it explicitly when they
		// configure CloudCoder.
		if (Boolean.valueOf(config.getProperty("cloudcoder.submitsvc.oop.easysandbox.enable", "false"))) {
			// Enable EasySandbox
			prefs.setLimit(CommandLimit.ENABLE_SANDBOX, 1);
			prefs.setLimit(CommandLimit.SANDBOX_HEAP_SIZE_BYTES,
					Integer.parseInt(config.getProperty("cloudcoder.submitsvc.oop.easysandbox.heapsize", "8388608")));
		} else {
			// Disable EasySandbox
			prefs.setLimit(CommandLimit.ENABLE_SANDBOX, 0);
			logger.warn(
					"Warning: cloudcoder.submitsvc.oop.easysandbox.enable={}",
					config.getProperty("cloudcoder.submitsvc.oop.easysandbox.enable", "false"));
		}
		
		submission.addArtifact(prefs);
	}

}
