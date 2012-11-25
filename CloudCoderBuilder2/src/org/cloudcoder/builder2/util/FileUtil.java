package org.cloudcoder.builder2.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	public static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	/**
	 * Make a temporary directory as a subdirectory of the standard system
	 * temp directory.
	 *  
	 * @return the {@link File} naming the temp directory
	 */
	public static File makeTempDir() {
		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		return FileUtil.makeTempDir(sysTempDir.getAbsolutePath());
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

}
