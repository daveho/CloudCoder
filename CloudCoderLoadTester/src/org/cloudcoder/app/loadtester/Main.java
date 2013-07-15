package org.cloudcoder.app.loadtester;

public class Main {
	public static void main(String[] args) {
		Client client = new Client("http", "localhost", 8081, "cloudcoder/cloudcoder");
		client.login("dhovemey", "muffin");
		System.out.println("Successful login!");
	}
}
