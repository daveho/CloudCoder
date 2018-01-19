package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.Document;

// Updater for No-IP
public class NoIpUpdater implements IDynamicDnsUpdater {
	@Override
	public String getUpdateCommand(Document document, String publicIpAddr) {
		throw new UnsupportedOperationException("No-IP updater not supported yet");
	}

	@Override
	public boolean checkResult(String resultText) {
		throw new UnsupportedOperationException("No-IP updater not supported yet");
	}
}