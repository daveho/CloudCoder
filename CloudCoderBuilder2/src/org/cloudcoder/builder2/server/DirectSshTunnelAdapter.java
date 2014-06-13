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

import org.cloudcoder.builder2.util.ProcessUtil;
import org.cloudcoder.builder2.util.StringUtil;
import org.cloudcoder.daemon.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A variation of {@link SshTunnelAdapter} that creates a "direct"
 * connection to a remote host/port via an SSH tunnel.  The connection
 * is "direct" because it uses the <code>-W</code> ssh option
 * to connect to the remote host/port.  This means that the
 * process's stdin/stdout can be used to communicate to the remote
 * host/port.  However, this prevents the use of an {@link ISocketFactory}
 * to layer a communication protocol (e.g., SSL) on top of the
 * ssh tunnel.  This isn't really a big loss because layering
 * SSL over ssh is somewhat redundant: use of an SSH tunnel should
 * be used <em>instead of</em> SSL.
 * 
 * @author David Hovemeyer
 */
public class DirectSshTunnelAdapter implements ISocketAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SshTunnelAdapter.class);
	
	private String host;
	private int port;
	private String sshRemoteUser;

	// These fields are volatile because the watchdog thread may
	// close the connection asynchronously.
	private volatile Process proc;
	private volatile InputStream in;
	private volatile OutputStream out;

	/**
	 * Constructor.  The {@link #connect()} method must be
	 * called to create the actual ssh tunnel and socket connection.
	 * 
	 * @param host           the remote host
	 * @param port           the remote port
	 * @param sshRemoteUser  the remote user (used to create the ssh tunnel)
	 */
	public DirectSshTunnelAdapter(String host, int port, String sshRemoteUser) {
		this.host = host;
		this.port = port;
		this.sshRemoteUser = sshRemoteUser;
	}

	public void connect() throws IOException {
		// Start the ssh tunnel.  We assume that the current user
		// is authorized to connect to the remote host without
		// providing an explicit username/password.
		String[] cmd = {
				"ssh",
				"-o", "TCPKeepAlive=yes",
				"-W", "localhost:" + port,
				sshRemoteUser + "@" + host
		};
		logger.info("Starting direct ssh tunnel: {}", StringUtil.mergeOneLine(cmd));
		this.proc = Runtime.getRuntime().exec(cmd, ProcessUtil.getEnvArray());

		// The ssh process's stdin/stdout are forwarded to the
		// remote host/port.
		this.in = proc.getInputStream();
		this.out = proc.getOutputStream();
	}

	@Override
	public void close() throws IOException {
		// Destroy the ssh tunnel subprocess
		logger.info("Destroying direct ssh tunnel process");
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
