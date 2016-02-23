package org.cloudcoder.app.wizard.exec;

import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Vpc;

// Collects information needed to do cloud service admin actions.
// This is the implementation for AWS.
public class AWSInfo {
	private Vpc vpc;
	private KeyPair keyPair;
	
	public Vpc getVpc() {
		return vpc;
	}
	
	public void setVpc(Vpc vpc) {
		this.vpc = vpc;
	}
	
	public KeyPair getKeyPair() {
		return keyPair;
	}
	
	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}
}
