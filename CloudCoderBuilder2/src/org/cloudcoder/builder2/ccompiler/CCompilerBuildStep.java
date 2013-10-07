// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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
import java.util.Properties;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.DeleteDirectoryCleanupAction;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.NativeExecutable;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.FileUtil;
import org.cloudcoder.builder2.util.SubmissionResultUtil;

/**
 * An {@link IBuildStep} that compiles a C/C++ program to produce a
 * {@link NativeExecutable} artifact.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class CCompilerBuildStep implements IBuildStep {
	/**
	 * Default name for resulting executable.
	 * Make it something distinctive so that it shows up clearly
	 * in process listings.
	 */
	private static final String DEFAULT_PROG_NAME = "cctestprog";

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		ProgramSource[] programSourceList = submission.getArtifact(ProgramSource[].class);
		if (programSourceList == null) {
			throw new InternalBuilderException(this.getClass(), "No ProgramSource list");
		}
		
		// For now, we only handle a single source file
		if (programSourceList.length > 1) {
			throw new InternalBuilderException(this.getClass(), "Multiple C source files are not supported yet");
		}
		
		ProgramSource programSource = programSourceList[0];
		
		File tempDir = FileUtil.makeTempDir(config);
		if (tempDir == null) {
			// Couldn't create temp dir
			submission.addArtifact(SubmissionResultUtil.createSubmissionResultForUnexpectedBuildError(
					"Could not create temp directory for compilation"));
			return;
		}
		submission.addCleanupAction(new DeleteDirectoryCleanupAction(tempDir));
		
		Compiler compiler = new Compiler(programSource.getProgramText(), tempDir, DEFAULT_PROG_NAME, config);
		compiler.setCompilerExe("g++"); // FIXME: should make this configurable
		if (!compiler.compile()) {
			// Compilation failed
			submission.addArtifact(CUtil.createSubmissionResultFromFailedCompile(
					compiler,
					programSource.getPrologueLength(),
					programSource.getEpilogueLength()));
		} else {
			// Compilation succeeded
			
			// Annotate with CompilationResult
			CompilationResult compilationResult = new CompilationResult();
			compilationResult.setOutcome(CompilationOutcome.SUCCESS);
			compilationResult.setCompilerDiagnosticList(compiler.getCompilerDiagnosticList());
			compilationResult.adjustDiagnosticLineNumbers(
					programSource.getPrologueLength(),
					programSource.getEpilogueLength());
			submission.addArtifact(compilationResult);
			
			// Annotate with NativeExecutable
			submission.addArtifact(new NativeExecutable(tempDir, DEFAULT_PROG_NAME));
		}
	}
}
