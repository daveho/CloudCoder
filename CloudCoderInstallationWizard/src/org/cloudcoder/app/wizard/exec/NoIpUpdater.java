package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.Document;

// Updater for No-IP
public class NoIpUpdater implements IDynamicDnsUpdater {
	@Override
	public String getUpdateCommand(Document document, String publicIpAddr) {
		String dnsHostname = document.getValue("dns.hostname").getString();
		String username = document.getValue("dynDnsAcct.username").getString();
		String password = document.getValue("dynDnsAcct.password").getString();
		
		String updateCommand =
				"curl -u '" +
				username + ":" + password +
				"' 'http://dynupdate.no-ip.com/nic/update?hostname=" +
				dnsHostname +
				"&myip=" +
				publicIpAddr +
				"'";
		
		return updateCommand;
	}

	@Override
	public boolean checkResult(String resultText) {
		resultText = resultText.trim().toLowerCase();
		return resultText.startsWith("good ") || resultText.startsWith("nochg ");
	}
}
