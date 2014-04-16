#! /usr/bin/perl -w

use strict;
use FileHandle;

# Script to collect all configuration information needed to
# build and deploy the CloudCoder webapp, Builder, and
# other components.  Generates a cloudcoder.properties file.

my @propertyNames = ();
my %properties = ();
my %prior = ();

# Which features should be configured
my %features = (
	'webapp' => 1,
	'repoWebApp' => 0,
	'builderWebService' => 0,
	'healthMonitor' => 0,
	'all' => 0,
);

my $useDefaultKeystore = 0;

# Handle command line options for feature configuration
while (scalar(@ARGV) >= 1) {
	my $arg = shift @ARGV;
	if ($arg eq '--repo') {
		$features{'repoWebApp'} = 1;
		print "Configuring for CloudCoder exercise repository\n\n";
	} elsif ($arg eq '--bws') {
		$features{'builderWebService'} = 1;
		print "Configuring for CloudCoder builder web service\n\n";
	} elsif ($arg eq '--nowebapp') {
		$features{'webapp'} = 0;
		print "NOT configuring CloudCoder webapp\n\n";
	} elsif ($arg eq '--healthmonitor') {
		$features{'healthMonitor'} = 1;
		print "Configuring for CloudCoder health monitor\n\n";
	} elsif ($arg eq '--all') {
		$features{'all'} = 1;
		print "Configuring for all features\n\n";
	} else {
		die "Unknown option: $arg\n";
	}
}

print "Welcome to the CloudCoder configuration script!\n";
print "Please enter the information needed to configure CloudCoder for your system.\n";
print "You can just hit enter to accept the default value (if there is one).\n";
print "\n";

if (-r "cloudcoder.properties") {
	my $reload = ask("You seem to have a cloudcoder.properties file already: use it to\n" .
		"load defaults?", "yes");

	if ((lc $reload) eq 'yes') {
		my $prior_fh = new FileHandle("<cloudcoder.properties") || die;
		while (<$prior_fh>) {
			chomp;
			if (/^([^=]+)=(.*)$/) {
				$prior{$1} = $2;
			}
		}
		$prior_fh->close();
	}
}

if (useFeature('webapp')) {
	askprop("Where is your GWT SDK installed (the directory with webAppCreator in it)?",
		"gwt.sdk", undef);
	
	section("Database configuration properties");
	
	askprop("What MySQL username will the webapp use to connect to the database?",
		"cloudcoder.db.user", undef);
	askprop("What MySQL password will the webapp use to connect to the database?",
		"cloudcoder.db.passwd", undef);
	askprop("What MySQL database will contain the CloudCoder tables?",
		"cloudcoder.db.databaseName", "cloudcoderdb");
	askprop("What host will CloudCoder connect to to access the MySQL database?",
		"cloudcoder.db.host", "localhost");
	askprop("If MySQL is running on a non-standard port, enter :XXXX (e.g, :8889 for MAMP).\n" .
		"Just hit enter if MySQL is running on the standard port.",
		"cloudcoder.db.portStr", undef);
	
	section("Login service properties");
	
	askprop("Which login service do you want to use (imap, database, remoteuser)?",
		"cloudcoder.login.service", "database");
	if ($properties{"cloudcoder.login.service"} eq 'imap') {
		askprop("What is the hostname or IP address of your IMAP server?",
			"cloudcoder.login.host", undef);
	} elsif ($properties{"cloudcoder.login.service"} eq 'remoteuser') {
		print "You chose 'remoteuser' as your login service, meaning that\n";
		print "user authentication is provided by having a proxy server\n";
		print "set an X-Remote-User HTTP header that will be trusted by\n";
		print "CloudCoder.  If an untrusted user can send HTTP messages\n";
		print "to CloudCoder, then you have no security.  Proceed at your\n";
		print "own risk!\n";

		ask("Press enter to continue...");
	}
}

# FIXME: it should be possible to not configure the builder
section("Builder properties");

askprop("What host will the CloudCoder webapp be running on?\n" .
	"(This information is needed by the Builder so it knows how to connect\n" .
	"to the webapp.)",
	"cloudcoder.submitsvc.oop.host", "localhost");
askprop("How many threads should the Builder use? (suggestion: 1 per core)",
	"cloudcoder.submitsvc.oop.numThreads", "2");
