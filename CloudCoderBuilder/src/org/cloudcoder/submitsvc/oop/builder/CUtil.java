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

package org.cloudcoder.submitsvc.oop.builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.CompilerDiagnostic;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for C testers.
 * 
 * @author David Hovemeyer
 */
public class CUtil {
	private static final Logger logger = LoggerFactory.getLogger(CUtil.class);

	/** Maximum number of bytes of output native code testers should read from an untrusted test process. */
	public static final int MAX_BYTES_ALLOWED = 20 * 100 * 2;

	/** Maximum number of lines of output native code testers should read from an untrusted test process. */
	public static final int MAX_LINES_ALLOWED = 20;

	/** Maximum number of characters per line native code testers should read from an untrusted test process. */
	public static final int MAX_CHARACTERS_PER_LINE = 200;
	
	public static File makeTempDir() 
	{
	    final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
	    return makeTempDir(sysTempDir.getAbsolutePath());
	}
	
	/**
	 * Make a temporary directory.
	 * 
	 * @param baseDir  the parent of the temporary directory to create (e.g., "/tmp")
	 * @return a File representing the newly-created temp directory
	 */
	public static File makeTempDir(String baseDir) {
	    int attempts = 1;
	    File tempDir = null;
	
	    while (tempDir == null && attempts < 10) {
	        try {
	            // start by creating a temporary file
	            File tempFile = File.createTempFile("cmp", "", new File(baseDir));
	
	            // temporary file created successfully - delete it and make an identically-named
	            // directory
	            if (tempFile.delete() && tempFile.mkdir()) {
	                // success!
	                tempDir = tempFile;
	            }
	        } catch (IOException e) {
	            logger.warn("Unable to delete temp file and create directory");
	        }
	        attempts++;
	    }
	    return tempDir;
	}

	public static SubmissionResult createSubmissionResultForUnexpectedBuildError(String message) {
		// FIXME: would be nice to have a more straightforward way to indicate this kind of issue
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.UNEXPECTED_COMPILER_ERROR);
		CompilerDiagnostic compilerDiagnostic = new CompilerDiagnostic(1, 1, 1, 1, message);
		compilationResult.setCompilerDiagnosticList(new CompilerDiagnostic[]{compilerDiagnostic});
		return new SubmissionResult(compilationResult);
	}

	public static SubmissionResult createSubmissionResultFromFailedCompile(Compiler compiler, int prologueLength, int epilogueLength) {
		CompilerDiagnostic[] compilerDiagnosticList = compiler.getCompilerDiagnosticList();
		CompilationResult compilationResult = new CompilationResult(CompilationOutcome.FAILURE);
		compilationResult.setCompilerDiagnosticList(compilerDiagnosticList);
		compilationResult.adjustDiagnosticLineNumbers(prologueLength, epilogueLength);
		SubmissionResult submissionResult = new SubmissionResult(compilationResult);
		submissionResult.setTestResults(new TestResult[0]);
		return submissionResult;
	}
	
	public static String merge(String[] list) {
		return merge(Arrays.asList(list));
	}

	public static String merge(List<String> list){
		return doMerge(list, "\n");
	}
	
	public static String mergeOneLine(String[] list) {
		return mergeOneLine(Arrays.asList(list));
	}

	public static String mergeOneLine(List<String> list) {
		return doMerge(list, " ");
	}

	private static String doMerge(List<String> list, String sep) {
		StringBuilder builder=new StringBuilder();
	    for (String s : list) {
	        builder.append(s);
	        builder.append(sep);
	    }
	    return builder.toString();
	}

	/**
	 * Create a process runner suitable for running an untrusted test program.
	 * Limits amount of output read to a reasonable level.
	 * Also, sets OS-level resource limits.
	 * 
	 * @return a ProcessRunner
	 */
	public static ProcessRunner createProcessRunner() {
		// Create a LimitedProcessRunner so that some resource limits
		// are enforced by the OS.  (E.g., amount of memory used,
		// don't allow subprocesses to be created, etc.)
		ProcessRunner processRunner = new LimitedProcessRunner() {
			/* (non-Javadoc)
			 * @see org.cloudcoder.submitsvc.oop.builder.ProcessRunner#createOutputCollector(java.io.InputStream)
			 */
			@Override
			protected IOutputCollector createOutputCollector(InputStream inputStream) {
				LimitedOutputCollector collector = new LimitedOutputCollector(inputStream);
				
				collector.setMaxBytesAllowed(MAX_BYTES_ALLOWED);
				collector.setMaxLinesAllowed(MAX_LINES_ALLOWED);
				collector.setMaxCharactersPerLine(MAX_CHARACTERS_PER_LINE);
				
				return collector;
			}
		};
		return processRunner;
	}
}
