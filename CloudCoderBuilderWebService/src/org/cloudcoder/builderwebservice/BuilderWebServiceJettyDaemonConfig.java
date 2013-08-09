package org.cloudcoder.builderwebservice;

import org.cloudcoder.jetty.JettyDaemonConfig;

public interface BuilderWebServiceJettyDaemonConfig extends JettyDaemonConfig {
	public String getContextPath();
}
