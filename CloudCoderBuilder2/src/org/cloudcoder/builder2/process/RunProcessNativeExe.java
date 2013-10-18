// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2010-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.process;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.cloudcoder.builder2.ccompiler.Compiler;
import org.cloudcoder.builder2.model.WrapperMode;
import org.cloudcoder.builder2.util.DeleteDirectoryRecursively;
import org.cloudcoder.builder2.util.FileUtil;
import org.cloudcoder.builder2.util.SingletonHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton to create and manage the native executable process wrapper
 * (cRunProcess.exe).
 * 
 * @author David Hovemeyer
 */
public class RunProcessNativeExe {
	private static final String EXE_NAME = "cRunProcess.exe";

	private static final Logger logger = LoggerFactory.getLogger(RunProcessNativeExe.class);
	
	private static final SingletonHolder<RunProcessNativeExe, Properties> holder = new SingletonHolder<RunProcessNativeExe, Properties>() {
		@Override
		protected RunProcessNativeExe onCreate(Properties arg) {
			return new RunProcessNativeExe(arg);
		}
	};
	
	/**
	 * Get the singleton instance.
	 * 
	 * @param config configuration properties
	 * @return the singleton instance
	 */
	public static RunProcessNativeExe getInstance(Properties config) {
		return holder.get(config);
	}

	private File tempDir;
	private String nativeExePath;
	
	private RunProcessNativeExe(Properties config) {
		try {
			build(config);
		} catch (Exception e) {
			logger.error("Could not compile native exe process wrapper", e);
		}
	}

	private void build(Properties config) throws IOException {
		String source = ProcessUtil.resourceToString("org/cloudcoder/builder2/process/res/cRunProcess.c");
		
		this.tempDir = FileUtil.makeTempDir(config);
		
		Compiler compiler = new Compiler(tempDir, EXE_NAME, config);
		compiler.setWrapperMode(WrapperMode.SCRIPT); // critical: cRunProcess.exe can't be used to help compile itself!
		compiler.addModule("cRunProcess.c", source);
		compiler.setCompilerExe("gcc"); // important: code is not valid C++, can't use g++
		compiler.addFlag("-std=gnu99");
		compiler.addFlag("-D_BSD_SOURCE");

		if (!compiler.compile()) {
			for (String err : compiler.getCompilerOutput()) {
				logger.error("Compile error: {}", err);
			}
			throw new IOException("Error compiling cRunProcess.exe");
		}
		
		this.nativeExePath = tempDir.getAbsolutePath() + "/" + EXE_NAME;
	}
	
	/**
	 * Get the path to the native exe process wrapper.
	 * 
	 * @return the native exe process wrapper, or null if it couldn't be created
	 */
	public String getNativeExePath() {
		return nativeExePath;
	}

	/**
	 * Clean up.
	 */
	public void cleanup() {
		if (tempDir != null) {
			new DeleteDirectoryRecursively(tempDir).delete();
		}
	}
}
