package org.cloudcoder.app.wizard.model;

/**
 * Supported Dynamic DNS providers: these must be in the
 * <a href="https://publicsuffix.org/">Public suffix list</a> to work with Let's Encrypt.
 */
public enum DynamicDnsProvider {
	NONE("Don't use a dynamic DNS hostname", false),
	DUCK_DNS("Duck DNS, https://www.duckdns.org/", false),
	NOIP("No-IP, https://www.noip.com/", true),
	;
	
	private String desc;
	private boolean usernamePasswordAuth;
	
	private DynamicDnsProvider(String desc, boolean usernamePasswordAuth) {
		this.desc = desc;
		this.usernamePasswordAuth = usernamePasswordAuth;
	}
	
	@Override
	public String toString() {
		return desc;
	}
	
	public boolean requireUsernameAndPassword() {
		return usernamePasswordAuth;
	}
}
