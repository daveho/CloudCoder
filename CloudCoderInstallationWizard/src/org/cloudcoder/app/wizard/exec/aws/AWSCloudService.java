package org.cloudcoder.app.wizard.exec.aws;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.cloudcoder.app.wizard.exec.ExecException;
import org.cloudcoder.app.wizard.exec.ICloudService;
import org.cloudcoder.app.wizard.exec.InstallationProgress;
import org.cloudcoder.app.wizard.model.AWSRegion;
import org.cloudcoder.app.wizard.model.Document;
import org.cloudcoder.app.wizard.model.DocumentFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;
import com.amazonaws.services.ec2.model.AssociateRouteTableRequest;
import com.amazonaws.services.ec2.model.AssociateRouteTableResult;
import com.amazonaws.services.ec2.model.AttachInternetGatewayRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateInternetGatewayResult;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateRouteRequest;
import com.amazonaws.services.ec2.model.CreateRouteResult;
import com.amazonaws.services.ec2.model.CreateRouteTableRequest;
import com.amazonaws.services.ec2.model.CreateRouteTableResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateSubnetRequest;
import com.amazonaws.services.ec2.model.CreateSubnetResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.CreateVpcResult;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.DomainType;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterface;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.Vpc;

// Cloud service operations for AWS.
// Eventually, implement similar classes for other cloud providers.
public class AWSCloudService implements ICloudService<AWSInfo, AWSCloudService> {
	private static final String CLOUDCODER_VPC_NAME = "cloudcoder-vpc";
	private static final String CLOUDCODER_VPC_SUBNET_NAME = "cloudcoder-vpc-subnet";
	private static final String CLOUDCODER_SECURITY_GROUP_NAME = "cloudcoder-security-group";
	private static final String UBUNTU_SERVER_AMI_OWNER = "099720109477";
	
	// We look for an AMI with this specific name.
	// I checked three regions and found it in all of them,
	// so I'm guessing it's available in all regions.
	private static final String UBUNTU_AMI_NAME =
			"ubuntu/images/hvm-ssd/ubuntu-trusty-14.04-amd64-server-20160114.5";
	
	private Document document;
	
	private AWSCredentials credentials;
	private AmazonEC2Client client;
	
	private AWSInfo info;
	
	public void setDocument(Document document) {
		this.document = document;
		this.info = new AWSInfo();
	}
	
	@Override
	public Document getDocument() {
		return document;
	}
	
	@Override
	public void addInstallSteps(InstallationProgress<AWSInfo, AWSCloudService> progress) {
		progress.addInstallStep(new ProvisioningInstallStep(this));
	}
	
	@Override
	public AWSInfo getInfo() {
		return info;
	}
	
	public void login() throws ExecException {
		try {
			// Get AWS credentials, use them to set system properties
			System.setProperty("aws.accessKeyId", document.getValue("aws.accessKeyId").getString());
			System.setProperty("aws.secretKey", document.getValue("aws.secretAccessKey").getString());
			
			SystemPropertiesCredentialsProvider provider = new SystemPropertiesCredentialsProvider();
			this.credentials = provider.getCredentials();
			
			this.client = new AmazonEC2Client(credentials);
			AWSRegion region = document.getValue("awsRegion.region").getEnum(AWSRegion.class);
			client.setEndpoint(region.getEndpoint());
		} catch (AmazonServiceException e) {
			throw new ExecException("Failed to login to AWS", e);
		}
	}
	
