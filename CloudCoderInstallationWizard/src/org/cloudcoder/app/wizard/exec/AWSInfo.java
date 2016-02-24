package org.cloudcoder.app.wizard.exec;

import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.NetworkAcl;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;

// Collects information needed to do cloud service admin actions.
// This is the implementation for AWS.
public class AWSInfo {
	private Vpc vpc;
	private Subnet subnet;
	private KeyPair keyPair;
	private SecurityGroup securityGroup;
	private Image webappImage;
	private Instance webappInstance;
	private String elasticIpAllocationId;
	private String elasticIp;
	private String webappIpAssociationId;
	private NetworkAcl networkAcl;
	
	public Vpc getVpc() {
		return vpc;
	}
	
	public void setVpc(Vpc vpc) {
		this.vpc = vpc;
	}
	
	public Subnet getSubnet() {
		return subnet;
	}
	
	public void setSubnet(Subnet subnet) {
		this.subnet = subnet;
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
	
	public String getElasticIpAllocationId() {
		return elasticIpAllocationId;
	}

	public void setElasticIpAllocationId(String allocationId) {
		this.elasticIpAllocationId = allocationId;
	}
	
	public String getElasticIp() {
		return elasticIp;
	}

	public void setElasticIp(String publicIp) {
		this.elasticIp = publicIp;
	}
	
	public String getWebappIpAssociationId() {
		return webappIpAssociationId;
	}

	public void setWebappIpAssociationId(String associationId) {
		this.webappIpAssociationId = associationId;
	}
	
	public NetworkAcl getNetworkAcl() {
		return networkAcl;
	}

	public void setNetworkAcl(NetworkAcl acl) {
		this.networkAcl = acl;
	}
}
