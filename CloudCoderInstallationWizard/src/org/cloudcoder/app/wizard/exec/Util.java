package org.cloudcoder.app.wizard.exec;

public class Util {
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// This should not happen
			System.err.println("Interrupted while sleeping");
		}
	}
}
