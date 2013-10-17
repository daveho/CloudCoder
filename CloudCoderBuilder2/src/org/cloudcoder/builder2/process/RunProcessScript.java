// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2010-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.process;

import java.io.IOException;
import java.util.Properties;

import org.cloudcoder.builder2.util.FileUtil;
import org.cloudcoder.builder2.util.SingletonHolder;
import org.cloudcoder.daemon.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton for accessing the <code>runProcess.sh</code> script.
 * Externalizes the script as a file if necessary (e.g., because we are
 * running from a jar file.)
 * 
 * @author David Hovemeyer
 */
public class RunProcessScript {
	private static Logger logger = LoggerFactory.getLogger(RunProcessScript.class);
	
//	private static final String SCRIPT_FILENAME = "runProcess.sh";
//	private static final String SCRIPT_FILENAME = "runProcess2.sh";
	private static final String SCRIPT_FILENAME = "runProcess3.sh";

	private static final SingletonHolder<String, Properties> holder = new SingletonHolder<String, Properties>() {
		@Override
		protected String onCreate(Properties arg) {
			// "Externalize" the runProcess.sh script.
			// If we're running out of a directory, then we can directly access the file
			// in the classpath.  If we're running out of a jarfile, then this will copy
			// runProcess.sh into a temporary file in the filesystem.
			try {
				String runProcessPath = RunProcessScript.class.getPackage().getName().replace('.', '/') + "/res/" + SCRIPT_FILENAME;
				return Util.getExternalizedFileName(ProcessRunner.class.getClassLoader(), runProcessPath, FileUtil.makeTempDir(arg));
			} catch (IOException e) {
				logger.error("Can't exernalize runProcess.sh", e);
				throw new IllegalStateException("Couldn't get externalized path for " + SCRIPT_FILENAME, e);
			}
		}
	};
	
	/**
	 * Get the instance (path) to the externalized form of <code>runProcess.sh</code>.
	 * 
	 * @param config builder configuration properties
	 * @return the path to <code>runProcess.sh</code>
	 * @throws IllegalStateException if the script can't be externalized
	 */
	public static String getInstance(Properties config) {
		return holder.get(config);
	}
}
