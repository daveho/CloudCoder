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
					
					// Check the current state.
					// If the server loop is not waiting for a keepalive signal,
					// then DON'T MESS WITH IT.
					StateData stateData = stateManager.getStateData();
					if (stateData.getState() != State.WAITING_FOR_KEEPALIVE) {
						continue;
					}
					
					// See how long the server loop has been waiting.
					long waitTime = System.currentTimeMillis() - stateData.getTs();
					if (waitTime > MAX_WAIT_MS) {
						// The server loop has waited too long to receive the
						// problem id / keepalive signal.  Force a reconnect.
						logger.warn("Watchdog: {} ms without keepalive, forcing reconnect", waitTime);
						connectionManager.forceClose();
					}
				}
			} catch (InterruptedException e) {
				logger.info("Watchdog interrupted, shutting down...");
			}
		}
	}

	private static final Logger logger=LoggerFactory.getLogger(Builder2Server.class);

	/**
	 * Server loop states.
	 */
	private enum State {
		/** Created but not running yet. */
		INITIAL,
		/** Server loop started, but not connected to the webapp. */
		NOT_CONNECTED,
		/** Attempting to connect to the webapp. */
		ATTEMPTING_TO_CONNECT,
		/** Connected to the webapp. */
		CONNECTED,
		/** Server loop is waiting for a keepalive signal from the webapp. */
		WAITING_FOR_KEEPALIVE,
		/** Actively working on building/testing a submission. */
		WORKING;

		/**
		 * @return true if it is safe to shutdown, false if not
		 */
		public boolean allowShutdown() {
			return this != WORKING;
		}
	}
	
	/**
	 * Current state data: the state and timestamp when state
	 * was entered.
	 */
	private static class StateData {
		private long ts; // timestamp of when this state was entered
		private State state;
		
		public StateData(long ts, State state) {
			this.ts = ts;
			this.state = state;
		}
		
		public long getTs() {
			return ts;
		}
		
		public State getState() {
			return state;
		}
	}
	
	/**
	 * Singleton object to manage state data.
	 */
	private static class StateManager {
		private Object lock;
		private StateData stateData;
		
		public StateManager() {
			lock = new Object();
			stateData = new StateData(0L, State.INITIAL);
		}
		
		public void setState(State state) {
			synchronized (lock) {
				stateData = new StateData(System.currentTimeMillis(), state);
			}
		}
		
		public StateData getStateData() {
			synchronized (lock) {
				return new StateData(stateData.getTs(), stateData.getState());
			}
		}
	}
	
	/**
	 * Connection to the webapp.
	 */
	private static class Connection {
		private ISocket socket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		
		public Connection(ISocket socket, ObjectInputStream in, ObjectOutputStream out) {
			this.socket = socket;
			this.in = in;
			this.out = out;
		}

		public ISocket getSocket() {
			return socket;
		}
		
		public ObjectInputStream getIn() {
			return in;
		}
		
		public ObjectOutputStream getOut() {
			return out;
		}
	}
	
	/**
	 * Singleton to manage the connection to the webapp.
	 */
	private static class ConnectionManager {
		private Object lock = new Object();
		private Connection conn;

		/**
		 * @return true if a connection to the webapp is open, false if not
		 */
		public boolean isConnected() {
			synchronized (lock) {
				return conn != null;
			}
		}

		/**
		 * @return the {@link Connection} to the webapp
		 */
		public Connection getConnection() {
			synchronized (lock) {
				return conn;
			}
		}

		/**
		 * Set the {@link Connection} to the webapp.
		 * 
		 * @param conn the {@link Connection} to the webapp
		 */
		public void setConnection(Connection conn) {
			synchronized (lock) {
				this.conn = conn;
			}
		}

		/**
		 * Forcibly close the current connection (if any).
		 */
		public void forceClose() {
			synchronized (lock) {
				if (conn != null) {
					Connection c = conn;
					conn = null;
					IOUtil.closeQuietly(c.getSocket());
					IOUtil.closeQuietly(c.getIn());
					IOUtil.closeQuietly(c.getOut());
				}
			}
		}
	}

	private volatile boolean shutdownRequested;
	private StateManager stateManager;
	private NoConnectTimer noConnectTimer;
	private WebappSocketFactory webappSocketFactory;
	private Builder2 builder2;
	private ConnectionManager connectionManager;
	
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
		this.stateManager = new StateManager();
		this.noConnectTimer = new NoConnectTimer();
		this.webappSocketFactory = webappSocketFactory;
		this.builder2 = new Builder2(config);
		this.connectionManager = new ConnectionManager();
	}

	/**
	 * The main server loop.
	 */
	public void run() {
		try {
			stateManager.setState(State.NOT_CONNECTED);
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
			if (!connectionManager.isConnected()) {
				attemptToConnectToServer();
				return;
			}
			Connection conn = connectionManager.getConnection();

			// This is a critical point: the server loop will block waiting
			// for a problem id / keepalive signal.  If there are connection
			// issues, this might block indefinitely.  Entering the
			// WAITING_FOR_KEEPALIVE state lets the watchdog thread know
			// that the connection should be forcibly closed if the server
			// loop gets hung up here.
			stateManager.setState(State.WAITING_FOR_KEEPALIVE);
			Integer problemId = safeReadObject(conn.getIn());
			stateManager.setState(State.WORKING);

			// The CloudCoder app will send us a negative problem id as
			// a keepalive signal when there are no submissions that need building/testing.
			// We can just ignore these.
			if (problemId < 0) {
				stateManager.setState(State.CONNECTED);
				return;
			}

			// The protocol allows the builder to cache Problems and TestCases by their problem id,
			// but this is a very bad idea, since the Problem and TestCases could change on the
			// webapp side (for example, if an instructor is editing an exercise).
			// For this reason, we ALWAYS claim not to have the Problem/TestCases, forcing
			// the webapp to send the most up to date versions.  It's a small amount
			// of data, and it's important for correct behavior.

			// Tell the webapp we don't have this Problem/TestCases
			conn.getOut().writeObject(Boolean.FALSE);
			conn.getOut().flush();

			// Receive the Problem and TestCases
			Problem problem = safeReadObject(conn.getIn());
			List<TestCase> testCaseList = safeReadObject(conn.getIn());

			// read program text
			String programText = safeReadObject(conn.getIn());

			// Test the submission!
			SubmissionResult result = builder2.testSubmission(problem, testCaseList, programText);

			// Send the SubmissionResult back to the webapp
			conn.getOut().writeObject(result);
			conn.getOut().flush();
			
			// Everything went well: return to the CONNECTED state
			stateManager.setState(State.CONNECTED);
		} catch (IOException e) {
			// Quite possibly, this is a routine shutdown of the CloudCoder server.
			// We'll try connecting again soon.
			logger.error("Error communicating with server", e);
			connectionManager.forceClose();
			stateManager.setState(State.NOT_CONNECTED);
		} catch (ClassNotFoundException e) {
			// This should not happen!
			logger.error("Unexpected ClassNotFoundException, shutting down builder thread", e);
			connectionManager.forceClose();
			stateManager.setState(State.NOT_CONNECTED);
			shutdown();
			throw new IllegalStateException("Class not found reading message", e);
		}
	}

	private ISocket createSecureSocket() throws IOException, GeneralSecurityException {
		return webappSocketFactory.connectToWebapp();
	}

	public void attemptToConnectToServer() {
		ISocket socket = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		
		try {
			try {
				stateManager.setState(State.ATTEMPTING_TO_CONNECT);
				socket = createSecureSocket();
			} catch (GeneralSecurityException e) {
				logger.error("Unexpected GeneralSecurityException connecting to webapp", e);
				stateManager.setState(State.NOT_CONNECTED);
				throw new RuntimeException(e);
			}
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			logger.info("Connected!");
			stateManager.setState(State.CONNECTED);
			noConnectTimer.connected();
			connectionManager.setConnection(new Connection(socket, in, out));
		} catch (IOException e) {
			IOUtil.closeQuietly(socket);
			IOUtil.closeQuietly(in);
			IOUtil.closeQuietly(out);
			logger.info("Failed attempt to connect to server at {}", System.currentTimeMillis());
			stateManager.setState(State.NOT_CONNECTED);
			noConnectTimer.notConnected(e);
			try {
				// Cool off for a bit
				Thread.sleep(8000L);
			} catch (InterruptedException ee) {
				// ignore
			}
		}
	}

	@SuppressWarnings("unchecked")
	private<E> E safeReadObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
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

		// Shut down the server loop.
		// If building/testing is in progress, wait for it to finish.
		while (!stateManager.getStateData().getState().allowShutdown()) {
			logger.info("shutdown(): waiting for building/testing of current submission to complete");
			try {
				Thread.sleep(2000L);
			} catch (InterruptedException e) {
				logger.error("Interrupted waiting for server loop to finish current submission");
			}
		}
		
		// Close the socket that the server loop is using
		// to communicate with the webapp.
		// Rude, but effective.
		connectionManager.forceClose();
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
