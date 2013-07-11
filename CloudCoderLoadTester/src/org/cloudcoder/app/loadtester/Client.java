package org.cloudcoder.app.loadtester;

import org.cloudcoder.app.shared.model.User;

public class Client {
	public static void main(String[] args) {
		User user = RPC.loginSvc.login("dhovemey", "muffin");
		
		System.out.println(user != null ? "Logged in!" : "Could not log in");
		
		System.out.println("User email is " + user.getEmail());
	}
}
