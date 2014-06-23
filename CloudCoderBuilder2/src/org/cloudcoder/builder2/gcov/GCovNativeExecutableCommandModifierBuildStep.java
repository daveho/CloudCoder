// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.builder2.gcov;

import java.io.File;
import java.util.Properties;

import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.CommandExecutionPreferences;
import org.cloudcoder.builder2.model.CommandLimit;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.NativeExecutable;
import org.cloudcoder.builder2.util.PropertyUtil;

/**
 * Modify the {@link Command}s produced to execute {@link NativeExecutable}s
 * so that they save test coverage data.  Assumes that the native executables
 * were compiled with test coverage enabled (e.g., using
 * {@link GCovCCompilerBuildStep}.  This step is a no-op if gcov is not enabled
 * in cloudcoder.properties.
 * 
 * @author David Hovemeyer
 */
public class GCovNativeExecutableCommandModifierBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		if (!PropertyUtil.isEnabled(config, "cloudcoder.builder2.cprog.gcov")) {
			return;
		}

		Command[] commandList = submission.requireArtifact(this.getClass(), Command[].class);
		
		// For each command, make a directory in which the raw coverage
		// data files can be saved, based on the order of the commands
		// (which in turn is based on the order of the TestCases.)
		for (int i = 0; i < commandList.length; i++) {
			String dirName = String.format("cov%03d", i);
			File testCovDataDir = new File(commandList[i].getDir(), dirName);
			if (!testCovDataDir.mkdirs()) {
				throw new InternalBuilderException("Could not create test coverage data directory " + testCovDataDir.getPath());
			}
			commandList[i].setEnvironmentVariable("GCOV_PREFIX", dirName);
			
			// Setting GCOV_PREFIX_STRIP to a large value ensures that the
			// generated coverage output data is placed directly in the
			// coverage data directory.  (If we don't do this it creates a
			// directory structure based on the absolute path of the executable,
			// which is complicated and annoying.)
			commandList[i].setEnvironmentVariable("GCOV_PREFIX_STRIP", "40");
		}
		
		// Modify the CommandExecutionPreferences to disable sandboxing,
		// and to allow the creation of files
		CommandExecutionPreferences prefs = submission.getArtifact(CommandExecutionPreferences.class);
		if (prefs != null) {
			prefs.setLimit(CommandLimit.ENABLE_SANDBOX, 0);
			prefs.setLimit(CommandLimit.FILE_SIZE_KB, 100000);
		}
	}

}
