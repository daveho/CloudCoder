package org.cloudcoder.app.wizard.model;

public enum AWSRegion {
	US_EAST_1("us-east-1"),
	US_WEST_1("us-west-1"),
	US_WEST_2("us-west-2"),
	EU_WEST_1("eu-west-1"),
	EU_CENTRAL_1("eu-central-1"),
	AP_SOUTHEAST_1("ap-southeast-1"),
	AP_SOUTHEAST_2("ap-southeast-2"),
	AP_NORTHEAST_1("ap-northeast-1"),
	AP_NORTHEAST_2("ap-northeast-2"),
	SA_EAST_1("sa-east-1"),
	;
	
	private final String region;
	
	private AWSRegion(String region) {
		this.region = region;
	}
	
	public String getRegion() {
		return region;
	}
	
	public String getEndpoint() {
		return "ec2." + region + ".amazonaws.com";
	}
	
	@Override
	public String toString() {
		return region;
	}
}
