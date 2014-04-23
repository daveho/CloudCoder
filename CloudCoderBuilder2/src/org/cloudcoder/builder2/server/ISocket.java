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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface to abstract a socket connection from the builder to
 * the remote webapp.  Having this be an interface allows us to
 * more easily have multiple ways for the builder to connect to
 * the webapp (e.g., support SSH tunneling, which would be
 * more difficult if we had to extend the actual java.net.Socket class).
 * Exposes only the methods that the builder actually uses.
 * 
 * @author David Hovemeyer
 */
public interface ISocket extends Closeable {
	/**
	 * Get the input stream to read from the socket.
	 * 
	 * @return the input stream
	 * @throws IOException
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Get the output stream to write to the socket.
	 * 
	 * @return the output stream
	 * @throws IOException
	 */
	OutputStream getOutputStream() throws IOException;
}
