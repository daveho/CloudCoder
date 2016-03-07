package org.cloudcoder.app.wizard.exec;

import java.io.File;

public interface InstallationConstants {
	/**
	 * All installation data is placed in this directory.
	 */
	public static final File DATA_DIR = new File(System.getProperty("user.home"), "ccinstall");
}
