package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.Document;

public interface IDynamicDnsUpdater {
	/**
	 * Get an update URL to update the IP address associated with
	 * the dyanamic DNS hostname.
	 * 
	 * @param document      the {@link Document} (which has properties needed to
	 *                      authorize the update, e.g., the Duck DNS token)
	 * @param publicIpAddr  the public IP address to associate with the
	 *                      dynamic DNS hostname
	 * @return the update URL (should be issued using an HTTP GET)
	 */
	public String getUpdateUrl(Document document, String publicIpAddr);
	
	/**
	 * Check the result of the update.
	 * 
	 * @param resultText the text (output of the curl command to do the update)
	 * @return true if the update was reported as successful, false otherwise
	 */
	public boolean checkResult(String resultText);
}
