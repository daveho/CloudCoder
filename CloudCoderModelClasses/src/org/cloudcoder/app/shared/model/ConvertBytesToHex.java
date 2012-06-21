// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.shared.model;

/**
 * Convert bytes to a string of hex digits.
 * 
 * @author David Hovemeyer
 */
public class ConvertBytesToHex {
	private static final String HEX = "0123456789abcdef";
	
	private byte[] data;
	
	/**
	 * Constructor.
	 * 
	 * @param data the bytes to convert
	 */
	public ConvertBytesToHex(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Convert the data to a hex string.
	 * 
	 * @return the hex string
	 */
	public String convert() {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			buf.append(HEX.charAt((b >>> 4) & 0xF));
			buf.append(HEX.charAt(b & 0xF));
		}
		return buf.toString();
	}

}
