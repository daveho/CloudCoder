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
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
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

	private String host;
	private int port;
	private String keystoreFilename;
	private String keystorePassword;
	private SSLSocketFactory socketFactory;
	/*
	private boolean sshTunnel;
	private String sshRemoteUser;
	*/

	/**
	 * Constructor.
	 * 
	 * @param host               the host on which the webapp is running
	 * @param port               the port the webapp is using to listen for connections from builders
	 * @param keystoreFilename   the name of the keystore file containing the key(s) needed
	 *                           for secure communication with the webapp
	 * @param keystorePassword   the keystore password
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public WebappSocketFactory(String host, int port, String keystoreFilename, String keystorePassword)
			throws IOException, GeneralSecurityException {
		this.host = host;
		this.port = port;
		this.keystoreFilename = keystoreFilename;
		this.keystorePassword = keystorePassword;
		if (this.getClass().getClassLoader().getResource(keystoreFilename)==null) {
		    //XXX this is a hack so that we can distribute jarfiles that communicate securely
		    //TODO better documentation of generating new keystores
		    // also should figure out how to generate keystores from Java programmatically
		    this.keystoreFilename="defaultkeystore.jks";
		    this.keystorePassword="changeit";
		}
		this.socketFactory = createSocketFactory();
		logger.info("Builder: using keystore {}", this.keystoreFilename);
	}
	
//	/**
//	 * Set whether or not an ssh tunnel will be used.
//	 * Defaults to false.  If true, an ssh tunnel will be
//	 * used to allow a port on the local machine to connect
//	 * to the remote webapp port on the webapp host machine.
//	 * If set to true, the {@link #setSshRemoteUser(String)}
//	 * method should be called to specify which user account
//	 * will be used on the remote machine. 
//	 * 
//	 * @param sshTunnel true if ssh 
//	 */
//	public void setSshTunnel(boolean sshTunnel) {
//		this.sshTunnel = sshTunnel;
//	}
//	
//	/**
//	 * Set the remote user account that ssh will use when creating
//	 * the ssh tunnel.
//	 * 
//	 * @param sshRemoteUser the ssh remote user account
//	 */
//	public void setSshRemoteUser(String sshRemoteUser) {
//		this.sshRemoteUser = sshRemoteUser;
//	}

	private SSLSocketFactory createSocketFactory() throws IOException, GeneralSecurityException {
		String keyStoreType = "JKS";
		InputStream keyStoreInputStream = this.getClass().getClassLoader().getResourceAsStream(keystoreFilename);
		if (keyStoreInputStream == null) {
			throw new IOException("Could not load keystore " + keystoreFilename);
		}

		KeyStore keyStore;
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
	 * Create a secure connection to the webapp.
	 * 
	 * @return Socket through which the builder can communicate with the webapp
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public ISocket connectToWebapp() throws UnknownHostException, IOException {
		SSLSocket socket = (SSLSocket) socketFactory.createSocket(host, port);
		socket.setEnabledProtocols(new String[]{"TLSv1"});
		return new SocketAdapter(socket);
	}
}
