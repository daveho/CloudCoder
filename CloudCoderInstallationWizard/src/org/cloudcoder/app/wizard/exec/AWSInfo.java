package org.cloudcoder.app.wizard.exec;

import java.util.List;

import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.NetworkInterface;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;

// Collects information needed to do cloud service admin actions.
// This is the implementation for AWS.
public class AWSInfo {
	private Vpc vpc;
	private List<Subnet> subnets;
	private KeyPair keyPair;
	private SecurityGroup securityGroup;
	private Image webappImage;
	private Instance webappInstance;
	private NetworkInterface webappNetworkInterface;
	
	public Vpc getVpc() {
		return vpc;
	}
	
	public void setVpc(Vpc vpc) {
		this.vpc = vpc;
	}
	
	public List<Subnet> getSubnets() {
		return subnets;
	}

	public void setSubnets(List<Subnet> subnet) {
		this.subnets = subnet;
	}
	
	public KeyPair getKeyPair() {
		return keyPair;
	}
	
	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}
	
	public SecurityGroup getSecurityGroup() {
		return securityGroup;
	}

	public void setSecurityGroup(SecurityGroup group) {
		this.securityGroup = group;
	}
	
	public Image getWebappImage() {
		return webappImage;
	}

	public void setWebappImage(Image image) {
		this.webappImage = image;
	}
	
	public Instance getWebappInstance() {
		return webappInstance;
	}

	public void setWebappInstance(Instance instance) {
		this.webappInstance = instance;
	}
	
	public NetworkInterface getWebappNetworkInterface() {
		return webappNetworkInterface;
	}

	public void setWebappNetworkInterface(NetworkInterface ni) {
		this.webappNetworkInterface = ni;
	}
}
