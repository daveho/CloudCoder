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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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
	
	// Map of tester classes for known problem types
	private static final Map<ProblemType, Class<? extends ITester>> testerClassMap =
			new HashMap<ProblemType, Class<? extends ITester>>();
	static {
		testerClassMap.put(ProblemType.JAVA_METHOD, JavaTester.class);
		testerClassMap.put(ProblemType.C_FUNCTION, CTester.class);
		testerClassMap.put(ProblemType.C_PROGRAM, CProgramTester.class);
		testerClassMap.put(ProblemType.PYTHON_FUNCTION, PythonTester.class);
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

	private static final int DEFAULT_PORT = 47374;
	
	private static class Options {
		private String appHost;
		private int appPort;
		private int numThreads;
		
		public Options() {
			this.appHost = "localhost";
			this.appPort = DEFAULT_PORT;
			this.numThreads = 2;
		}
		
		/**
		 * @return the appHost
		 */
		public String getAppHost() {
			return appHost;
		}
		
		/**
		 * @return the appPort
		 */
		public int getAppPort() {
			return appPort;
		}
		
		/**
		 * @return the numThreads
		 */
		public int getNumThreads() {
			return numThreads;
		}
		
		public boolean parse(String[] args) {
			for (String arg : args) {
				if (arg.startsWith("--appHost=")) {
					this.appHost = arg.substring("--appHost=".length());
				} else if (arg.startsWith("--appPort=")) {
					this.appPort = Integer.parseInt(arg.substring("--appPort=".length()));
				} else if (arg.startsWith("--numThreads=")) {
					this.numThreads = Integer.parseInt(arg.substring("--numThreads=".length()));
				} else {
					System.err.println("Unknown argument: " + arg);
					return false;
				}
			}
			return true;
		}
	}
	
	private static class BuilderAndThread {
		final Builder builder;
		final Thread thread;
		public BuilderAndThread(Builder builder, Thread thread) {
			this.builder = builder;
			this.thread = thread;
		}
	}

	public static void main(String[] args) {
		// Parse command-line options
		Options options = new Options();
		if (!options.parse(args)) {
			System.err.println("Usage: " + Builder.class.getName() + " [options...]");
			System.err.println("  --appHost=<hostname>         specify CloudCoder application hostname");
			System.err.println("  --appPort=<port>             specify CloudCoder application port");
			System.err.println("  --numThreads=<num threads>   number of builder threads (parallelism)");
			System.exit(1);
		}

		// Start Builder threads
		List<BuilderAndThread> builderAndThreadList = new ArrayList<Builder.BuilderAndThread>();
		for (int i = 0; i < options.getNumThreads(); i++) {
			Builder builder_ = new Builder(options.getAppHost(), options.getAppPort());
			Thread thread_ = new Thread(builder_);
	
			BuilderAndThread builderAndThread = new BuilderAndThread(builder_, thread_);
			builderAndThreadList.add(builderAndThread);
			
			builderAndThread.thread.start();
		}

		// Wait until "quit" is written to the FIFO
		try {
			final String appName = "cloudCoderBuilder";
			BufferedReader reader = new BufferedReader(System.getProperty(appName + ".fifo") != null
					? new FileReader(System.getProperty(appName + ".fifo"))
					: new InputStreamReader(System.in));
			
			try {
				while (true) {
					String line = reader.readLine();
					if (line == null) {
						logger.warn("Reached EOF on FIFO?");
						break;
					}
					if (line.trim().toLowerCase().equals("quit")) {
						logger.info("Quit command read from FIFO");
						break;
					}
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			logger.error("IOException reading from FIFO", e);
		}

		// Shut down all Builder threads
		for (BuilderAndThread builderAndThread : builderAndThreadList) {
			try {
				builderAndThread.builder.shutdown();
				builderAndThread.thread.join();
				logger.info("Finished");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
