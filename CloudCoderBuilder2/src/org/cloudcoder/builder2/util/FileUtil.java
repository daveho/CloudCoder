// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.builder2.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File utility methods.
 *  
 * @author David Hovemeyer
 */
public class FileUtil {
	public static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	/**
	 * Make a temporary directory.
	 * 
	 * @param config configuration properties: this should contain a "cloudcoder.builder2.tmpdir"
	 *               property specifying the directory where temp directories should be
	 *               created
	 * @return a File representing the newly-created temp directory
	 */
	public static File makeTempDir(Properties config) {
		return makeTempDir(config.getProperty("cloudcoder.builder2.tmpdir"));
	}

	/**
	 * Make a temporary directory.
	 * 
	 * @param baseDir  the parent of the temporary directory to create (e.g., "/tmp")
	 * @return a File representing the newly-created temp directory
	 */
	private static File makeTempDir(String baseDir) {
		if (baseDir == null) {
			throw new IllegalArgumentException("makeTempDir called with null baseDir");
		}
		
		int attempts = 1;
		File tempDir = null;
	
		while (tempDir == null && attempts < 10) {
			try {
				// start by creating a temporary file
				File tempFile = File.createTempFile("cmp", "", new File(baseDir));
	
				// temporary file created successfully - delete it and make an identically-named
				// directory
				if (tempFile.delete() && tempFile.mkdir()) {
					// success!
					tempDir = tempFile;
				}
			} catch (IOException e) {
				logger.warn("Unable to delete temp file and create directory");
			}
			attempts++;
		}
		return tempDir;
	}

	/**
	 * Get a filename from a URL. For example, if the URL
	 * is "http://example.com/foo/bar/baz.jar", the filename
	 * is "baz.jar".
	 * 
	 * @param url a URL
	 * @return the filename
	 */
	public static String getFileNameFromURL(String url) {
		int lastSlash = url.lastIndexOf('/');
		return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
	}

}
