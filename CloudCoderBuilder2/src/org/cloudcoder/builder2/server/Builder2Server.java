// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2014, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2014, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.daemon.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "Builder" server loop.  It runs separately from the CloudCoder server,
 * and waits to receive submissions (Problem and program text).
 * For each submission received, it compiles it, tests it,
 * and reports back a SubmissionResult.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class Builder2Server implements Runnable {
	/**
	 * The maximum amount of time that the watchdog thread will
	 * allow a wait for a problem id or keepalive signal from 
	 * the webapp.  If the wait becomes longer then we will
	 * assume that the connection between the builder and
	 * the webapp has been broken and the watchdog will force
	 * the server loop to reconnect.
	 */
	private static final long MAX_WAIT_MS = 60000L; // after 1 minute of waiting, assume connection is bad
	
	/**
	 * Runnable for watchdog thread.
	 */
	private class Watchdog implements Runnable {
		@Override
		public void run() {
			try {
				while (!shutdownRequested) {
					Thread.sleep(10000L);
					
					// The watchdog should only interfere with the builder
					// if it is stuck waiting to receive a keepalive signal.
					if (!waitingForKeepalive) {
						continue;
					}
					
					long w = waitStart.get();
					if (w >= 0L) {
						long waitTime = System.currentTimeMillis() - w;
						if (waitTime > MAX_WAIT_MS) {
							// The server loop has waited too long to receive the
							// problem id / keepalive signal.  Force a reconnect.
							logger.warn("Watchdog: {} ms without keepalive, forcing reconnect", waitTime);
							ISocket s = socket;
							if (s != null) {
								try {
									logger.warn("Watchdog: attempting to forcibly close socket...");
									s.close();
								} catch (IOException e) {
									logger.warn("Watchdog: error closing socket", e);
								}
							}
						}
					}
				}
			} catch (InterruptedException e) {
				logger.info("Watchdog interrupted, shutting down...");
			}
		}
	}

    private static final Logger logger=LoggerFactory.getLogger(Builder2Server.class);

    private volatile boolean shutdownRequested;
    private volatile boolean waitingForKeepalive;
    private volatile boolean working;
    private AtomicLong waitStart;
    private NoConnectTimer noConnectTimer;
    private WebappSocketFactory webappSocketFactory;
    private Builder2 builder2;
    private volatile ISocket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Thread watchdogThread;

    /**
     * Constructor.
     * 
     * @param webappSocketFactory the {@link WebappSocketFactory} that will create socket
     *                            connections to the webapp
     * @param config              configuration properties: i.e., properties from cloudcoder.properties file
     */
    public Builder2Server(WebappSocketFactory webappSocketFactory, Properties config) {
        this.shutdownRequested = false;
        this.waitingForKeepalive = false;
        this.working = false;
        this.waitStart = new AtomicLong(-1L);
        this.noConnectTimer = new NoConnectTimer();
        this.webappSocketFactory = webappSocketFactory;
        this.builder2 = new Builder2(config);
    }

    /**
     * The main server loop.
     */
    public void run() {
    	try {
	    	watchdogThread = new Thread(new Watchdog());
	    	watchdogThread.start();
	        while (!shutdownRequested) {
	            runOnce();
	        }
    	} catch (Throwable e) {
    		logger.error("Fatal exception in Builder2Server thread?", e);
    	}
    }

    /**
     * Attempt to read one submission, compile and test it, and send the
     * result back to the webapp.
     */
    protected void runOnce() {
        try {
            if (this.socket == null) {
                attemptToConnectToServer();
                return;
            }

            // Read a message from the webapp, which will begin
            // with an Integer problem id.
            waitingForKeepalive = true;
            long start = System.currentTimeMillis();
			waitStart.set(start); // record the start time of the wait
			//logger.info("Starting wait at {}", start);
            Integer problemId = safeReadObject();
            waitStart.set(-1L); // problem id or keepalive signal received, wait finished
            //logger.info("Received problem id/keepalive from server at {}", System.currentTimeMillis());
            waitingForKeepalive = false;

            // The CloudCoder app will send us a negative problem id as
            // a keepalive signal when there are no submissions that need building/testing.
            // We can just ignore these.
            if (problemId < 0) {
                return;
            }

            try {
            	// Working on testing a submission: should not be forcibly shut down
            	working = true;
            
	            // The protocol allows the builder to cache Problems and TestCases by their problem id,
	            // but this is a very bad idea, since the Problem and TestCases could change on the
	            // webapp side (for example, if an instructor is editing an exercise).
	            // For this reason, we ALWAYS claim not to have the Problem/TestCases, forcing
	            // the webapp to send the most up to date versions.  It's a small amount
	            // of data, and it's important for correct behavior.
	            
	            // Tell the webapp we don't have this Problem/TestCases
	            out.writeObject(Boolean.FALSE);
	            out.flush();
	
	            // Receive the Problem and TestCases
	            Problem problem = safeReadObject();
	            List<TestCase> testCaseList = safeReadObject();
	
	            // read program text
	            String programText = safeReadObject();
	
	            // Test the submission!
	            SubmissionResult result = builder2.testSubmission(problem, testCaseList, programText);
	
	            // Send the SubmissionResult back to the webapp
	            out.writeObject(result);
	            out.flush();
            } finally {
            	// No longer working on testing a submission
            	working = false;
            }
        } catch (IOException e) {
            // Quite possibly, this is a routine shutdown of the CloudCoder server.
            // We'll try connecting again soon.
            logger.error("Error communicating with server");
            socket = null;
            in = null;
            out = null;
        } catch (ClassNotFoundException e) {
            // XXX Should we crash the whole builder here
            // or log the error and keep running?
            throw new IllegalStateException("Class not found reading message", e);
        }
    }

    private ISocket createSecureSocket() throws IOException, GeneralSecurityException {
        return webappSocketFactory.connectToWebapp();
    }

    public void attemptToConnectToServer() {
        try {
            try {
                this.socket=createSecureSocket();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            this.in = new ObjectInputStream(socket.getInputStream());
            logger.info("Connected!");
            noConnectTimer.connected();
            this.out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
        	// It is possible to get an IOException when creating the ObjectInputStream
        	// (e.g., if we get an immediate EOF).  Close the socket and its
        	// input/output streams (if any) and set them to null to inform runOnce() that
        	// there is no active connection.
        	IOUtil.closeQuietly(this.socket);
        	IOUtil.closeQuietly(this.in);
        	IOUtil.closeQuietly(this.out);
        	this.socket = null;
        	this.in = null;
        	this.out = null;
        	
            // ClientCoder server may not be running right now...try again soon
        	logger.info("Failed attempt to connect to server at {}", System.currentTimeMillis());
            noConnectTimer.notConnected(e);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ee) {
                // ignore
            }
        }
    }

    @SuppressWarnings("unchecked")
    private<E> E safeReadObject() throws IOException, ClassNotFoundException {
        Object o = in.readObject();
        if (o == null) {
            throw new IOException("Could not read!");
        }
        return (E) o;
    }

    public void shutdown() {
    	shutdownRequested = true;

    	// Shut down the watchdog thread
    	watchdogThread.interrupt();
    	try {
    		watchdogThread.join();
    	} catch (InterruptedException e) {
    		logger.error("Interrupted waiting for watchdog thread to finish", e);
    	}

    	// Shut down the server loop
    	if (working) {
    		logger.warn("shutdown(): cannot close worker socket because working=true");
    	} else {
    		try {
    			// Close the socket that the server loop is using
    			// to communicate with the webapp.
    			// Rude, but effective.
    			ISocket s = socket;
    			if (s != null) {
    				s.close();
    			}
    		} catch (IOException e) {
    			logger.error("Unable to close client socket, but Builder is shutting down anyway",e);
    		}
    	}
    }

    /**
     * A main method for running the Builder interactively (during development).
     * @param args
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Running the builder interactively (type \"shutdown\" to quit)");

        Builder2Daemon daemon = new Builder2Daemon();

        daemon.start("instance");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String command = reader.readLine();
            if (command == null || command.trim().equals("shutdown")) {
                break;
            }
        }

        daemon.shutdown();

        System.out.println("Builder exiting");
    }
}
