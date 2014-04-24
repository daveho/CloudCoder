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
import java.net.Socket;

/**
 * Factory to create a socket connecting to given host and port.
 * The purpose is to abstract the actual transport protocol
 * (plain TCP vs. SSL/TLS).
 * 
 * @author David Hovemeyer
 */
public interface ISocketFactory {
	/**
	 * Create a socket connecting to given host and port.
	 * 
	 * @param host the host
	 * @param port the port
	 * @return the Socket
	 * @throws IOException
	 */
	public Socket createSocket(String host, int port) throws IOException;
}
