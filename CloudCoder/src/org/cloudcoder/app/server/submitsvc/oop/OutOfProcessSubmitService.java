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
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.ssl.KeyMaterial;
import org.apache.commons.ssl.SSLServer;
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

	private ServerTask serverTask;
	private Thread serverThread;
	private String keystoreFilename;
	private String keystorePassword;
//	private String webappKeyAlias;
//	private String builderCertAlias;
	private SSLServer sslServer;
	
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
			throws IOException, GeneralSecurityException
	{
		logger.info("Using keystore {}", keystoreFilename);
		KeyMaterial km = new KeyMaterial(getClass().getResource("/" + keystoreFilename), keystorePassword.toCharArray());

		this.sslServer = new SSLServer();
		sslServer.setKeyMaterial(km);
		sslServer.addTrustMaterial(km);

		return sslServer.createServerSocket(port);
	}
	
	public void start(int port) throws IOException {
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
			keystoreFilename = getConfigProperty(event, "cloudcoder.submitsvc.ssl.webapp.keystore");
			keystorePassword = getConfigProperty(event, "cloudcoder.submitsvc.ssl.keystorepassword");
			
//			// Determine webapp private key alias
//			webappKeyAlias = getConfigProperty(event, "cloudcoder.submitsvc.ssl.webapp.keyalias");
//			
//			// Determine the keystore alias of the builder's certificate,
//			// so we can add it to the truststore.
//			builderCertAlias = getConfigProperty(event, "cloudcoder.submitsvc.ssl.builder.keyalias");
			
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

	private String getConfigProperty(ServletContextEvent event, String propName) {
		String val = event.getServletContext().getInitParameter(propName);
		if (val == null) {
			throw new IllegalArgumentException(propName + " property is not set");
		}
		return val;
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