	public void findOrCreateVpc() throws ExecException {
		try {
			// See if there is an existing VPC
			Vpc cloudcoderVpc = null;
			DescribeVpcsResult vpcs = client.describeVpcs();
			for (Vpc vpc : vpcs.getVpcs()) {
				List<Tag> tags = vpc.getTags();
				//System.out.println("VPC id: " + vpc.getVpcId());
				for (Tag t : tags) {
					//System.out.printf("  Tag: key=%s, value=%s\n", t.getKey(), t.getValue());
					if (t.getKey().equals("Name") && t.getValue().equals(CLOUDCODER_VPC_NAME)) {
						cloudcoderVpc = vpc;
						break;
					}
				}
			}
			
			if (cloudcoderVpc != null) {
				System.out.println("Found " + CLOUDCODER_VPC_NAME + ", id=" + cloudcoderVpc.getVpcId());
				info.setVpc(cloudcoderVpc);
				
				// Find its subnet (there should just be one)
				DescribeSubnetsRequest dsReq = new DescribeSubnetsRequest()
					.withFilters(new Filter("vpc-id", Arrays.asList(cloudcoderVpc.getVpcId())));
				DescribeSubnetsResult dsRes = client.describeSubnets(dsReq);
				info.setSubnet(dsRes.getSubnets().get(0));
				return;
			}
			
			// Create a VPC
			CreateVpcRequest req = new CreateVpcRequest("10.0.0.0/16");
			CreateVpcResult result = client.createVpc(req);
			cloudcoderVpc = result.getVpc();
			
			// Tag it with the correct name
			tagResource(cloudcoderVpc.getVpcId(), "Name", CLOUDCODER_VPC_NAME);
			System.out.printf("Tagged VPC %s with Name=%s\n", cloudcoderVpc.getVpcId(), CLOUDCODER_VPC_NAME);
			
			info.setVpc(cloudcoderVpc);
			
			// Get availability zones
			DescribeAvailabilityZonesRequest azReq = new DescribeAvailabilityZonesRequest()
				.withFilters(new Filter("state", Arrays.asList("available")));
			DescribeAvailabilityZonesResult azRes = client.describeAvailabilityZones(azReq);
			
			// Shuffle availability zones
			List<AvailabilityZone> zones = new ArrayList<AvailabilityZone>();
			zones.addAll(azRes.getAvailabilityZones());
			Collections.shuffle(zones);

			// Create a single subnet in a single availability zone,
			// trying each one in order.
			
			Subnet cloudcoderSubnet = null;
			for (AvailabilityZone az : zones) {
				System.out.printf("AZ %s has state %s\n", az.getZoneName(), az.getState());

				// For some insane reason, it is not possible to know
				// in advance which AZs support the creation of subnets:
				// even if the AZ state is "available", trying to create
				// a subnet may throw an error.
				// All we can do is try and see if it fails.
				try {
					// Each subnet can support 251 instances (AWS reserves 4 IPs per subnet)
					CreateSubnetRequest csReq = new CreateSubnetRequest()
						.withAvailabilityZone(az.getZoneName())
						.withCidrBlock("10.0.0.0/24")
						.withVpcId(cloudcoderVpc.getVpcId());
	
					CreateSubnetResult csRes = client.createSubnet(csReq);
					Subnet subnet = csRes.getSubnet();
					
					// Tag the subnet with a meaningful name
					tagResource(subnet.getSubnetId(), "Name", "cloudcoder-vpc-subnet");
					
					cloudcoderSubnet = subnet;
					System.out.printf("Created subnet %s in availability zone %s\n", subnet.getSubnetId(), az.getZoneName());
					
					break;
				} catch (AmazonServiceException e) {
					System.out.printf("Warning: subnet creation in zone %s failed: %s\n", az.getZoneName(), e.getMessage());
				}
			}
			
			if (cloudcoderSubnet == null) {
				AWSRegion region = document.getValue("awsRegion.region").getEnum(AWSRegion.class);
				throw new ExecException("Could not create subnet in any availability zone in " + region);
			}
			info.setSubnet(cloudcoderSubnet);
			
			// Create a VPC gateway
			CreateInternetGatewayResult igRes = client.createInternetGateway();
			InternetGateway ig = igRes.getInternetGateway();
			tagResource(ig.getInternetGatewayId(), "Name", "cloudcoder-gw");
			System.out.printf("Created gateway %s\n", ig.getInternetGatewayId());
			
			// Attach to VPC
			AttachInternetGatewayRequest aigReq = new AttachInternetGatewayRequest()
				.withInternetGatewayId(ig.getInternetGatewayId())
				.withVpcId(cloudcoderVpc.getVpcId());
			client.attachInternetGateway(aigReq);
			
			// Create a route table
			CreateRouteTableRequest rtReq = new CreateRouteTableRequest()
				.withVpcId(cloudcoderVpc.getVpcId());
			CreateRouteTableResult rtRes = client.createRouteTable(rtReq);
			RouteTable rt = rtRes.getRouteTable();
			System.out.printf("Created route table %s\n", rt.getRouteTableId());
			
			// Associate route table with subnet
			AssociateRouteTableRequest artReq = new AssociateRouteTableRequest()
				.withRouteTableId(rt.getRouteTableId())
				.withSubnetId(cloudcoderSubnet.getSubnetId());
			@SuppressWarnings("unused")
			AssociateRouteTableResult artRes = client.associateRouteTable(artReq);
			// Need to keep track of association id?
			
			// Create a route from the subnet to the internet via the gateway
			CreateRouteRequest crReq = new CreateRouteRequest()
				.withDestinationCidrBlock("0.0.0.0/0")
				.withGatewayId(ig.getInternetGatewayId())
				.withRouteTableId(rt.getRouteTableId());
			CreateRouteResult crRes = client.createRoute(crReq);
			
			if (!crRes.getReturn()) {
				throw new ExecException("Could not create default route via gateway");
			}
			
		} catch (AmazonServiceException e) {
			throw new ExecException("Failed to login to enumerate VPCs/create new VPC", e);
		}
	}

