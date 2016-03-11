package org.cloudcoder.app.wizard.model;

import java.io.File;

import org.cloudcoder.app.wizard.exec.InstallationConstants;
import org.cloudcoder.app.wizard.model.validators.ConditionalValidator;
import org.cloudcoder.app.wizard.model.validators.FileReadableValidator;
import org.cloudcoder.app.wizard.model.validators.MultiValidator;
import org.cloudcoder.app.wizard.model.validators.NoopValidator;
import org.cloudcoder.app.wizard.model.validators.StringValueEndsInSuffixValidator;
import org.cloudcoder.app.wizard.model.validators.StringValueEqualValidator;
import org.cloudcoder.app.wizard.model.validators.StringValueNonemptyValidator;

import com.amazonaws.services.ec2.model.InstanceType;

public class DocumentFactory {
	public static final String DEFAULT_MYSQL_PASSWD = "abc123";

	/**
	 * Create the instance of {@link Document}.
	 * 
	 * @return the instance of {@link Document}
	 */
	public static Document create() {
		Document document = new Document();
		
		// Add pages
		
		// The "env" page has details about the environment.
		// It's not actually used for user configuration.
		Page dbPage = new Page("db", "Wizard internal db");
		boolean hasCcinstallProperties = new File(InstallationConstants.DATA_DIR, "ccinstall.properties").exists();
		dbPage.add(
				new BooleanValue("hasCcinstallProperties", "hasCcinstallProperties", hasCcinstallProperties),
				NoopValidator.INSTANCE);
		document.addPage(dbPage);
		
		Page selectTaskPage = new Page("selectTask", "Select installation task");
		selectTaskPage.addHelpText("msg", "Message");
		selectTaskPage.add(new EnumValue<InstallationTask>(
				InstallationTask.class, "installationTask", "Installation task"), NoopValidator.INSTANCE);
		document.addPage(selectTaskPage);
		
		Page welcomePage = new Page("welcome", "Welcome to the CloudCoder installation wizard");
		welcomePage.addHelpText("msg", "Welcome message");
		welcomePage.add(new BooleanValue("dryRun", "Do a dry run"), NoopValidator.INSTANCE);
		document.addPage(welcomePage);
		
		Page awsCredentialsPage = new Page("aws", "Enter your AWS credentials");
		awsCredentialsPage.addHelpText("msg", "Message");
		awsCredentialsPage.add(new StringValue("accessKeyId", "Access key ID"), StringValueNonemptyValidator.INSTANCE);
		awsCredentialsPage.add(new PasswordValue("secretAccessKey", "Secret access key"), StringValueNonemptyValidator.INSTANCE);
		document.addPage(awsCredentialsPage);
		
		Page awsRegionPage = new Page("awsRegion", "Choose an AWS region");
		awsRegionPage.addHelpText("msg", "Message");
		awsRegionPage.add(new EnumValue<AWSRegion>(AWSRegion.class, "region", "AWS EC2 Region"), NoopValidator.INSTANCE);
		document.addPage(awsRegionPage);
		
		Page keypairPage = new Page("awsKeypair", "Choose or create a keypair");
		keypairPage.addHelpText("msg", "Message");
		keypairPage.add(new BooleanValue("useExisting", "Use existing keypair"), NoopValidator.INSTANCE);
		keypairPage.add(new FilenameValue("filename", "Existing keypair file"), FileReadableValidator.INSTANCE);
		keypairPage.selectivelyEnable("filename", new EnableIfBooleanFieldChecked("useExisting"));
		document.addPage(keypairPage);
		
		Page instanceTypePage = new Page("awsInstanceType", "Choose EC2 instance type for webapp server");
		instanceTypePage.addHelpText("msg", "Message");
		instanceTypePage.add(
				new EnumValue<InstanceType>(InstanceType.class, "instanceType", "Instance type", InstanceType.T2Micro),
				NoopValidator.INSTANCE);
		document.addPage(instanceTypePage);
		
		Page dnsPage = new Page("dns", "Enter DNS information");
		dnsPage.addHelpText("msg", "Message");
		dnsPage.add(
				new StringValue("hostname", "Hostname"),
				new MultiValidator(
						StringValueNonemptyValidator.INSTANCE,
						new ConditionalValidator("useDuckDns", new StringValueEndsInSuffixValidator(".duckdns.org", true))
						)
				);
		dnsPage.add(new BooleanValue("useDuckDns", "Use Duck DNS"), NoopValidator.INSTANCE);
		dnsPage.add(new PasswordValue("duckDnsToken", "Duck DNS token"), StringValueNonemptyValidator.INSTANCE);
		dnsPage.selectivelyEnable("duckDnsToken", new EnableIfBooleanFieldChecked("useDuckDns"));
		document.addPage(dnsPage);
		
		Page ccAcctPage = new Page("ccAcct", "Enter CloudCoder account information");
		ccAcctPage.addHelpText("msg", "Message", DisplayOption.HALF_HEIGHT);
		ccAcctPage.add(new StringValue("username", "Username"), StringValueNonemptyValidator.INSTANCE);
		ccAcctPage.add(new PasswordValue("password", "Password"), StringValueNonemptyValidator.INSTANCE);
		ccAcctPage.add(new PasswordValue("confirmPassword", "Confirm password"), new StringValueEqualValidator("password"));
		ccAcctPage.add(new StringValue("firstname", "First name"), StringValueNonemptyValidator.INSTANCE);
		ccAcctPage.add(new StringValue("lastname", "Last name"), StringValueNonemptyValidator.INSTANCE);
		ccAcctPage.add(new StringValue("email", "Email address"), StringValueNonemptyValidator.INSTANCE);
		ccAcctPage.add(new StringValue("website", "Website (optional)"), NoopValidator.INSTANCE);
		document.addPage(ccAcctPage);
		
		Page mysqlAcctPage = new Page("mysqlAcct", "Enter MySQL account information");
		mysqlAcctPage.addHelpText("msg", "Message");
		mysqlAcctPage.add(new PasswordValue("rootPasswd", "MySQL root password", DEFAULT_MYSQL_PASSWD), StringValueNonemptyValidator.INSTANCE);
		mysqlAcctPage.add(new PasswordValue("ccPasswd", "MySQL cloudcoder password", DEFAULT_MYSQL_PASSWD), StringValueNonemptyValidator.INSTANCE);
		document.addPage(mysqlAcctPage);
		
		Page instDetailsPage = new Page("instDetails", "Enter instance details");
		instDetailsPage.addHelpText("msg", "Message");
		instDetailsPage.add(new StringValue("institutionName", "Institution name"), StringValueNonemptyValidator.INSTANCE);
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
		// Note that this is just dummy help text: it will be replaced
		// by a dynamically-generated report if the installation succeeds
		// (which is the only way to see the finished page.)
		finishedPage.addHelpText("msg", "Message", DisplayOption.DOUBLE_HEIGHT);
		document.addPage(finishedPage);
		
		document.selectivelyEnablePageRange("welcome", "finished",
				new EnablePageIfEnumSelected<InstallationTask>(
						"selectTask.installationTask",
						InstallationTask.class,
						InstallationTask.INSTALL_CLOUDCODER));
		
		// TODO: needs an actual UI
		Page issueSslPage = new Page("issueSsl", "Issue and install SSL certificate");
		document.addPage(issueSslPage);
		
		document.selectivelyEnablePageRange("issueSsl", "issueSsl",
				new EnablePageIfEnumSelected<InstallationTask>(
						"selectTask.installationTask",
						InstallationTask.class,
						InstallationTask.ISSUE_AND_INSTALL_SSL_CERTIFICATE));

		// The "db" page is always disabled (i.e., not shown to the user)
		document.selectivelyEnablePageRange("db", "db", DisablePage.INSTANCE);
		
		return document;
	}
}
