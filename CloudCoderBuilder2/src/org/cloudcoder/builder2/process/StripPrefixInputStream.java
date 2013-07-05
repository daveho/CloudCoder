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
