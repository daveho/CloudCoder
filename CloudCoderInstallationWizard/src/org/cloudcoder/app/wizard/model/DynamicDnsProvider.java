package org.cloudcoder.app.wizard.model;

import org.cloudcoder.app.wizard.exec.IDynamicDnsUpdater;

/**
 * Supported Dynamic DNS providers: these must be in the
 * <a href="">Public suffix list</a> to work with Let's Encrypt.
 */
public enum DynamicDnsProvider {
	NONE("Don't use a dynamic DNS hostname"),
	DUCK_DNS("Duck DNS, https://www.duckdns.org/"),
	NOIP("No-IP, https://www.noip.com/"),
	;
	
	private String desc;
	
	private DynamicDnsProvider(String desc) {
		this.desc = desc;
	}
	
	@Override
	public String toString() {
		return desc;
	}
}
