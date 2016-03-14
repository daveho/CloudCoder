package org.cloudcoder.app.wizard.exec;

import java.io.File;

public interface InstallationConstants {
	/**
	 * All installation data is placed in this directory.
	 */
	public static final File DATA_DIR = new File(System.getProperty("user.home"), "ccinstall");
	
	/**
	 * This is the private key used to communicate with the
	 * webapp instance.  This might be a saved generated key,
	 * or it could be a copy of an existing key.
	 */
	public static final File PRIVATE_KEY_FILE = new File(DATA_DIR, "cloudoder-keypair.pem");
}
