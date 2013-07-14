package org.cloudcoder.app.loadtester;

public class Main {
	public static void main(String[] args) {
		Client client = new Client("localhost", 8888, "cloudcoder");
		client.login("dhovemey", "muffin");
		System.out.println("Successful login!");
		
		
		
	}
}
