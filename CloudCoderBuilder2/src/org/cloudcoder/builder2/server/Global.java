// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <dhovemey@ycp.edu>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.builder2.server;

import java.util.Properties;

import org.cloudcoder.builder2.csandbox.EasySandboxSharedLibrary;
import org.cloudcoder.builder2.extlib.ExternalLibraryCache;
import org.cloudcoder.builder2.javasandbox.JVMKillableTaskManager;
import org.cloudcoder.builder2.pythonfunction.PythonKillableTaskManager;

/**
 * Global setup and cleanup needed for before builders start and
 * after builders finish.
 * 
 * @author David Hovemeyer
 */
public class Global {
	/**
	 * Set up global resources.
	 * 
	 * @param config configuration properties
	 */
	public static void setup(Properties config) {
		// Install KillableTaskManager's security manager
		JVMKillableTaskManager.installSecurityManager();
		PythonKillableTaskManager.installSecurityManager();
	}
	
	/**
	 * Clean up global resources.
	 * 
	 * @param config configuration properties
	 */
	public static void cleanup(Properties config) {
		// Ensure that if the EasySandbox shared library was built,
		// that its directory is deleted before the daemon exits.
		if (EasySandboxSharedLibrary.isCreated()) {
			EasySandboxSharedLibrary.getInstance(config).cleanup();
		}
		
		// Delete directories/files used by the ExternalLibraryCache
		ExternalLibraryCache.getInstance(config).cleanup();
	}
}
