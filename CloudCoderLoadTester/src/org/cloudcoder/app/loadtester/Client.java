package org.cloudcoder.app.loadtester;

import java.util.HashMap;

import org.cloudcoder.app.client.rpc.LoginService;
import org.cloudcoder.app.shared.model.User;

import com.gdevelop.gwt.syncrpc.SyncProxy;

public class Client {
//	public static void main(String[] args) {
//		User user = RPC.loginSvc.login("dhovemey", "muffin");
//		
//		System.out.println(user != null ? "Logged in!" : "Could not log in");
//		
//		System.out.println("User email is " + user.getEmail());
//	}
	
	private String host;
	private int port;
	private String contextPath;
	private HashMap<Class<?>, Object> serviceMap;
	private User user;
	
	public Client(String host, int port, String contextPath) {
		this.host = host;
		this.port = port;
		this.contextPath = contextPath;
		this.serviceMap = new HashMap<Class<?>, Object>();
	}
	
	private<E> E getService(Class<E> cls, String relativePath) {
		Object obj = serviceMap.get(cls);
		if (obj == null) {
			obj = SyncProxy.newProxyInstance(cls, "http://" + host + ":" + port + "/" + contextPath + "/", relativePath);
			serviceMap.put(cls, obj);
		}
		return cls.cast(obj);
	}
	
	public void login(String username, String password) {
		LoginService loginSvc = getService(LoginService.class, "login");
		this.user = loginSvc.login(username, password);
		if (user == null) {
			throw new IllegalArgumentException("Could not log in");
		}
	}
}
