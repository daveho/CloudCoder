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
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public Builder(String host, int port) {
		this.shutdownRequested = false;
		this.host = host;
		this.port = port;
		this.problemIdToProblemMap = new HashMap<Integer, Problem>();
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

					// let the server know whether or not we have this
					// problem cached
					out.writeObject((Boolean) (problem != null));
					out.flush();

					// if we don't have the problem, the server will
					// send it to us
					if (problem == null) {
						problem = safeReadObject();
					}

					// read program text
					String programText = safeReadObject();

					List<TestResult> testResultList = testSubmission(problem, programText);
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

	private List<TestResult> testSubmission(Problem problem, String programText) {
		List<TestResult> testResultList = new ArrayList<TestResult>();

		// FIXME: fake implementation for now
		TestResult testResult = new TestResult("passed", "You rule, dude", "Hello, world", "Oh yeah");
		testResultList.add(testResult);

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
