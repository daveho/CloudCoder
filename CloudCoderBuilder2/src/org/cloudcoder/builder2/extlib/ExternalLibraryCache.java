// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.extlib;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudcoder.app.shared.model.ConvertBytesToHex;
import org.cloudcoder.builder2.model.ExternalLibrary;
import org.cloudcoder.builder2.util.DeleteDirectoryRecursively;
import org.cloudcoder.builder2.util.FileUtil;
import org.cloudcoder.daemon.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class to manage external libraries.
 * 
 * @author David Hovemeyer
 */
public class ExternalLibraryCache {
	private static final Logger logger = LoggerFactory.getLogger(ExternalLibraryCache.class);
	
	private static ExternalLibraryCache theInstance = new ExternalLibraryCache();
	
	/**
	 * Get the singleton instance.
	 * 
	 * @return the singleton instance
	 */
	public static ExternalLibraryCache getInstance() {
		return theInstance;
	}
	
	/**
	 * Key identifying an external library: specifies both the URL
	 * and the MD5 checksum of the library.
	 */
	private static class Key {
		public String value;
		
		public Key(String url, String md5) {
			value = url + ":" + md5;
		}
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof Key)) {
				return false;
			}
			return this.value.equals(((Key)obj).value);
		}
	}
	
	/**
	 * Cache entry: contains an {@link ExternalLibrary} object,
	 * allowing it to be loaded if necessary.
	 */
	private class Entry {
		private String libUrl;
		private String libMd5;
		private Object entryLock = new Object();
		private boolean dlStarted;
		private ExternalLibrary obj;
		
		public Entry(String libUrl, String libMd5) {
			this.libUrl = libUrl;
			this.libMd5 = libMd5;
		}
		
		public ExternalLibrary get() throws InterruptedException {
			synchronized (entryLock) {
				// If ExternalLibrary object is available, return it
				if (obj != null) {
					return obj;
				}
				
				// If download has been initiated by another thread, wait for it to complete
				if (dlStarted) {
					while (obj == null) {
						entryLock.wait();
					}
					return obj;
				}
				
				// Initiate the download
				dlStarted = true;
			}
			
			// Attempt to download the library
			ExternalLibrary extLib = attemptDownload(libUrl, libMd5);
			
			// Notify other threads that the ExternalLibrary object is available
			synchronized (entryLock) {
				obj = extLib;
				entryLock.notifyAll();
			}
			
			return obj;
		}
	}
	
	private File tmpDir;
	private Object lock;
	private Map<Key, Entry> entryMap; 
	
	private ExternalLibraryCache() {
		tmpDir = FileUtil.makeTempDir();
		lock = new Object();
		entryMap = new HashMap<Key, Entry>();
	}
	
	/**
	 * Get the {@link ExternalLibrary} object for given URL/MD5.
	 * 
	 * @param url   the URL of the library
	 * @param md5   the MD5 checksum of the library
	 * @return      the {@link ExternalLibrary} object
	 * @throws InterruptedException
	 */
	public ExternalLibrary get(String url, String md5) throws InterruptedException {
		Key key = new Key(url, md5);
		
		// Get or create cache Entry
		Entry entry;
		
		synchronized (lock) {
			entry = entryMap.get(key);
			if (entry == null) {
				entry = new Entry(url, md5);
				entryMap.put(key, entry);
			}
		}
		
		return entry.get();
	}
	
	/**
	 * Clean up any files or directories being used by
	 * the cache.
	 */
	public void cleanup() {
		new DeleteDirectoryRecursively(tmpDir).delete();
	}
	
	private static final Pattern VALID_MD5 = Pattern.compile("^[A-Fa-f0-9]*$"); 
	private static final Random rng = new Random();
	
	private ExternalLibrary attemptDownload(String libUrl, String libMd5) {
		logger.info("Attempting download of library {}, md5={}", libUrl, libMd5);
		
		ExternalLibrary result;
		
		try {
			String fileName = FileUtil.getFileNameFromURL(libUrl);
			
			// Make sure the MD5 checksum is valid.
			// If it contains invalid characters (especially '/' and '.')
			// it could potentially cause the library to overwrite
			// another file in the system.
			Matcher m = VALID_MD5.matcher(libMd5);
			if (!m.matches()) {
				throw new IllegalArgumentException("Invalid MD5: " + libMd5);
			}
			
			// Create a directory to store the external library file.
			// Directory name will be of the form <<random long as hex>>-<<md5 hash>>-<<file name>>.
			// In theory we could get a collision, but in practice this is
			// extremely unlikely.
			File libDir = new File(tmpDir, "" + String.format("%x", rng.nextLong()) + "-" + libMd5 + "-" + fileName);
			if (!libDir.mkdir()) {
				throw new IOException("Could not create directory " + libDir.getAbsolutePath());
			}
			
			// Filename where library will be stored
			File libFile = new File(libDir, fileName);

			// Download the library and compute its MD5 checksum
			URL url = new URL(libUrl);
			InputStream is = null;
			OutputStream os = null;
			MessageDigest md = MessageDigest.getInstance("MD5");
			try {
				is = new DigestInputStream(url.openStream(), md);
				os = new BufferedOutputStream(new FileOutputStream(libFile));
				IOUtil.copy(is, os);
			} finally {
				IOUtil.closeQuietly(is);
				IOUtil.closeQuietly(os);
			}
			
			// Make sure the MD5 checksums match
			String downloadMd5Hash = new ConvertBytesToHex(md.digest()).convert();
			if (!libMd5.toLowerCase().equals(downloadMd5Hash.toLowerCase())) {
				throw new IOException("For URL " + libUrl + ": download md5=" + downloadMd5Hash + " does not match specified md5=" + libMd5);
			}
			
			// Success!
			result = new ExternalLibrary(true, libUrl, libMd5, libFile.getAbsolutePath());
			logger.info("Successful download of {}", libUrl);
		} catch (Exception e) {
			logger.error("Could not download external library " + libUrl, e);
			result = new ExternalLibrary(false, libUrl, libMd5, "<<nonexistent>>");
		}
		
		return result;
	}
}
