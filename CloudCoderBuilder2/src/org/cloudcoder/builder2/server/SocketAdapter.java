// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <dhovemey@ycp.edu>
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Implementation of {@link ISocket} that wraps a normal
 * {@link Socket}.
 * 
 * @author David Hovemeyer
 */
public class SocketAdapter implements ISocket {
	private Socket delegate;

	/**
	 * Constructor.
	 * 
	 * @param delegate the {@link Socket} to wrap
	 */
	public SocketAdapter(Socket delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void close() throws IOException {
		delegate.close();
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return delegate.getInputStream();
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		return delegate.getOutputStream();
	}
}
