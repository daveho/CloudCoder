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

package org.cloudcoder.builder2.csandbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.cloudcoder.builder2.ccompiler.Compiler;
import org.cloudcoder.builder2.util.DeleteDirectoryRecursively;
import org.cloudcoder.builder2.util.FileUtil;
import org.cloudcoder.daemon.IOUtil;

/**
 * Compile the EasySandbox shared library.
 * 
 * @author David Hovemeyer
 */
public class EasySandboxSharedLibrary {
	private File tempDir;
	private String sharedLibraryPath;
	
	/**
	 * Compile the EasySandbox shared library.
	 * 
	 * @throws IOException if an error occurs
	 */
	public void build() throws IOException {
		// Get source code for the EasySandbox source files
		String source1 = sourceResourceToString("EasySandbox.c");
		String source2 = sourceResourceToString("malloc.c");
		
		this.tempDir = FileUtil.makeTempDir();
		
		// Compile the code and link it into a shared library
		Compiler compiler = new Compiler(tempDir, "EasySandbox.so");
		compiler.addModule("EasySandbox.c", source1);
		compiler.addModule("malloc.c", source2);
		compiler.addFlag("-fPIC");
		compiler.addFlag("-shared");
		
		if (!compiler.compile()) {
			throw new IOException("Error compiling EasySandbox shared library");
		}
		
		sharedLibraryPath = tempDir.getAbsolutePath() + "/EasySandbox.so";
	}
	
	/**
	 * Get the path of the shared library.
	 * 
	 * @return the path of the shared library
	 */
	public String getSharedLibraryPath() {
		return sharedLibraryPath;
	}
	
	/**
	 * Clean up.
	 */
	public void cleanup() {
		if (tempDir != null) {
			new DeleteDirectoryRecursively(tempDir).delete();
		}
	}
	
	private String sourceResourceToString(String sourceFileName) throws IOException {
		InputStream in = null;
		try {
			in = this.getClass().getClassLoader().getResourceAsStream("org/cloudcoder/builder2/csandbox/res/" + sourceFileName);
			InputStreamReader r = new InputStreamReader(in);
			StringWriter sw = new StringWriter();
			IOUtil.copy(r, sw);
			return sw.toString();
		} finally {
			IOUtil.closeQuietly(in);
		}
	}
}
