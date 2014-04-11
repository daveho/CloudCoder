// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.healthmonitor;

import java.util.Properties;

import org.cloudcoder.daemon.IDaemon;
import org.cloudcoder.daemon.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The health monitor {@link IDaemon} implementation.
 * Creates the {@link HealthMonitor} thread and controls its
 * execution.
 * 
 * @author David Hovemeyer
 */
public class HealthMonitorDaemon implements IDaemon {
	private static final Logger logger = LoggerFactory.getLogger(HealthMonitorDaemon.class);
	
	private HealthMonitorConfig config;
	private HealthMonitor healthMonitor;
	private Thread healthMonitorThread;

	public void setConfig(HealthMonitorConfig config) {
		this.config = config;
	}
	
	@Override
	public void start(String instanceName) {
		logger.info("Starting up");
		
		if (config == null) {
			logger.info("Reading embedded configuration properties...");
			Properties props =
					Util.loadPropertiesFromResource(HealthMonitorDaemon.class.getClassLoader(), "cloudcoder.properties");
			config = new HealthMonitorConfig();
			config.load(props);
			
			logger.info("Monitoring {} instance(s):", config.getWebappInstanceList().size());
			for (String instance : config.getWebappInstanceList()) {
				logger.info("Instance: {}", instance);
			}
		}

		if (config.getWebappInstanceList().isEmpty()) {
			throw new IllegalStateException("Configuration doesn't specify any instances to monitor");
		}
		
		healthMonitor = new HealthMonitor();
		healthMonitor.setConfig(config);
		healthMonitorThread = new Thread(healthMonitor);
		healthMonitorThread.start();
		
		logger.info("Monitor thread started");
	}

	@Override
	public void handleCommand(String command) {
		// No commands are implemented at the moment
		logger.info("Unrecognized command: {}", command);
	}

	@Override
	public void shutdown() {
		if (healthMonitorThread == null || !healthMonitorThread.isAlive()) {
			logger.error("Health monitor is not running");
		} else {
			try {
				healthMonitor.shutdown();
				healthMonitorThread.interrupt();
				
				logger.info("Waiting for monitor thread to finish...");
				healthMonitorThread.join();
				logger.info("Monitor thread has finished");
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for monitor thread to exit");
			}
		}
	}

}
