package org.cloudcoder.webserver;

import org.cloudcoder.daemon.DaemonController;
import org.cloudcoder.daemon.IDaemon;

/**
 * Implementation of {@link DaemonController} for the CloudCoder webapp.
 * 
 * @author David Hovemeyer
 */
public class CloudCoderDaemonController extends DaemonController {
	@Override
	public String getDefaultInstanceName() {
		return "instance";
	}

	@Override
	public Class<? extends IDaemon> getDaemonClass() {
		return CloudCoderDaemon.class;
	}
	
	@Override
	protected Options createOptions() {
		// Put the stdout log in the "logs" directory.
		return new Options() {
			@Override
			public String getStdoutLogFileName() {
				return "logs/stdout.log";
			}
		};
	}
}
