package org.cloudcoder.app.wizard.exec;

import com.jcraft.jsch.JSch;

public class Bootstrap {
	private ICloudInfo info;

	public Bootstrap(ICloudInfo info) {
		this.info = info;
	}
	
	public void bootstrapWebappServer() {
		JSch jsch = new JSch();
		
		// TODO: figure out how to authenticate using PEM file
	}
}
