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

import java.util.HashMap;
import java.util.Map;

import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Bytecode;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.model.LoadedClasses;
import org.cloudcoder.builder2.util.SubmissionResultUtil;

import sun.org.mozilla.classfile.ByteCode;

/**
 * Use a class loader to load all of the {@link ByteCode}
 * objects produced by an earlier {@link JavaCompilerBuildStep}.
 * Produces a {@link LoadedClasses} object as a result artifact.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class LoadClassesBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission) {
		Bytecode[] bytecodeList = submission.getArtifact(Bytecode[].class);
		if (bytecodeList == null) {
			throw new InternalBuilderException(this.getClass(), "No Bytecode list");
		}

		Map<String, byte[]> classes = new HashMap<String, byte[]>();
		for (Bytecode bytecode : bytecodeList) {
			classes.put(bytecode.getClassName(), bytecode.getCode());
		}

		ByteArrayClassLoader classLoader = new ByteArrayClassLoader(classes);

		// Load the classes.  We'll get a ClassNotFoundException if there were
		// any unresolved references in the compiled classes.
		for (Bytecode bytecode : bytecodeList) {
			try {
				String className = bytecode.getClassName();
				classLoader.loadClass(className);
			} catch (ClassNotFoundException e) {
				// Fatal error: can't load a class needed to execute tests on the submission
				SubmissionResult result = SubmissionResultUtil.createSubmissionResultForUnexpectedBuildError(
						"Could not load compiled classes: " + e.getMessage());
				submission.addArtifact(result);
				return;
			}
		}
		
		// Success: add the LoadedClasses artifact
		LoadedClasses loadedClasses = new LoadedClasses(classLoader);
		submission.addArtifact(loadedClasses);

	}

}
