package org.cloudcoder.app.wizard.exec;

import org.cloudcoder.app.wizard.model.AWSRegion;
import org.cloudcoder.app.wizard.model.Document;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;

// Currently hard-coded for Amazon AWS.
// Eventually, implement this for other cloud providers.
public class CloudService {
	private Document document;
	
	private AWSCredentials credentials;
	private AmazonEC2Client client;
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	public void login() {
		// Get AWS credentials, use them to set system properties
		System.setProperty("aws.accessKeyId", document.getValue("aws.accessKeyId").getString());
		System.setProperty("aws.secretKey", document.getValue("aws.secretAccessKey").getString());
		
		SystemPropertiesCredentialsProvider provider = new SystemPropertiesCredentialsProvider();
		this.credentials = provider.getCredentials();
		
		this.client = new AmazonEC2Client(credentials);
		AWSRegion region = document.getValue("awsRegion.region").getEnum(AWSRegion.class);
		client.setEndpoint(region.getEndpoint());
	}
}
