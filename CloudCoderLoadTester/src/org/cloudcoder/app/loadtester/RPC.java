package org.cloudcoder.app.loadtester;

import org.cloudcoder.app.client.rpc.LoginService;

import com.gdevelop.gwt.syncrpc.SyncProxy;

public class RPC {
	public static final int PORT = 8888;
	
	public static final LoginService loginSvc =
			(LoginService) SyncProxy.newProxyInstance(LoginService.class, "http://localhost:" + PORT + "/cloudcoder/", "login");
}
