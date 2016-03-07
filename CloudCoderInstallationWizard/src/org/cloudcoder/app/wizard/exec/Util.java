package org.cloudcoder.app.wizard.exec;

public class Util implements InstallationConstants {
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
}