askprop("What port will the CloudCoder webapp use to listen for connections from\n" .
	"Builders?",
	"cloudcoder.submitsvc.oop.port", "47374");
askprop("Should the builder use EasySandbox for C/C++ submissions? (recommended)",
	"cloudcoder.submitsvc.oop.easysandbox.enable", "true");
if ((lc $properties{"cloudcoder.submitsvc.oop.easysandbox.enable"}) eq 'true') {
	askprop("What should the default EasySandbox heap size be in bytes?\n",
		"cloudcoder.submitsvc.oop.easysandbox.heapsize", "8388608");
}

# Allow builder JVM arguments to be specified
askprop("Are there JVM options that should be used when running the builder?",
	"cloudcoder.builder2.jvmargs", "");

section("TLS/SSL (secure communication between webapp and builder(s))");

my $useDefaultKeystoreYN = ask("Do you want to use the default keystore\n" .
	"(Answer 'yes' for development, 'no' for production)", "no");
print "\n";
$useDefaultKeystore = (lc $useDefaultKeystoreYN) eq 'yes';

if ($useDefaultKeystore) {
	setprop("cloudcoder.submitsvc.ssl.cn", 'None');
	setprop("cloudcoder.submitsvc.ssl.keystore", 'keystore.jks');
	setprop("cloudcoder.submitsvc.ssl.keystore.password", 'changeit');
} else {
	askprop("What is the hostname of your institution?",
		  "cloudcoder.submitsvc.ssl.cn", "None");
	askprop("What is the name of the keystore that will store your public/private keypair?\n" .
		"(A new keystore will be created if it doesn't already exist)",
		"cloudcoder.submitsvc.ssl.keystore", "keystore.jks");
	askprop("What is the keystore/key password?",
		"cloudcoder.submitsvc.ssl.keystore.password", "changeit");
}

if (useFeature('webapp')) {
	section("Web server properties (webapp)");
	
	askprop("What port will the CloudCoder web server listen on?",
		"cloudcoder.webserver.port", "8081");
	askprop("What context path should the webapp use?",
		"cloudcoder.webserver.contextpath", "/cloudcoder");
	askprop("Should the CloudCoder web server accept connections only from localhost?\n" .
		"(Set this to 'true' if using a reverse proxy, which is recommended)",
		"cloudcoder.webserver.localhostonly", "true");
	askprop("How many request handling threads should the webapp use?\n" .
		"(Suggestion: 1/2 expected number of concurrent users)",
		"cloudcoder.webserver.numThreads", "80");
}

if (useFeature('repoWebApp')) {
	section("Database configuration (repository webapp)");

	askprop("What MySQL username will the repository webapp use to connect to the database?",
		"cloudcoder.repoapp.db.user", undef);
	askprop("What MySQL password will the repository webapp use to connect to the database?",
		"cloudcoder.repoapp.db.passwd", undef);
	askprop("What MySQL database will contain the repository tables?",
		"cloudcoder.repoapp.db.databaseName", "cloudcoderrepodb");
	askprop("What host will the repository webapp connect to to access the MySQL database?",
		"cloudcoder.repoapp.db.host", "localhost");
	askprop("If MySQL is running on a non-standard port, enter :XXXX (e.g, :8889 for MAMP).\n" .
		"Just hit enter if MySQL is running on the standard port.",
		"cloudcoder.repoapp.db.portStr", undef);

	section("Webserver configuration (repository webapp)");

	askprop("What port will the exercise repository web server listen on?",
		"cloudcoder.repoapp.webserver.port", "8082");
	askprop("What context path should the exercise repository webapp use?",
		"cloudcoder.repoapp.webserver.contextpath", "/repo");
	askprop("Should the exercise repository web server accept connections only from localhost?\n" .
		"(Set this to 'true' if using a reverse proxy, which is recommended)",
		"cloudcoder.repoapp.webserver.localhostonly", "true");
	askprop("How many request handling threads should the repository webapp use?\n" .
		"(Suggestion: 1/2 expected number of concurrent users)",
		"cloudcoder.repoapp.webserver.numThreads", "80");
	askprop("What SMTP server should the repo webapp use to send mail?",
		"cloudcoder.repoapp.smtp.host", "smtp.1and1.com");
	askprop("What SMTP port should the repo webapp use to send mail?",
		"cloudcoder.repoapp.smtp.port", "587");
	askprop("What SMTP username should the repo webapp use?",
		"cloudcoder.repoapp.smtp.user", undef);
	askprop("What SMTP password should the repo webapp use?",
		"cloudcoder.repoapp.smtp.passwd", undef);
}

