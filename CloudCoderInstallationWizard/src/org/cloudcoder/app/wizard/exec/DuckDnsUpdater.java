package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.Document;

// Updater for Duck DNS
public class DuckDnsUpdater implements IDynamicDnsUpdater {
	@Override
	public String getUpdateUrl(Document document, String publicIpAddr) {
		String authToken = document.getValue("dynDns.duckDnsToken").getString();
		String dnsHostname = document.getValue("dns.hostname").getString();
		
		String domain = dnsHostname.substring(0, dnsHostname.length() - ".duckdns.org".length());
		
		String updateUrl =
				"https://www.duckdns.org/update?domains=" + domain + "&token=" + authToken + "&ip=" + publicIpAddr;
		
		return updateUrl;
	}

	@Override
	public boolean checkResult(String resultText) {
		return resultText.trim().equals("OK");
	}
}