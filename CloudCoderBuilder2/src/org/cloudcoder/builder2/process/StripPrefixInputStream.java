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

package org.cloudcoder.builder2.process;

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that strips a prefix of a specified sequence of
 * bytes from another input stream.  We use this to get rid of the
 * "&lt;&lt;entering SECCOMP mode&gt;&gt;" message printed to
 * stdout and stderr by EasySandbox.
 * 
 * @author David Hovemeyer
 */
public class StripPrefixInputStream extends InputStream {
	private InputStream delegate;
	private int count;
	private byte[] prefix;
	
	public StripPrefixInputStream(InputStream delegate, byte[] prefix) {
		this.delegate = delegate;
		this.count = 0;
		
		// Make a copy of the prefix array
		this.prefix = new byte[prefix.length];
		System.arraycopy(prefix, 0, this.prefix, 0, prefix.length);
	}

	@Override
	public int read() throws IOException {
		// If we've read fewer bytes than the length of the prefix,
		// attempt to read the entire prefix
		while (count < prefix.length) {
			// Read a byte
			int b = delegate.read();
			
			if (b < 0) {
				// End of stream was reached before reading all of the prefix.
				count = prefix.length;
				return b; 
			}
			
			if ((byte)b != prefix[count]) {
				// The byte read did not match the prefix.
				count = prefix.length;
				return b;
			}
			
			count++;
		}
		
		// Prefix has been read/skipped, so just read data normally
		return delegate.read();
	}
	
	// TODO: override other read methods for efficiency

}
