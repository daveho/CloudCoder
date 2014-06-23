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
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.cloudcoder.builder2.server.Builder2Daemon.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WebappSocketFactory object creates secure connections to the webapp.
 * 
 * @author Jaime Spacco
 * @author David Hovemeyer
 */
public class WebappSocketFactory {
	private static final Logger logger = LoggerFactory.getLogger(WebappSocketFactory.class);

	private Options options;
	
	/**
	 * Constructor.
	 * 
	 * @param options the {@link Options} describing how to connect to the webapp
	 */
	public WebappSocketFactory(Options options) {
		this.options = options;
	}

	private SSLSocketFactory createSocketFactory() throws IOException, GeneralSecurityException {
		String keyStoreType = "JKS";
		String keystoreFilename = options.getKeystoreFilename();
		InputStream keyStoreInputStream = this.getClass().getClassLoader().getResourceAsStream(keystoreFilename);
		if (keyStoreInputStream == null) {
			throw new IOException("Could not load keystore " + keystoreFilename);
		}

		KeyStore keyStore;
		String keystorePassword = options.getKeystorePassword();
		try {
			keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(keyStoreInputStream, keystorePassword.toCharArray());
		} finally {
			IOUtils.closeQuietly(keyStoreInputStream);
		}

		TrustManagerFactory trustManagerFactory=TrustManagerFactory.getInstance("PKIX", "SunJSSE");
		//trustManagerFactory.init(trustStore);
		// XXX Load the cert (public key) here instead of the private key?
		trustManagerFactory.init(keyStore);

		// TrustManager
		X509TrustManager x509TrustManager = null;
		for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
			if (trustManager instanceof X509TrustManager) {
				x509TrustManager = (X509TrustManager) trustManager;
				break;
			}
		}
		if (x509TrustManager == null) {
			throw new IllegalArgumentException("Cannot find x509TrustManager");
		}

		// KeyManager
		KeyManagerFactory keyManagerFactory =
				KeyManagerFactory.getInstance("SunX509", "SunJSSE");
		keyManagerFactory.init(keyStore, keystorePassword.toCharArray());
		X509KeyManager x509KeyManager = null;
		for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
			if (keyManager instanceof X509KeyManager) {
				x509KeyManager = (X509KeyManager) keyManager;
				break;
			}
		}
		if (x509KeyManager == null) {
			throw new NullPointerException();
		}

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(new KeyManager[]{x509KeyManager},
				new TrustManager[]{x509TrustManager}, null);

		return sslContext.getSocketFactory();
	}

	/**
	 * Create a connection to the webapp.
	 * Note that a connection could be directly secured by SSL/TLS,
	 * or indirectly secured by an ssh tunnel, or both. 
	 * 
	 * @return ISocket through which the builder can communicate with the webapp
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public ISocket connectToWebapp() throws UnknownHostException, IOException, GeneralSecurityException {
		// Create a socket factory that will create a socket to
		// the webapp (either directly or via the ssh tunnel).
		ISocketFactory socketFactory;
		if (options.useSSL()) {
			socketFactory = new SSLSocketFactoryAdapter(createSocketFactory());
		} else {
			socketFactory = new PlainSocketFactory();
		}
		
		// Depending on whether or not an ssh tunnel is being created,
		// use the socket factory to create the actual ISocket connection to the webapp.
		if (options.useSshTunnel()) {
			// Communicate over ssh tunnel.
			// This is a bit redundant in the sense that ssh already implements
			// authentication and encryption.  However, we have seen very
			// strange issues creating TLS connections directly to the
			// webapp port, even though ssh seems to work reliably. (?)
			SshTunnelAdapter socket =
					new SshTunnelAdapter(socketFactory, options.getAppHost(), options.getAppPort(), options.getSshRemoteUser());
			boolean connected = false;
			try {
				socket.connect();
				connected = true;
				return socket;
			} finally {
				if (!connected) {
					socket.close();
				}
			}
		} else {
			// Open connection directly to webapp port.
			Socket socket = socketFactory.createSocket(options.getAppHost(), options.getAppPort());
			return new SocketAdapter(socket);
		}
	}
}
