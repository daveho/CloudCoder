package org.cloudcoder.builder2.model;

import java.io.File;

/**
 * Implementation of {@link IExecutable} representing a native executable
 * (for example, the result of compiling a C/C++ program.)
 * 
 * @author David Hovemeyer
 */
public class NativeExecutable implements IExecutable {
	private final File dir;
	private final String exeFileName;
	
	/**
	 * Constructor.
	 * 
	 * @param dir          directory containing the native executable
	 * @param exeFileName  the unqualified filename of the native executable
	 */
	public NativeExecutable(File dir, String exeFileName) {
		this.dir = dir;
		this.exeFileName = exeFileName;
	}
	
	public File getDir() {
		return dir;
	}
	
	public String getExeFileName() {
		return exeFileName;
	}
}
