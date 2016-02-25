package org.cloudcoder.app.wizard.exec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.NetworkAcl;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.util.IOUtils;

// Collects information needed to do cloud service admin actions.
// This is the implementation for AWS.
public class AWSInfo extends AbstractCloudInfo implements ICloudInfo {
	private Vpc vpc;
	private Subnet subnet;
	private String keyPairFilename;
	private KeyPair keyPair;
	private SecurityGroup securityGroup;
	private Image webappImage;
	private Instance webappInstance;
	private String elasticIpAllocationId;
	private String elasticIp;
	private String webappIpAssociationId;
	private NetworkAcl networkAcl;
	
	@Override
	public String getWebappPublicIp() {
		return getElasticIp();
	}
	
	@Override
	public String getWebappPrivateIp() {
		return webappInstance.getNetworkInterfaces().get(0).getPrivateIpAddress();
	}
	
	@Override
	public String getPrivateKeyFilename() {
		if (keyPairFilename != null) {
			// User specified a keypair file
			return keyPairFilename;
		}
		try {
			// Save the generated keypair in a file in a well-known location
			File temp = new File(getDataDir(), ICloudService.CLOUDCODER_KEYPAIR_NAME + ".pem");
			FileWriter fw = new FileWriter(temp);
			try {
				fw.write(keyPair.getKeyMaterial());
			} finally {
				fw.close();
			}
			return temp.getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException("Could not save keypair to tempfile");
		}
	}
	
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
	
	public String getKeyPairFilename() {
		return keyPairFilename;
	}
	
	public void setKeyPairFilename(String keyPairFilename) {
		this.keyPairFilename = keyPairFilename;
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