if (useFeature('builderWebService')) {
	section("Webserver configuration (builder web service)");

	askprop("What port will the builder web service listen on?",
		"cloudcoder.builderwebservice.port", "8083");
	askprop("What context path should the builder web service use?",
		"cloudcoder.builderwebservice.contextpath", "/bws");
	askprop("Should the builder web service accept connections only from localhost?\n" .
		"(Set this to 'true' if using a reverse proxy, which is recommended)",
		"cloudcoder.builderwebservice.localhostonly", "true");
	askprop("How many request handling threads should the builder web service use?\n" .
		"(Suggestion: 1/2 expected number of concurrent users)",
		"cloudcoder.builderwebservice.numThreads", "80");

	askprop("What username should the client provide?",
		"cloudcoder.builderwebservice.clientusername");
	askprop("What password should the client provide?",
		"cloudcoder.builderwebservice.clientpassword");
}

if (useFeature('healthMonitor')) {
	section("Health monitor configuration");

	askprop("Instances to check (separated by commas)\n" .
		"Example: https://cs.ycp.edu/cloudcoder,https://cloudcoder.org/demo",
		"cloudcoder.healthmonitor.instances");
	askprop("Email address where health status reports should be sent",
		"cloudcoder.healthmonitor.reportEmail");
	askprop("SMTP username of account from which to send email",
		"cloudcoder.healthmonitor.smtp.user");
	askprop("SMTP password of account from which to send email",
		"cloudcoder.healthmonitor.smtp.passwd");
	askprop("SMTP server to use when sending email",
		"cloudcoder.healthmonitor.smtp.host");
	askprop("SMTP port to use when sending email",
		"cloudcoder.healthmonitor.smtp.port");
	askprop("Use TLS with SMTP?",
		"cloudcoder.healthmonitor.smtp.useTLS", "true");
}

my $confirm = ask("Write configuration file (cloudcoder.properties)?", "yes");
if ((lc $confirm) ne 'yes') {
	print "Properties not written\n";
	exit 1;
}

print "Writing properties...";
STDOUT->flush();
my $fh = new FileHandle(">cloudcoder.properties");
foreach my $property (@propertyNames) {
	print $fh "$property=$properties{$property}\n";
}
$fh->close();
print "Done!\n";

if ($useDefaultKeystore) {
	# Use the default keystore.
	run("cp CloudCoderBuilder2/src/defaultkeystore.jks keystore.jks");
} else {
	# Create a keystore if it doesn't already exist.
	if (! -e $properties{'cloudcoder.submitsvc.ssl.keystore'}) {
		run('./createKeystore.pl',
			$properties{'cloudcoder.submitsvc.ssl.keystore'},
			$properties{'cloudcoder.submitsvc.ssl.keystore.password'},
			$properties{'cloudcoder.submitsvc.ssl.cn'});
	}
}

sub useFeature {
	my ($name) = @_;
	(exists $features{$name}) || die "Unknown feature name: $name\n";
	return $features{$name} || $features{'all'};
}

sub section {
	my ($name) = @_;
	print "#" x 72, "\n";
	print " >>> $name <<<\n";
	print "#" x 72, "\n\n";
}

sub ask {
	my ($question, $defval) = @_;

	print "$question\n";
	if (defined $defval) {
		print "[default: $defval] ";
	}
	print "==> ";

	my $value = <STDIN>;
	chomp $value;

	if ((defined $defval) && $value =~ /^\s*$/) {
		$value = $defval;
	}

	return $value;
}

sub askprop {
	my ($question, $property, $defval) = @_;

	# If there are prior properties, use them as defaults.
	if (exists $prior{$property}) {
		$defval = $prior{$property};
	}

	my $value = ask($question, $defval);

	setprop($property, $value);

	print "\n";
}

sub setprop {
	my ($property, $value) = @_;
	push @propertyNames, $property;
	$properties{$property} = $value;
}

sub run {
	system(@_)/256 == 0 || die "Command $_[0] failed\n";
}
