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

package org.cloudcoder.builder2.javacompiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Bytecode;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.ProgramSource;
import org.cloudcoder.builder2.util.ArrayUtil;
import org.cloudcoder.builder2.util.SubmissionResultUtil;

/**
 * Build step to compile a Java source file to class file(s).
 * Produces an array of {@link Bytecode} objects as a submission
 * artifact.  Also produces a {@link FindJavaPackageAndClassNames} object
 * that records the name of the package and class name in the
 * source program.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class JavaCompilerBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		ProgramSource programSource = submission.getArtifact(ProgramSource.class);
		if (programSource == null) {
			throw new InternalBuilderException(this.getClass(), "No ProgramSource");
		}

		// Determine the package name and top-level class name,
		// add resulting FindJavaPackageAndClassNames object as submission artifact
		FindJavaPackageAndClassNames packageAndClassNames = new FindJavaPackageAndClassNames();
		packageAndClassNames.determinePackageAndClassNames(programSource.getProgramText());
		if (packageAndClassNames.getClassName() == null) {
			SubmissionResult result = SubmissionResultUtil.createSubmissionResultForUnexpectedBuildError(
					"Could not determine top-level class name");
			submission.addArtifact(result);
			return;
		}
		submission.addArtifact(packageAndClassNames);

		// Attempt to compile the program
		InMemoryJavaCompiler compiler = new InMemoryJavaCompiler();
		compiler.addClassFile(packageAndClassNames.getFullyQualifiedClassName(), programSource.getProgramText());
		if (!compiler.compileWithoutLoadingClasses()) {
			SubmissionResult result = new SubmissionResult(compiler.getCompileResult());
			submission.addArtifact(result);
			return;
		}
		
		// Create Bytecode artifacts for each compiled class
		Map<String, byte[]> compiledClasses = compiler.getFileManager().getClasses();
		List<Bytecode> bytecodeList = new ArrayList<Bytecode>();
		for (Map.Entry<String, byte[]> entry : compiledClasses.entrySet()) {
			String clsName = entry.getKey();
			byte[] bytes = entry.getValue();
			
			Bytecode bytecode = new Bytecode(clsName, bytes);
			bytecodeList.add(bytecode);
		}
		
		// Create array of Bytecode objects, add as submission artifact
		Bytecode[] bytecodeArray = ArrayUtil.toArray(bytecodeList, Bytecode.class);
		submission.addArtifact(bytecodeArray);
	}

}
