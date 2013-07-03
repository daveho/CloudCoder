package org.cloudcoder.builder2.csandbox;

import java.io.IOException;

import org.cloudcoder.daemon.Util;

public class EasySandboxSharedLibrary {
	private String sharedLibraryPath;
	
	public void build() throws IOException {
		// Externalize the EasySandbox source files
		String sourceFile1 = externalizeSourceFile("EasySandbox.c");
		String sourceFile2 = externalizeSourceFile("malloc.c");
		
		// TODO: compile it
		//Compiler compiler = new Compiler
	}

	/**
	 * @param sourceFileName
	 * @return
	 * @throws IOException
	 */
	private String externalizeSourceFile(String sourceFileName)
			throws IOException {
		return Util.getExternalizedFileName(this.getClass().getClassLoader(), "org/cloudcoder/builder2/csandbox/res/" + sourceFileName);
	}
	
	public String getSharedLibraryPath() {
		return sharedLibraryPath;
	}
	
	public void cleanup() {
		
	}
}
