package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class Util implements InstallationConstants {
	public static final int MAX_BACKUPS = 200;

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// This should not happen
			System.err.println("Interrupted while sleeping");
		}
	}

	/**
	 * Ensure that the installation data directory exists.
	 */
	public static void createDataDir() {
		DATA_DIR.mkdirs();
		if (!DATA_DIR.isDirectory()) {
			throw new IllegalStateException("Could not create " + DATA_DIR.getAbsolutePath());
		}
	}
	
	/**
	 * Create backup file of given file.
	 * 
	 * @param f a file about to be overwritten
	 */
	public static void createBackupFile(File f) throws IOException {
		if (!f.exists()) {
			// File doesn't exist yet
			return;
		}
		
		// Find an unused backup filename
		File backup = null;
		for (int i = 1; i <= MAX_BACKUPS; i++) {
			File candidate = new File(String.format("%s.%03d", f.getAbsolutePath(), i));
			if (!candidate.exists()) {
				backup = candidate;
				break;
			}
		}
		if (backup == null) {
			throw new IOException("Too many existing backup files for " + f.getAbsolutePath());
		}
		
		copyFile(f, backup);
		
		System.out.printf("Backed up %s to %s\n", f.getAbsolutePath(), backup.getAbsolutePath());
	}

	public static void copyFile(File src, File dest) throws IOException {
		// Copy original file to backup file
		try (FileOutputStream fos = new FileOutputStream(dest);
				FileInputStream fis = new FileInputStream(src)) {
			IOUtils.copy(fis, fos);
		}
	}
}
