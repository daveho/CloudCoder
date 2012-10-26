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

package org.cloudcoder.submitsvc.oop.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.CompilationOutcome;
import org.cloudcoder.app.shared.model.CompilationResult;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.Submission;
import org.cloudcoder.app.shared.model.SubmissionResult;
import org.cloudcoder.app.shared.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "Builder" process.  It runs separately from the CloudCoder server,
 * and waits to receive submissions (Problem and program text).
 * For each submission received, it compiles it, tests it,
 * and reports back a SubmissionResult.
 * 
 * @author David Hovemeyer
 * @author Jaime Spacco
 */
public class Builder implements Runnable {
    private static final Logger logger=LoggerFactory.getLogger(Builder.class);
    
	private volatile boolean shutdownRequested;
	private volatile boolean working;
	private NoConnectTimer noConnectTimer;
	private WebappSocketFactory webappSocketFactory;
	private Map<Integer, Problem> problemIdToProblemMap;
	private Map<Integer, List<TestCase>> problemIdToTestCaseListMap;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Map<ProblemType, ITester> testerMap;

	public Builder(WebappSocketFactory webappSocketFactory) {
		this.shutdownRequested = false;
		this.noConnectTimer = new NoConnectTimer();
		this.webappSocketFactory = webappSocketFactory;
		this.problemIdToProblemMap = new HashMap<Integer, Problem>();
		this.problemIdToTestCaseListMap = new HashMap<Integer, List<TestCase>>();
		this.testerMap=new HashMap<ProblemType, ITester>();
		
	}
	
	// Map of tester classes for known problem types
	private static final Map<ProblemType, Class<? extends ITester>> testerClassMap =
			new HashMap<ProblemType, Class<? extends ITester>>();
	static {
		testerClassMap.put(ProblemType.JAVA_METHOD, JavaTester.class);
		testerClassMap.put(ProblemType.C_FUNCTION, CTester.class);
		testerClassMap.put(ProblemType.C_PROGRAM, CProgramTester.class);
		testerClassMap.put(ProblemType.PYTHON_FUNCTION, PythonTester.class);
		testerClassMap.put(ProblemType.JAVA_PROGRAM, JavaProgramTester.class);
	}
	
	private ITester getTester(ProblemType problemType) {
		ITester result = testerMap.get(problemType);
	    if (result == null) {
	        // Use reflection to create the tester
	    	Class<? extends ITester> testerCls = testerClassMap.get(problemType);
	    	if (testerCls == null) {
	    		throw new UnsupportedOperationException("problem type "+problemType+" not supported");
	    	}
	    	try {
	    		result = testerCls.newInstance();
	    	} catch (Exception e) {
	    		throw new IllegalStateException("Could not instantiate tester for " + problemType, e);
	    	}
	    	testerMap.put(problemType, result);
	    }
	    return result;
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
					
					// The CloudCoder app may send us a negative problem id as
					// a keepalive signal.  We can just ignore these.
					if (problemId < 0) {
						//logger.debug("Received keepalive signal from CloudCoder app");
						continue requestLoop;
					}
					
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

					// Send the submission details to Builder for testing,
					// read response.
					SubmissionResult result = sendSubmissionForTesting(problem, testCaseList, programText);
					
					out.writeObject(result);
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

	private SubmissionResult sendSubmissionForTesting(Problem problem,
			List<TestCase> testCaseList, String programText) {
		SubmissionResult result;
		try {
		    ITester tester=getTester(problem.getProblemType());
		    Submission submission=new Submission(problem, testCaseList, programText);
		    result=tester.testSubmission(submission);
		} catch (Throwable e) {
			CompilationResult compres=
			        new CompilationResult(CompilationOutcome.BUILDER_ERROR);
			logger.error("Builder error", e);
			result=new SubmissionResult(compres);
		}
		logger.info("Sending SubmissionResult back to server");
		if (result==null) {
		    logger.error("null SubmissionResult");
		} else {
		    if (result.getTestResults()==null) {
		        logger.error("null TestResult");
		    } else {
		        logger.info(result.getTestResults().length+" results");
		    }
		}
		return result;
	}

	private Socket createSecureSocket()
			throws IOException, GeneralSecurityException
	{
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
			// ClientCoder server may not be running right now...try again soon
			//logger.error("Cannot connect to CloudCoder server");
			noConnectTimer.notConnected();
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
		if (working) {
			logger.warn("shutdown(): cannot close worker socket because working=true");
		} else {
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

	/**
	 * A main method for running the Builder interactively (during development).
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Running the builder interactively (type \"shutdown\" to quit)");
		
		BuilderDaemon daemon = new BuilderDaemon();
		
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
