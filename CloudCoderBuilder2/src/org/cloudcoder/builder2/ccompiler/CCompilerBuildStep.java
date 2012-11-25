// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.ccompiler;

import java.io.File;

import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.DeleteDirectoryCleanupAction;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.NativeExecutable;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.FileUtil;

/**
 * An {@link IBuildStep} that compiles a C/C++ program to produce a
 * {@link NativeExecutable} artifact.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class CCompilerBuildStep implements IBuildStep {
	private static final String DEFAULT_PROG_NAME = "prog";

	@Override
	public void execute(BuilderSubmission submission) {
		ProgramSource programSource = submission.getArtifact(ProgramSource.class);
		if (programSource == null) {
			throw new InternalBuilderException(CCompilerBuildStep.class.getSimpleName() +
					": No program source to compile");
		}
		
		File tempDir = FileUtil.makeTempDir();
		if (tempDir == null) {
			// Couldn't create temp dir
			submission.addArtifact(CUtil.createSubmissionResultForUnexpectedBuildError(
					"Could not create temp directory for compilation"));
			return;
		}
		submission.addCleanupAction(new DeleteDirectoryCleanupAction(tempDir));
		
		Compiler compiler = new Compiler(programSource.getProgramText(), tempDir, DEFAULT_PROG_NAME);
		compiler.setCompilerExe("g++"); // FIXME: should make this configurable
		if (!compiler.compile()) {
			// Compilation failed
			submission.addArtifact(CUtil.createSubmissionResultFromFailedCompile(compiler, 0, 0));
		} else {
			// Compilation succeeded
			
			// Annotate with compiler diagnostics
			submission.addArtifact(compiler.getCompilerDiagnosticList());
			
			// Annotate with NativeExecutable
			submission.addArtifact(new NativeExecutable(tempDir, DEFAULT_PROG_NAME));
		}
	}
}
