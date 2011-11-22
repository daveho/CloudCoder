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

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.cloudcoder.app.server.submitsvc.oop.OutOfProcessSubmitService;
import org.cloudcoder.app.shared.model.Problem;
import org.cloudcoder.app.shared.model.TestCase;
import org.cloudcoder.app.shared.model.TestResult;

/**
 * A "Builder" process.  It runs separately from the CloudCoder server,
 * and waits to receive submissions (Problem and program text).
 * For each submission received, it compiles it, tests it,
 * and reports back a list of TestResults.
 * 
 * @author David Hovemeyer
 */
public class Builder implements Runnable {
	private volatile boolean shutdownRequested;
	private volatile boolean working;
	private String host;
	private int port;
	private Map<Integer, Problem> problemIdToProblemMap;
	private Map<Integer, List<TestCase>> problemIdToTestCaseListMap;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public Builder(String host, int port) {
		this.shutdownRequested = false;
		this.host = host;
		this.port = port;
		this.problemIdToProblemMap = new HashMap<Integer, Problem>();
		this.problemIdToTestCaseListMap = new HashMap<Integer, List<TestCase>>();
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

					List<TestResult> testResultList = testSubmission(problem, testCaseList, programText);
					out.writeObject(testResultList);
					out.flush();
				} catch (IOException e) {
					// Quite possibly, this is a routine shutdown of the CloudCoder server.
					// We'll try connecting again soon.
					System.out.println("Error communicating with server");
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
			System.out.println("Connected!");
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// ClientCoder server may not be running right now...try again soon
			System.out.println("Cannot connect to CloudCoder server");
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
				// TODO: log?
			}
		}
	}

	private List<TestResult> testSubmission(Problem problem, List<TestCase> testCaseList, String programText) {
		List<TestResult> testResultList = new ArrayList<TestResult>();

		// FIXME: fake implementation for now
		TestResult testResult = new TestResult("passed", "You rule, dude", "Hello, world", "Oh yeah");
		testResultList.add(testResult);

		// I *TEST* it!!!

		// The Test class is the subject of the test
		StringBuilder test = new StringBuilder();
		test.append("public class Test {\n");
		test.append(programText + "\n");
		test.append("}\n");
		
		// The Tester class contains the unit tests
		// FIXME: this could be cached
		StringBuilder tester = new StringBuilder();
		tester.append("public class Tester {\n");
		
		tester.append("\tpublic static boolean eq(Object o1, Object o2) { return o1.equals(o2); }\n");
		
		for (TestCase tc : testCaseList) {
			tester.append("\tpublic static boolean ");
			tester.append(tc.getTestCaseName());
			tester.append("() {\n");
			tester.append("\t\tTest t = new Test();\n");
			tester.append("\t\treturn eq(" + problem.getTestName() + "(" + tc.getInput() + "), " + tc.getOutput() + ");\n");
			tester.append("\t\t}\n");
		}
		tester.append("}");
		
		// Compile
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		List<JavaFileObject> sources = new ArrayList<JavaFileObject>();
		sources.add(MemoryFileManager.makeSource("Test", test.toString()));
		sources.add(MemoryFileManager.makeSource("Tester", tester.toString()));
		
		MemoryFileManager fm = new MemoryFileManager(compiler.getStandardFileManager(null, null, null));
		// FIXME: should get diagnostics so we can report them
		compiler.getTask(null, fm, null, null, null, sources);
		
		// TODO: use reflection to call test methods
		
		return testResultList;
	}

	public static void main(String[] args) {
		Builder builder = new Builder("localhost", OutOfProcessSubmitService.PORT);
		Thread thread = new Thread(builder);
		thread.start();

		Scanner keyboard = new Scanner(System.in);
		System.out.println("Type shutdown to quit");

		for (;;) {
			String line = keyboard.nextLine();
			if (line == null || line.equals("shutdown")) {
				break;
			}
		}

		try {
			builder.shutdown();
			thread.join();
			System.out.println("Finished");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
