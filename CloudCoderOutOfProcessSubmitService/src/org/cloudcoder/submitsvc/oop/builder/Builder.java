// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
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

package org.cloudcoder.submitsvc.oop.builder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.cloudcoder.app.server.submitsvc.oop.OutOfProcessSubmitService;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestOutcome;
import org.cloudcoder.app.shared.model.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "Builder" process.  It runs separately from the CloudCoder server,
 * and waits to receive submissions (Problem and program text).
 * For each submission received, it compiles it, tests it,
 * and reports back a list of TestResults.
 * 
 * @author David Hovemeyer
 */
public class Builder implements Runnable {
    private static final Logger logger=LoggerFactory.getLogger(Builder.class);
    
	private volatile boolean shutdownRequested;
	private volatile boolean working;
	private String host;
	private int port;
	private Map<Integer, Problem> problemIdToProblemMap;
	private Map<Integer, List<TestCase>> problemIdToTestCaseListMap;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Map<ProblemType, ITester> testerMap;

	public Builder(String host, int port) {
		this.shutdownRequested = false;
		this.host = host;
		this.port = port;
		this.problemIdToProblemMap = new HashMap<Integer, Problem>();
		this.problemIdToTestCaseListMap = new HashMap<Integer, List<TestCase>>();
		this.testerMap=new HashMap<ProblemType, ITester>();
	}
	
	private ITester getTester(ProblemType problemType) {
	    if (!testerMap.containsKey(problemType)) {
	        //XXX Could use reflection to create the tester
	        // could also dynamically replace the testers for a running server
	        // may need to use JMX for this?
	        // could somehow encode versions of testers
	        if (problemType==ProblemType.JAVA_METHOD) {
	            testerMap.put(problemType, new JavaTester());
	        } else if (problemType==ProblemType.C_FUNCTION) {
	            testerMap.put(problemType, new CTester());
	        } else if (problemType==ProblemType.PYTHON_FUNCTION) {
	            testerMap.put(ProblemType.PYTHON_FUNCTION, new PythonTester());
	        } else {
	            throw new UnsupportedOperationException("problem type "+problemType+" not supported");
	        }
	    }
	    return testerMap.get(problemType);
	}

	public void run() {
		requestLoop:
			while (!shutdownRequested) {
				try {
					if (this.socket == null) {
						attemptToConnectToServer();
						continue requestLoop;
					}

					working = false;
					Integer problemId = safeReadObject();
					working = true;
					
					Problem problem = problemIdToProblemMap.get(problemId);
					List<TestCase> testCaseList = problemIdToTestCaseListMap.get(problemId);

					// let the server know whether or not we have this
					// problem cached
					out.writeObject((Boolean) (problem != null));
					out.flush();

					// if we don't have the problem, the server will
					// send it to us
					if (problem == null) {
						problem = safeReadObject();
						testCaseList = safeReadObject();
					}

					// read program text
					String programText = safeReadObject();

					List<TestResult> testResultList;
					try {
					    ITester tester=getTester(problem.getProblemType());
					    testResultList = tester.testSubmission(problem, testCaseList, programText);
					} catch (IllegalStateException e) {
						testResultList = new ArrayList<TestResult>();
						testResultList.add(new TestResult(TestOutcome.INTERNAL_ERROR, e.getMessage()));
					}
					out.writeObject(testResultList);
					out.flush();
				} catch (IOException e) {
					// Quite possibly, this is a routine shutdown of the CloudCoder server.
					// We'll try connecting again soon.
					logger.error("Error communicating with server");
					socket = null;
					in = null;
					out = null;
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException("Class not found reading message", e);
				}
			}
	}

	public void attemptToConnectToServer() {
		try {
			this.socket = new Socket(host, port);
			this.in = new ObjectInputStream(socket.getInputStream());
			logger.info("Connected!");
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// ClientCoder server may not be running right now...try again soon
			logger.error("Cannot connect to CloudCoder server");
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
		if (!working) {
			try {
				// Rude, but effective.
				Socket s = socket;
				if (s != null) {
					s.close();
				}
			} catch (IOException e) {
			    logger.error("Unable to close client socket, but Builder is shutting down anyway",e);
			}
		}
	}

	public static void main(String[] args) {
		Builder builder = new Builder("localhost", OutOfProcessSubmitService.PORT);
		Thread thread = new Thread(builder);
		thread.start();

		Scanner keyboard = new Scanner(System.in);
		logger.warn("Type shutdown to quit");

		for (;;) {
			String line = keyboard.nextLine();
			if (line == null || line.equals("shutdown")) {
				break;
			}
		}

		try {
			builder.shutdown();
			thread.join();
			logger.warn("Finished");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
