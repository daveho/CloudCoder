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
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLSocketFactory;

import org.cloudcoder.builder2.util.ProcessUtil;
import org.cloudcoder.builder2.util.StringUtil;
import org.cloudcoder.daemon.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ISocket} that creates an ssh tunnel to
 * the webapp port on the remote webapp host.
 * 
 * @author David Hovemeyer
 */
public class SshTunnelAdapter implements ISocket {
	private static final Logger logger = LoggerFactory.getLogger(SshTunnelAdapter.class);
	
	private static final int LOCAL_PORT_RANGE_START = 10000;

	/**
	 * Give the ssh tunnel process a goodly amount of time to be ready to start
	 * accepting connections.
	 */
	private static final long SSH_READY_WAIT_MS = 10000L;
	
	private static AtomicInteger localPortAllocator = new AtomicInteger(LOCAL_PORT_RANGE_START);
	
	private ISocketFactory socketFactory;
	private String host;
	private int port;
	private String sshRemoteUser;

	// These fields are volatile because the watchdog thread may
	// close the connection asynchronously.
	private volatile Process proc;
	private volatile Socket socket;
	private volatile InputStream in;
	private volatile OutputStream out;

	/**
	 * Constructor.  The {@link #connect()} method must be
	 * called to create the actual ssh tunnel and socket connection.
	 * 
	 * @param socketFactory  the {@link SSLSocketFactory} to use to create the
	 *                       TLS connection
	 * @param host           the remote host
	 * @param port           the remote port
	 * @param sshRemoteUser  the remote user (used to create the ssh tunnel)
	 */
	public SshTunnelAdapter(ISocketFactory socketFactory, String host, int port, String sshRemoteUser) {
		this.socketFactory = socketFactory;
		this.host = host;
		this.port = port;
		this.sshRemoteUser = sshRemoteUser;
	}

	/**
	 * Create the ssh tunnel and socket connection.
	 * Note that if this method throws an exception, the caller
	 * is responsible for calling {@link #close()} to ensure
	 * that all resources are cleaned up (including the ssh
	 * tunnel process.) 
	 * 
	 * @throws IOException
	 */
	public void connect() throws IOException {
		// Allocate a local port.
		// Because each builder thread will use its own ssh tunnel,
		// we need to assign a different port to each.
		int localPort = allocateLocalPort();
		
		// Start the ssh tunnel.  We assume that the current user
		// is authorized to connect to the remote host without
		// providing an explicit username/password.
		String[] cmd = {
				"ssh",
				"-o", "TCPKeepAlive=yes",
				"-T", "-N",
				"-L", localPort + ":" + host + ":" + port,
				sshRemoteUser + "@" + host
		};
		logger.info("Starting ssh tunnel: {}", StringUtil.mergeOneLine(cmd));
		this.proc = Runtime.getRuntime().exec(cmd, ProcessUtil.getEnvArray());
		
		// Wait for a bit to allow ssh to be ready to accept connections
		try {
			Thread.sleep(SSH_READY_WAIT_MS);
		} catch (InterruptedException e) {
			// should not happen
		}
		
		// Create a socket connecting to the local side of the ssh tunnel.
		this.socket = socketFactory.createSocket("localhost", localPort);
		this.in = socket.getInputStream();
		this.out = socket.getOutputStream();
	}

	private int allocateLocalPort() {
		int localPort = 0;
		while (localPort == 0) {
			int p = localPortAllocator.getAndIncrement();
			if (p < 65536) {
				// Port number seems reasonable
				localPort = p;
			} else {
				// Start at beginning of range again
				localPortAllocator.set(LOCAL_PORT_RANGE_START);
			}
		}
		return localPort;
	}

	@Override
	public void close() throws IOException {
		// Destroy the ssh tunnel subprocess
		logger.info("Destroying ssh tunnel process");
		proc.destroy();
		boolean exited = false;
		while (!exited) {
			try {
				proc.waitFor();
				exited = true;
			} catch (InterruptedException e) {
				// ignore
			}
		}
		
		// Close streams
		IOUtil.closeQuietly(in);
		IOUtil.closeQuietly(out);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

}