	// Add a Name tag to a resource
	private void tagResource(String resourceId, String key, String value) {
		// Some number of retries may be necessary (it's not always possible
		// to tag a resource immediately after it has been created)
		int retries = 0;
		RuntimeException ex = null;
		while (retries < 10) {
			try {
				CreateTagsRequest tagReq = new CreateTagsRequest();
				tagReq.setTags(Arrays.asList(new Tag(key, value)));
				tagReq.setResources(Arrays.asList(resourceId));
				client.createTags(tagReq);
				return;
			} catch (RuntimeException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ee) {
					System.out.println("Interrupted while retrying tag operation");
				}
				ex = e;
				retries++;
			}
		}
		throw ex;
	}
	
	public void createOrChooseKeypair() throws ExecException {
		try {
			if (document.getValue("awsKeypair.useExisting").getBoolean()) {
				// Verify that chosen keypair filename matches the
				// name of existant keypair.  If so, load the key material
				// from a file and continue.
				
				String keyPairFilename = document.getValue("awsKeypair.filename").getString();
				String keyPairName = new File(keyPairFilename).getName();
				int ext = keyPairName.toLowerCase().lastIndexOf('.');
				if (ext >= 0) {
					keyPairName = keyPairName.substring(0, ext);
				}
				
				DescribeKeyPairsResult result = client.describeKeyPairs();
				List<KeyPairInfo> keyPairs = result.getKeyPairs();

				for (KeyPairInfo keyPairInfo : keyPairs) {
					if (keyPairInfo.getKeyName().equals(keyPairName)) {
						System.out.println("Found keypair " + keyPairName);
						this.info.setKeyPair(loadKeyPair(keyPairFilename, keyPairName));
						System.out.println("Loading key from file " + keyPairFilename);
						return;
					}
				}
				
				throw new ExecException("Could not find keypair " + keyPairName);
			} else {
				// Create a new keypair.
				CreateKeyPairRequest req = new CreateKeyPairRequest(ICloudService.CLOUDCODER_KEYPAIR_NAME);
				CreateKeyPairResult result = client.createKeyPair(req);
				this.info.setKeyPair(result.getKeyPair());
				System.out.println("Created keypair " + this.info.getKeyPair().getKeyName());
			}
		} catch (AmazonServiceException e) {
			throw new ExecException("Failed to find or create keypair", e);
		} catch (IOException e) {
			throw new ExecException("Failed to find or create keypair", e);
		}
	}
	
	private KeyPair loadKeyPair(String keyPairFilename, String keyName) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(keyPairFilename));
		String s = new String(data, Charset.forName("UTF-8"));
		KeyPair keyPair = new KeyPair();
		keyPair.setKeyName(keyName);
		keyPair.setKeyMaterial(s);
		// FIXME: is the signature important? Not sure we'll need it.
		return keyPair;
	}
	
	public void findOrCreateSecurityGroup() throws ExecException {
		if (info.getVpc() == null) {
			throw new IllegalArgumentException("Don't call this method until a VPC is found or created");
		}
		try {
			DescribeSecurityGroupsResult result = client.describeSecurityGroups();
			List<SecurityGroup> groups = result.getSecurityGroups();
			for (SecurityGroup group : groups) {
				// Ignore non-VPC security groups
				if (group.getVpcId() == null) {
					continue;
				}
				
				// Check whether the group is in the cloudcoder-vpc VPC.
				String vpcId = group.getVpcId();
				String ccVpcId = info.getVpc().getVpcId();
				System.out.printf("Group vpc id=%s, cc vpc id=%s\n", vpcId, ccVpcId);
				if (!vpcId.equals(ccVpcId)) {
					continue;
				}
				// Check the group name.
				if (group.getGroupName().equals(CLOUDCODER_SECURITY_GROUP_NAME)) {
					info.setSecurityGroup(group);
					return;
				}
			}
			
			// No matching security group, so create a new one.
			CreateSecurityGroupRequest gReq = new CreateSecurityGroupRequest();
			gReq.setGroupName(CLOUDCODER_SECURITY_GROUP_NAME);
			gReq.setVpcId(info.getVpc().getVpcId());
			gReq.setDescription("CloudCoder security group");
			CreateSecurityGroupResult gRes = client.createSecurityGroup(gReq);
			System.out.println("Created " + CLOUDCODER_SECURITY_GROUP_NAME);
			
			// Configure rules
			String groupId = gRes.getGroupId();
			allowIngress(22, groupId);
			allowIngress(443, groupId);
			allowIngress(47374, groupId);
			System.out.println("Added ingress rules to " + CLOUDCODER_SECURITY_GROUP_NAME);
			
			// Annoyingness: the CreateSecurityGroupResult doesn't give us a
			// SecurityGroup object.  We'll query it as an extra second step.
			DescribeSecurityGroupsRequest gReq2 = new DescribeSecurityGroupsRequest();
			gReq2.setGroupIds(Arrays.asList(groupId));
			DescribeSecurityGroupsResult gRes2 = client.describeSecurityGroups(gReq2);
			List<SecurityGroup> groups2 = gRes2.getSecurityGroups();
			for (SecurityGroup g : groups2) {
				if (g.getGroupId().equals(groupId)) {
					System.out.println("Reloaded " + CLOUDCODER_SECURITY_GROUP_NAME + " via API");
					info.setSecurityGroup(g);
					return;
				}
			}
			throw new ExecException("Failed to reload " + CLOUDCODER_SECURITY_GROUP_NAME + " via API");
		} catch (AmazonServiceException e) {
			throw new ExecException("Failed to find or create security group", e);
		}
	}

	private void allowIngress(int port, String groupId) {
		AuthorizeSecurityGroupIngressRequest iReq = new AuthorizeSecurityGroupIngressRequest()
			.withGroupId(groupId)
			.withIpPermissions(new IpPermission()
				.withIpRanges("0.0.0.0/0")
				.withIpProtocol("tcp")
				.withFromPort(port)
				.withToPort(port)
			);
		client.authorizeSecurityGroupIngress(iReq);
	}
	
	public void findUbuntuServerImage() throws ExecException {
		try {
			// Find an appropriate Ubuntu server AMI.
			// This is somewhat ad-hoc for now.
			
			DescribeImagesRequest iReq = new DescribeImagesRequest()
				.withOwners(Arrays.asList(UBUNTU_SERVER_AMI_OWNER))
				.withFilters(
						new Filter().withName("owner-id").withValues(UBUNTU_SERVER_AMI_OWNER),
						new Filter().withName("name").withValues(UBUNTU_AMI_NAME)
						);
			
			DescribeImagesResult iRes = client.describeImages(iReq);
			
			System.out.println("Found candidates:");
			List<Image> candidates = new ArrayList<Image>();
			candidates.addAll(iRes.getImages());
			
			if (candidates.isEmpty()) {
				throw new ExecException("Could not find any suitable Ubuntu server images!");
			}
			
			Image webappImage = candidates.get(0);
			System.out.printf("Found image: arch=%s, name=%s, id=%s\n",
					webappImage.getArchitecture(),
					webappImage.getName(),
					webappImage.getImageId());
			info.setWebappImage(webappImage);
		} catch (AmazonServiceException e) {
			throw new ExecException("Failed to create webapp instance", e);
		}
	}
	
	public void createWebappElasticIp() throws ExecException {
		try {
			// Sadly, elastic IP addresses cannot be tagged, so
			// we can't see if there is an existing one.
			// So, create a new one.
			
			AllocateAddressRequest aaReq = new AllocateAddressRequest()
				.withDomain(DomainType.Vpc);
			AllocateAddressResult aaRes = client.allocateAddress(aaReq);
			
			info.setElasticIpAllocationId(aaRes.getAllocationId());
			info.setElasticIp(aaRes.getPublicIp());
		} catch (AmazonServiceException e) {
			throw new ExecException("Could not find or create an elastic IP address for webapp instance");
		}
	}
	
	public void createWebappInstance() throws ExecException {
		try {
			System.out.println("Starting webapp instance...");
			RunInstancesRequest req = new RunInstancesRequest()
				.withImageId(info.getWebappImage().getImageId())
				.withInstanceType(document.getValue("awsInstanceType.instanceType").getEnum(InstanceType.class))
				.withMinCount(1)
				.withMaxCount(1)
				.withSubnetId(info.getSubnet().getSubnetId())
				.withSecurityGroupIds(info.getSecurityGroup().getGroupId())
				.withKeyName(info.getKeyPair().getKeyName());
			
			RunInstancesResult res = client.runInstances(req);
			
			Reservation resv = res.getReservation();
			List<Instance> instances = resv.getInstances();
			
			Instance webappInstance = instances.get(0);
			System.out.printf("Instance id=%s\n", webappInstance.getInstanceId());
			
			info.setWebappInstance(webappInstance);
		} catch (AmazonServiceException e) {
			throw new ExecException("Could not start webapp instance", e);
		}
	}
	
	public void waitForInstanceToStart() throws ExecException {
		try {
			System.out.printf(
					"Waiting for instance %s to reach running state (this could take several minutes)\n",
					info.getWebappInstance().getInstanceId());
			
			// Poll every 20 seconds, for up to 10 minutes
			int retries = 0;
			while (retries < 30) {
				DescribeInstancesRequest diReq = new DescribeInstancesRequest()
					.withInstanceIds(info.getWebappInstance().getInstanceId());
				DescribeInstancesResult diRes = client.describeInstances(diReq);
				Reservation res = diRes.getReservations().get(0);
				InstanceState state = res.getInstances().get(0).getState();
				System.out.printf("Instance state is %s\n", state.getName());
				if (state.getName().equals("running")) {
					break;
				}
				System.out.println("Sleeping for 20 seconds...");
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					System.out.println("Interrupted while waiting for instance to reach running state");
				}
				retries++;
			}
		} catch (AmazonServiceException e) {
			throw new ExecException("Failure waiting for webapp instance to start");
		}
	}
	
	public void assignPublicIpToWebapp() throws ExecException {
		try {
			InstanceNetworkInterface iface = info.getWebappInstance().getNetworkInterfaces().get(0);
			
			AssociateAddressRequest aaReq = new AssociateAddressRequest()
				.withAllocationId(info.getElasticIpAllocationId())
				.withInstanceId(info.getWebappInstance().getInstanceId())
				.withNetworkInterfaceId(iface.getNetworkInterfaceId());
			AssociateAddressResult aaRes = client.associateAddress(aaReq);
			info.setWebappIpAssociationId(aaRes.getAssociationId());
			
			System.out.printf("Successfully associated public ip address %s with instance %s\n",
					info.getElasticIp(), info.getWebappInstance().getInstanceId());
		} catch (AmazonServiceException e) {
			throw new ExecException("Failed to associate public ip with webapp instance", e);
		}
	}

	// This is just for testing.
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Access key ID: ");
		String accessKeyId = keyboard.nextLine();
		System.out.print("Secret access key: ");
		String secretAccessKey = keyboard.nextLine();
		System.out.print("Keypair filename: ");
		String keyPairFilename = keyboard.nextLine();
		
		Document document = DocumentFactory.create();
		document.getValue("aws.accessKeyId").setString(accessKeyId);
		document.getValue("aws.secretAccessKey").setString(secretAccessKey);
		
		document.getValue("awsKeypair.useExisting").setBoolean(true);
		document.getValue("awsKeypair.filename").setString(keyPairFilename);
		
		AWSCloudService svc = new AWSCloudService();
		svc.setDocument(document);
		try {
			svc.login();
			svc.findOrCreateVpc();
			svc.createOrChooseKeypair();
			svc.findOrCreateSecurityGroup();
			svc.findUbuntuServerImage();
			svc.createWebappElasticIp();
			svc.createWebappInstance();
			svc.waitForInstanceToStart();
			svc.assignPublicIpToWebapp();
		} catch (ExecException e) {
			System.err.println("Error occurred");
			e.printStackTrace();
		}
	}
}
