package org.cloudcoder.app.wizard.model;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.cloudcoder.app.wizard.exec.InstallationConstants;
import org.cloudcoder.app.wizard.model.Document.CompositeNameCallback;
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
	
	private static final List<String> INTERNAL_PAGES = Arrays.asList(
		"db", "prevCcinstall", "selectTask", "welcome"
	);
	
	/**
	 * Check a property name to see whether it's an "internal"
	 * property.  Internal properties shouldn't be loaded or saved.
	 * 
	 * @param propName property name
	 * @return true if the name designates an internal property
	 */
	public static boolean isInternalProperty(String propName) {
		return Document.withCompositeName(propName, new CompositeNameCallback<Boolean>() {
			@Override
			public Boolean execute(String pageName, String name) {
				return INTERNAL_PAGES.contains(pageName);
			}
		});
	}

	/**
	 * Create the instance of {@link Document}.
	 * 
	 * @return the instance of {@link Document}
	 */
	public static Document create() {
		Document document = new Document();
		
		/////////////////////////////////////////////////////////////////
		// Add pages
		/////////////////////////////////////////////////////////////////
		
		// The "db" page contains internal installation variables.
		// It's not actually used for user configuration, and the user
		// will not see it.
		Page dbPage = new Page("db", "Wizard internal db");
		boolean hasCcinstallProperties = new File(InstallationConstants.DATA_DIR, "ccinstall.properties").exists();
		// db.hasCcinstallProperties: true if previous ccinstall.properties are available
		dbPage.add(
				new BooleanValue("hasCcinstallProperties", "hasCcinstallProperties", hasCcinstallProperties),
				NoopValidator.INSTANCE);
		// db.sshConnectViaHostname: true if ssh connections to webapp instance
		// should be made via hostname rather than public IP address.
		// This is because we don't actually know the public IP address.
		dbPage.add(
				new BooleanValue("sshConnectViaHostname", "sshConnectViaHostname", false),
				NoopValidator.INSTANCE);
		// db.installedSslCert: set to true if an SSL certificate is successfully issued/installed
		dbPage.add(new BooleanValue("sslCertInstalled", "sslCertInstalled"), NoopValidator.INSTANCE);
		// db.dnsHostnameConfigured: set to true if a DNS hostname is successfully
		// configured and validated
		dbPage.add(new BooleanValue("dnsHostnameConfigured", "dnsHostnameConfigured"), NoopValidator.INSTANCE);
		document.addPage(dbPage);
		
		// Prompt to load previous ccinstall.properties values
		Page prevCcinstallPage = new Page("prevCcinstall", "Load previous configuration values?");
		prevCcinstallPage.addHelpText("msg", "Message");
		prevCcinstallPage.add(new BooleanValue("usePrevious", "Load previous values"), NoopValidator.INSTANCE);
		document.addPage(prevCcinstallPage);
		// Add a hook to load previous properties if the usePrevious field is checked
		document.addPageNavigationHook(
				"prevCcinstall",
				new ConditionalPageNavigationHook("prevCcinstall.usePrevious", new LoadPreviousCcinstallProperties()));
		
		Page selectTaskPage = new Page("selectTask", "Select installation task");
		selectTaskPage.addHelpText("msg", "Message");
		selectTaskPage.add(new EnumValue<InstallationTask>(
				InstallationTask.class, "installationTask", "Installation task"), NoopValidator.INSTANCE);
		document.addPage(selectTaskPage);
		// If doing post-install issue/install SSL cert, must connect to
		// webapp instance via DNS rather than public IP.
		document.addPageNavigationHook("selectTask", new IPageNavigationHook() {
			@Override
			public void onNext(Document document) {
				IValue taskValue = document.getValue("selectTask.installationTask");
				InstallationTask selectedTask = taskValue.getEnum(InstallationTask.class);
				
				// When installing SSL cert, use hostname to connect via ssh
				if (selectedTask == InstallationTask.ISSUE_AND_INSTALL_SSL_CERTIFICATE) {
					document.getValue("db.sshConnectViaHostname").setBoolean(true);
				}
				
				// Whichever task is being done, set error/finished targets
				document.setErrorPage("error" + selectedTask.getPageSuffix());
				document.setFinishedPage("finished" + selectedTask.getPageSuffix());
			}
		});
		
		// Config pages that should only be used when doing a full installation:
		// keep this up to date!
		String[] fullInstallPages = new String[]{
				"welcome", "aws", "awsRegion",
				"awsKeypair", // user MUST use the previously chosen or generated keypair when installing SSL cert
				"awsInstanceType",
				"dynDns",
				"ccAcct", "mysqlAcct", "instDetails",
				"ready", "error", "finished"
		};
		
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
		
		Page dynDnsPage = new Page("dynDns", "Dynamic DNS information");
		dynDnsPage.addHelpText("msg", "Message");
		dynDnsPage.add(new BooleanValue("useDuckDns", "Use Duck DNS"), NoopValidator.INSTANCE);
		dynDnsPage.add(new PasswordValue("duckDnsToken", "Duck DNS token"), StringValueNonemptyValidator.INSTANCE);
		dynDnsPage.selectivelyEnable("duckDnsToken", new EnableIfBooleanFieldChecked("useDuckDns"));
		document.addPage(dynDnsPage);
		
		Page dnsPage = new Page("dns", "Enter DNS hostname");
		dnsPage.addHelpText("msg", "Message");
		dnsPage.add(
				new StringValue("hostname", "Hostname"),
				new MultiValidator(
						StringValueNonemptyValidator.INSTANCE,
						new ConditionalValidator("dynDns.useDuckDns", new StringValueEndsInSuffixValidator(".duckdns.org", true))
						)
				);
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

		// This is the install page for both full install and installing SSL cert
		Page installPage = new Page("install", "Installing...");
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
		
		// Error page for SSL task
		Page errorSslPage = new Page("errorSsl", "An error occurred installing the SSL certificate");
		errorSslPage.addHelpText("msg", "Message");
		document.addPage(errorSslPage);
		
		// Finished page for SSL task
		Page finishedSslPage = new Page("finishedSsl", "SSL certificate installed successfully!");
		// Dummy text, replaced by report
		finishedSslPage.addHelpText("msg", "Message", DisplayOption.DOUBLE_HEIGHT);
		document.addPage(finishedSslPage);
		
		/////////////////////////////////////////////////////////////////
		// Set up selective page enablement
		/////////////////////////////////////////////////////////////////

		// The "db" page is always disabled (i.e., not shown to the user)
		document.selectivelyEnablePage("db", DisablePage.INSTANCE);
		
		// The "prevCcinstall" page is enabled only if db.hasCcinstallProperties
		// is true (which is true only if ccinstall.properties exists,
		// see above.)
		document.selectivelyEnablePage(
				"prevCcinstall",
				new EnablePageIfBooleanFieldChecked("db.hasCcinstallProperties"));

		// Some config pages are only enabled for the full install step,
		// not for the post-install issue/install SSL cert step.
		ISelectivePageEnablement fullInstallEnablement = new EnablePageIfEnumSelected<InstallationTask>(
				"selectTask.installationTask", InstallationTask.class, InstallationTask.INSTALL_CLOUDCODER);
		for (String pageName : fullInstallPages) {
			document.selectivelyEnablePage(pageName, fullInstallEnablement);
		}
		
		// The issueSsl page is (obviously) only enabled if doing
		// the SSL cert as a post-installation step
		ISelectivePageEnablement issueSslEnablement = new EnablePageIfEnumSelected<InstallationTask>(
				"selectTask.installationTask",
				InstallationTask.class,
				InstallationTask.ISSUE_AND_INSTALL_SSL_CERTIFICATE);
		document.selectivelyEnablePage("issueSsl", issueSslEnablement);
		
		return document;
	}
}
