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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.builder2.model.BuilderSubmission;
import org.cloudcoder.builder2.model.Bytecode;
import org.cloudcoder.builder2.model.BytecodeExecutable;
import org.cloudcoder.builder2.model.DeleteDirectoryCleanupAction;
import org.cloudcoder.builder2.model.IBuildStep;
import org.cloudcoder.builder2.model.InternalBuilderException;
import org.cloudcoder.builder2.util.FileUtil;
import org.cloudcoder.builder2.util.SubmissionResultUtil;

/**
 * Convert array of {@link Bytecode} objects into a {@link BytecodeExecutable}
 * artifact which describes the directory the resulting class files are
 * in and the names of the individual class files.  The name of the
 * main class is taken from the first {@link FindJavaPackageAndClassNames}
 * object found in the array of {@link FindJavaPackageAndClassNames} objects.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class BytecodeToBytecodeExecutableBuildStep implements IBuildStep {

	@Override
	public void execute(BuilderSubmission submission, Properties config) {
		Bytecode[] bytecodeList = submission.getArtifact(Bytecode[].class);
		if (bytecodeList == null) {
			throw new InternalBuilderException(this.getClass(), "No Bytecode list");
		}
		
		FindJavaPackageAndClassNames[] packageAndClassNamesList = submission.getArtifact(FindJavaPackageAndClassNames[].class);
		if (packageAndClassNamesList == null) {
			throw new InternalBuilderException(this.getClass(), "No FindJavaPackageAndClassNames list");
		}
		
		// Create temporary directory
		File tempDir = FileUtil.makeTempDir(config);
		if (tempDir == null) {
			// Couldn't create temp dir
			submission.addArtifact(SubmissionResultUtil.createSubmissionResultForUnexpectedBuildError(
					"Could not create temp directory for compilation"));
			return;
		}
		submission.addCleanupAction(new DeleteDirectoryCleanupAction(tempDir));

		// Write class files into temporary directory
		List<String> fileNameList = new ArrayList<String>();
		for (Bytecode bytecode : bytecodeList) {
			String clsName = bytecode.getClassName();
			byte[] bytes = bytecode.getCode();
			
			String fileName = clsName.replace('.', '/') + ".class";
			fileNameList.add(fileName);
			File out = new File(tempDir, fileName);
			out.getParentFile().mkdirs();
			
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(out);
				IOUtils.copy(new ByteArrayInputStream(bytes), os);
			} catch (IOException e) {
				SubmissionResult result = SubmissionResultUtil.createSubmissionResultForUnexpectedBuildError(
						"Error writing Java class file: " + e.getMessage());
				submission.addArtifact(result);
				return;
			} finally {
				IOUtils.closeQuietly(os);
			}
		}
		
		// Create BytecodeExecutable artifact
		BytecodeExecutable bytecodeExe = new BytecodeExecutable(tempDir, fileNameList);
		bytecodeExe.setMainClass(packageAndClassNamesList[0].getFullyQualifiedClassName());
		submission.addArtifact(bytecodeExe);
	}

}
