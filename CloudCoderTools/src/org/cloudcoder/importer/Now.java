package org.cloudcoder.importer;

public class Now {
	public static void main(String[] args) {
		long now = System.currentTimeMillis();
		System.out.println("Now: " + now);
		System.out.println("This time tomorrow: " + (now + (24L * 60L * 60L * 1000L)));
	}
}
