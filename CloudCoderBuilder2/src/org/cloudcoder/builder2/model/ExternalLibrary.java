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

package org.cloudcoder.builder2.model;

/**
 * A representation of an external library (such as a jar file needed
 * for a Java problem.)
 * 
 * @author David Hovemeyer
 */
public class ExternalLibrary {
	private final boolean available;
	private final String url;
	private final String md5;
	private final String fileName;
	
	/**
	 * Constructor.
	 * 
	 * @param available true if the external library is available, false if not
	 * @param url       the URL
	 * @param md5       the MD5 checksum
	 * @param fileName  the file name
	 */
	public ExternalLibrary(boolean available, String url, String md5, String fileName) {
		this.available = available;
		this.url = url;
		this.md5 = md5;
		this.fileName = fileName;
	}
	
	/**
	 * Determine whether this external library is available.
	 * It won't be available if, for instance, the builder could
	 * not download it using its specified URL.
	 * 
	 * @return true if the external library is available, false if not
	 */
	public boolean isAvailable() {
		return available;
	}
	
	/**
	 * Get the remote URL of the external library.
	 * 
	 * @return the remote URL of the external library
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Get the MD5 checksum of the external library.
	 * 
	 * @return the MD5 checksum of the external library
	 */
	public String getMd5() {
		return md5;
	}
	
	/**
	 * Get the local filename of the external library.
	 * 
	 * @return the local filename of the external library
	 */
	public String getFileName() {
		return fileName;
	}
}
