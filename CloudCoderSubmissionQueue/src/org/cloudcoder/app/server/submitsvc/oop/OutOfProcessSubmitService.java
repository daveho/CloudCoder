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

package org.cloudcoder.app.server.submitsvc.oop;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cloudcoder.app.server.submitsvc.IFutureSubmissionResult;
import org.cloudcoder.app.server.submitsvc.ISubmitService;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionException;
import org.cloudcoder.app.shared.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of ISubmitService that relies on out-of-process
 * servers (possibly on different machines) to compile and test
 * submissions. 
 * 
 * @author David Hovemeyer
 */
public class OutOfProcessSubmitService implements ISubmitService, ServletContextListener {
	private static volatile OutOfProcessSubmitService instance;
	private static final Logger logger=LoggerFactory.getLogger(OutOfProcessSubmitService.class);
	/**
	 * Get the singleton instance of OutOfProcessSubmitService.
	 * 
	 * @return the singleton instance of OutOfProcessSubmitService
	 */
	public static OutOfProcessSubmitService getInstance() {
		return instance;
	}
	
	public static final int DEFAULT_PORT = 47374;

	private volatile ServerTask serverTask;
	private Thread serverThread;
	private String keystoreFilename;
	private String keystorePassword;

	/**
	 * Poll to see how many worker tasks, and thus how many connected builder threads,
	 * there are.
	 * 
	 * @return number of connected builder threads
	 */
	public int getNumBuilderThreads() {
		ServerTask theServerTask = serverTask;
		if (theServerTask == null) {
			// In general, this should not happen.
			// It could happen if health data is requested before the webapp
			// and the server side of the OOP build service have had a chance
			// to initialize fully.
			logger.warn("getNumBuilderThreads() called, but there is no server task");
			return 0;
		}
		return theServerTask.getNumWorkerTasks();
	}
	
	@Override
	public IFutureSubmissionResult submitAsync(Problem problem, List<TestCase> testCaseList, String programText) 
	throws SubmissionException 
	{
		if (serverTask == null) {
			throw new IllegalStateException();
		}
		
		if (serverTask.getNumWorkerTasks() == 0) {
			// If no remote Builder threads are connected and running,
			// then there is no point in adding this submission to the queue,
			// since it could sit there forever.  Fail early in this case
			// so there is an obvious diagnostic on the client side.
			throw new SubmissionException("Cannot test submission: no Builders are available");
		}

		// Add the submission to the queue.
		OOPBuildServiceSubmission future = new OOPBuildServiceSubmission(
				new Submission(problem, testCaseList, programText));
		serverTask.submit(future);
		
		return future;
	}
	
	private ServerSocket createSSLServerSocket(int port)
	throws IOException, UnknownHostException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NoSuchProviderException, UnrecoverableKeyException, KeyManagementException
	{
	    String keyStoreType="JKS";
        InputStream keyStoreInputStream=this.getClass().getClassLoader().getResourceAsStream(keystoreFilename);
        if (keyStoreInputStream == null) {
            logger.warn("Could not find keystore file {}, will try defaultkeystore.jks", keystoreFilename);
            //XXX hack; these are the defaults.  Would be nice for
            keystoreFilename="defaultkeystore.jks";
            keystorePassword="changeit";
            keyStoreInputStream=this.getClass().getClassLoader().getResourceAsStream("defaultkeystore.jks");
            if (keyStoreInputStream == null) {
        	        throw new IOException("Could not load keystore from resource " + keystoreFilename);
            }
        }
        
        logger.info("Using keystore {}", keystoreFilename);
        
        // Load the keystore
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(keyStoreInputStream, keystorePassword.toCharArray());

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
        // the final null means use the default secure random source
        sslContext.init(new KeyManager[]{x509KeyManager},
                new TrustManager[]{x509TrustManager}, null);

        SSLServerSocketFactory serverSocketFactory =
                sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket =
                (SSLServerSocket) serverSocketFactory.createServerSocket(port);

        //serverSocket.setNeedClientAuth(true);
        serverSocket.setNeedClientAuth(false);
        // prevent older protocols from being used, especially SSL2 which is insecure
        serverSocket.setEnabledProtocols(new String[]{"TLSv1"});
        return serverSocket;
	}
	
	public void start(int port) throws IOException {
		//ServerSocket serverSocket = new ServerSocket(port);
	    ServerSocket serverSocket = null;
		
		try {
	        serverSocket=createSSLServerSocket(port);
	        if (serverSocket==null) {
	            logger.error("Null SSLServerSocket");
	            throw new RuntimeException("Null SSLServerSocket");
	        }
	        
		} catch (Exception e) {
		    logger.error("Error while creating SSLServerSocket", e);
		    throw new RuntimeException(e);
		}
		
		serverTask = new ServerTask(serverSocket);
		serverThread = new Thread(serverTask);
		serverThread.start();
		logger.info("Out of process submit service server thread started");
	}
	
	public void shutdown() throws InterruptedException {
		serverTask.shutdown();
		serverThread.join();
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		try {
			// Determine keystore filename and password
			keystoreFilename = event.getServletContext().getInitParameter("cloudcoder.submitsvc.ssl.keystore");
			if (keystoreFilename == null) {
				throw new IllegalArgumentException("cloudcoder.submitsvc.ssl.keystore property is not set");
			}
			keystorePassword = event.getServletContext().getInitParameter("cloudcoder.submitsvc.ssl.keystore.password");
			if (keystorePassword == null) {
				throw new IllegalArgumentException("cloudcoder.submitsvc.ssl.keystore.password property is not set");
			}
			
			System.out.println("keystore=" + keystoreFilename + ",password=" + keystorePassword);
			
			// See if a non-default port was specified
			String p = event.getServletContext().getInitParameter("cloudcoder.submitsvc.oop.port");
			int port = (p != null) ? Integer.parseInt(p) : DEFAULT_PORT;
			
			start(port);
			instance = this;
		} catch (IOException e) {
			throw new IllegalStateException("Could not create server thread for oop submit service", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		try {
			shutdown();
			instance = null;
		} catch (InterruptedException e) {
			throw new IllegalStateException("Interrupted while waiting for oop submit service server thread to shut down", e);
		}
	}
}
