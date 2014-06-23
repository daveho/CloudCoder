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

package org.cloudcoder.builder2.commandrunner;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.builder2.ccompiler.Compiler;
import org.cloudcoder.builder2.ccompiler.Compiler.Module;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Command;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.NativeExecutable;
import org.cloudcoder.builder2.util.PropertyUtil;

/**
 * Build step to collect coverage results and add them
 * as an annotation to the {@link SubmissionResult}.
 * This step is a no-op if gcov is not enabled
 * in cloudcoder.properties.
 * 
 * @author David Hovemeyer
 */
public class GCovCoverageResultsCollectorBuildStep implements IBuildStep {
	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		if (!PropertyUtil.isEnabled(config, "cloudcoder.builder2.cprog.gcov")) {
			return;
		}
		
		Compiler compiler = submission.requireArtifact(this.getClass(), Compiler.class); 
		
		NativeExecutable nativeExe = submission.requireArtifact(this.getClass(), NativeExecutable.class);
		Command[] commandList = submission.requireArtifact(this.getClass(), Command[].class);
		
		File compileDir = nativeExe.getDir();

		// Get the source Module for the compilation (so we know what the source filename is) 
		Module module = compiler.getModules().get(0);

		// Run the gcov command for each command / test case
		for (Command command : commandList) {
			File covDataDir = new File(compileDir, command.getEnv().get("GCOV_PREFIX"));
			
			// gcov is not my favorite program.
			// As of gcc-4.8:
			//   - If the -o option is used to make it look in a directory for
			//     coverage data, the .gcno file must be in that directory,
			//     meaning we have to copy it there
			//   - There is NO way to specify an output file name:
			//     the .gcov output file is created in whatever directory
			//     we run gcov in
			// For these reasons the easiest approach seems to be to copy
			// both the .gcno and source file into the coverage data directory,
			// and then run gcov from that directory.  This will produce a .c.gcov
			// file in that coverage data directory containing the coverage for
			// that specific test case.
			try {
				FileUtils.copyFile(new File(compileDir, module.sourceFileName), new File(covDataDir, module.sourceFileName));
				String gcnoFileName = FilenameUtils.getBaseName(module.sourceFileName) + ".gcno";
				FileUtils.copyFile(new File(compileDir, gcnoFileName), new File(covDataDir, gcnoFileName));
				
				// TODO: run gcov, collect the coverage data
			} catch (IOException e) {
				throw new InternalBuilderException("Error collecting coverage data", e);
			}
		}
	}
}
