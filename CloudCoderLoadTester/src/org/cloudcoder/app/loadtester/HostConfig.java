package org.cloudcoder.app.loadtester;

/**
 * Configuration specifying how to connect to the webapp.
 * 
 * @author David Hovemeyer
 */
public class HostConfig {
	private String protocol;
	private String hostname;
	private int port;
	private String contextPath;

	/**
	 * Constructor.
	 * 
	 * @param protocol     the protocol, e.g., "https"
	 * @param hostname     the hostname
	 * @param port         the port: 0 if the default port for the protocol
	 * @param contextPath  the context path (where the webapp.nocache.js is located)
	 */
	public HostConfig(String protocol, String hostname, int port, String contextPath) {
		this.protocol = protocol;
		this.hostname = hostname;
		this.port = port;
		this.contextPath = contextPath;
	}
	
	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}
	
	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @return the port (0 if the default port should be used) 
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * @return the context path
	 */
	public String getContextPath() {
		return contextPath;
	}
}
