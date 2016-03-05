package org.cloudcoder.app.wizard.model;

import org.cloudcoder.app.wizard.model.validators.FileReadableValidator;
import org.cloudcoder.app.wizard.model.validators.NoopValidator;
import org.cloudcoder.app.wizard.model.validators.StringValueNonemptyValidator;

import com.amazonaws.services.ec2.model.InstanceType;

public class DocumentFactory {
	public static final String DEFAULT_MYSQL_PASSWD = "abc123";

	public static Document create() {
		Document document = new Document();
		
		// Add pages
		
		Page selectTaskPage = new Page("selectTask", "Select installation task");
		selectTaskPage.addHelpText("msg", "Message");
		selectTaskPage.add(new EnumValue<InstallationTask>(
				InstallationTask.class, "installationTask", "Installation task"), new NoopValidator());
		document.addPage(selectTaskPage);
		
		Page welcomePage = new Page("welcome", "Welcome to the CloudCoder installation wizard");
		welcomePage.addHelpText("msg", "Welcome message");
		welcomePage.add(new BooleanValue("dryRun", "Do a dry run"), new NoopValidator());
		document.addPage(welcomePage);
		
		Page awsCredentialsPage = new Page("aws", "Enter your AWS credentials");
		awsCredentialsPage.addHelpText("msg", "Message");
		awsCredentialsPage.add(new StringValue("accessKeyId", "Access key ID"), StringValueNonemptyValidator.INSTANCE);
		awsCredentialsPage.add(new StringValue("secretAccessKey", "Secret access key"), StringValueNonemptyValidator.INSTANCE);
		document.addPage(awsCredentialsPage);
		
		Page awsRegionPage = new Page("awsRegion", "Choose an AWS region");
		awsRegionPage.addHelpText("msg", "Message");
		awsRegionPage.add(new EnumValue<AWSRegion>(AWSRegion.class, "region", "AWS EC2 Region"), new NoopValidator());
		document.addPage(awsRegionPage);
		
		Page keypairPage = new Page("awsKeypair", "Choose or create a keypair");
		keypairPage.addHelpText("msg", "Message");
		keypairPage.add(new BooleanValue("useExisting", "Use existing keypair"), new NoopValidator());
		keypairPage.add(new FilenameValue("filename", "Existing keypair file"), new FileReadableValidator());
		keypairPage.selectivelyEnable("filename", new EnableIfBooleanFieldChecked("useExisting"));
		document.addPage(keypairPage);
		
		Page instanceTypePage = new Page("awsInstanceType", "Choose EC2 instance type for webapp server");
		instanceTypePage.addHelpText("msg", "Message");
		instanceTypePage.add(
				new EnumValue<InstanceType>(InstanceType.class, "instanceType", "Instance type", InstanceType.T2Micro),
				new NoopValidator());
		document.addPage(instanceTypePage);
		
		Page dnsPage = new Page("dns", "Enter DNS information");
		dnsPage.addHelpText("msg", "Message");
		dnsPage.add(new StringValue("hostname", "Hostname"), new StringValueNonemptyValidator());
		dnsPage.add(new BooleanValue("useNoIp", "Use No-IP"), new NoopValidator());
		dnsPage.add(new StringValue("noIpUsername", "No-IP username"), new StringValueNonemptyValidator());
		dnsPage.selectivelyEnable("noIpUsername", new EnableIfBooleanFieldChecked("useNoIp"));
		dnsPage.add(new StringValue("noIpPassword", "No-IP password"), new StringValueNonemptyValidator());
		dnsPage.selectivelyEnable("noIpPassword", new EnableIfBooleanFieldChecked("useNoIp"));
		document.addPage(dnsPage);
		
		Page ccAcctPage = new Page("ccAcct", "Enter CloudCoder account information");
		ccAcctPage.addHelpText("msg", "Message", DisplayOption.HALF_HEIGHT);
		ccAcctPage.add(new StringValue("username", "Username"), new StringValueNonemptyValidator());
		ccAcctPage.add(new StringValue("password", "Password"), new StringValueNonemptyValidator());
		ccAcctPage.add(new StringValue("firstname", "First name"), new StringValueNonemptyValidator());
		ccAcctPage.add(new StringValue("lastname", "Last name"), new StringValueNonemptyValidator());
		ccAcctPage.add(new StringValue("email", "Email address (optional)"), new NoopValidator());
		ccAcctPage.add(new StringValue("website", "Website (optional)"), new NoopValidator());
		document.addPage(ccAcctPage);
		
		Page mysqlAcctPage = new Page("mysqlAcct", "Enter MySQL account information");
		mysqlAcctPage.addHelpText("msg", "Message");
		mysqlAcctPage.add(new StringValue("rootPasswd", "MySQL root password", DEFAULT_MYSQL_PASSWD), new StringValueNonemptyValidator());
		mysqlAcctPage.add(new StringValue("ccPasswd", "MySQL cloudcoder password", DEFAULT_MYSQL_PASSWD), new StringValueNonemptyValidator());
		document.addPage(mysqlAcctPage);
		
		Page instDetailsPage = new Page("instDetails", "Enter instance details");
		instDetailsPage.addHelpText("msg", "Message");
		instDetailsPage.add(new StringValue("institutionName", "Institution name"), new StringValueNonemptyValidator());
		document.addPage(instDetailsPage);
		
		Page readyPage = new Page("ready", "Ready to install");
		readyPage.addHelpText("msg", "Message");
		document.addPage(readyPage);
		
		Page installPage = new Page("install", "Installing CloudCoder");
		document.addPage(installPage);
		
		Page errorPage = new Page("error", "An error occurred");
		errorPage.addHelpText("msg", "Message");
		document.addPage(errorPage);
		
		Page finishedPage = new Page("finished", "CloudCoder is installed!");
		finishedPage.addHelpText("msg", "Message");
		document.addPage(finishedPage);
		
		return document;
	}
}
