package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.Document;

// Updater for Duck DNS
public class DuckDnsUpdater implements IDynamicDnsUpdater {
	@Override
	public String getUpdateCommand(Document document, String publicIpAddr) {
		String authToken = document.getValue("duckDns.token").getString();
		String dnsHostname = document.getValue("dns.hostname").getString();
		
		String domain = dnsHostname.substring(0, dnsHostname.length() - ".duckdns.org".length());
		
		String updateUrl =
				"https://www.duckdns.org/update?domains=" + domain + "&token=" + authToken + "&ip=" + publicIpAddr;

		// No HTTP auth is required for DuckDNS
		return "curl --silent --show-error '" + updateUrl + "'";
	}

	@Override
	public boolean checkResult(String resultText) {
		return resultText.trim().equals("OK");
	}
}