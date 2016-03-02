package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.Document;

public interface ICloudService {
	public static final String CLOUDCODER_KEYPAIR_NAME = "cloudcoder-keypair";
	
	/**
	 * Get the {@link Document} that stores the user-provided
	 * configuration information.
	 * 
	 * @return the {@link Document}
	 */
	public Document getDocument();
}
