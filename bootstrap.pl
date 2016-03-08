#! /usr/bin/perl -w

use strict;
use FileHandle;
use Getopt::Long qw{:config bundling no_ignore_case no_auto_abbrev};

# Bootstrap CloudCoder on an Ubuntu server

####################################################################
# Global data
####################################################################

# Selectable features
my %features = (
	'apache' => 1,
	'integrated-builder' => 0,
);
my %featuresHelp = (
	'apache' => 'Install Apache2 as an SSL proxy',
	'integrated-builder' => 'Install integrated builder',
);

# Download site
my $DOWNLOAD_SITE = 'https://s3.amazonaws.com/cloudcoder-binaries';

my $program = $0;
#print "program=$program\n";
#exit 0;

# Configuration properties
my %props = ();
my $propsLoaded = 0;

# Parse command line options
my %opts = ();

# Original options
my @origOpts;

####################################################################
# Parse command line, execute
####################################################################

# Save the original argument list.
@origOpts = @ARGV;

GetOptions(\%opts,
	qw(
		dry-run|n!
		help|h!
		enable=s
		disable=s
		config=s
		no-start!
		no-localhost-only!
		defer-keystore!
	)
) or (Usage() && exit 1);

if (exists $opts{'help'}) {
	Usage();
	exit 0;
}

my $dryRun = 0;
if (exists $opts{'dry-run'}) {
	print ">>> Dry run <<<\n";
	$dryRun = 1;
}

# See if any features are being enabled or disabled
if (exists $opts{'enable'}) {
	for my $feature (split(',', $opts{'enable'})) {
		$features{$feature} = 1;
	}
}
if (exists $opts{'disable'}) {
	for my $feature (split(',', $opts{'disable'})) {
		$features{$feature} = 0;
	}
}

# Preserve original options (so that they can be passed to step2.)
# We assume that anything that was not consumed by GetOptions
# is not an option.
my $npop = scalar(@ARGV);
while ($npop-- > 0) {
	pop @origOpts;
}
#print "Original options: ", join(' ', @origOpts), "\n";
#exit 0;

my $mode = 'start';

# See if the mode was specified explicitly
if (scalar(@ARGV) > 0) {
	$mode = shift @ARGV;
}

# Assume that any remaining command line option is
# stringified config properties (which is what should
# happen if step2 is being executed).
if (scalar(@ARGV) > 0) {
	%props = UnstringifyProps(shift @ARGV);
	$propsLoaded = 1;
}

# If configuration properties were specified by a command
# line option, attempt to load them.  If the mode is
# anything other than "start", failing to load them
# is a non-fatal error.
if (exists $opts{'config'}) {
	eval {
		LoadConfigProperties($opts{'config'});
		$propsLoaded = 1;
	};
	if ($@ && $mode eq 'start') {
		die $@;
	}
}

if ($mode eq 'start') {
	Start();
} elsif ($mode eq 'step2') {
	Step2();
} elsif ($mode eq 'generate-keystore') {
	GenerateAndConfigureKeystore();
} elsif ($mode eq 'configure-builder') {
	ConfigureIntegratedBuilder();
} elsif ($mode eq 'letsencrypt') {
	LetsEncrypt();
} else {
	die "Unknown mode: $mode\n";
}

####################################################################
# Subroutines
####################################################################

# Start does all of the sudo commands to install and configure
# software, create the cloud user, etc.
sub Start {
	# If configuration properties haven't been specified
	# via --config or by a stringified config properties argument,
	# read them interactively.
	if (!$propsLoaded) {
		ConfigureInteractively();
	}

	# Ensure that $HOME directory is not world-readable
	RunAdmin(cmd => ['chmod', '0700', $ENV{'HOME'}]);

	# We will run apt noninteractively
	$ENV{'DEBIAN_FRONTEND'} = 'noninteractive';
	
	# ----------------------------------------------------------------------
	# Install/configure required packages
	# ----------------------------------------------------------------------
	Section("Installing required packages...");

	# Run apt-get update so that repository metadata is current
	RunAdmin(cmd => ["apt-get", "update"]);

	# Determine which mysql-server version we will use
	my $mysqlVersion = FindMysqlVersion();
	print "Mysql version is $mysqlVersion\n";

	# Configure mysql root password so that no user interaction
	# will be required when installing packages.
	DebconfSetSelections("mysql-server-$mysqlVersion", "mysql-server/root_password", "password $props{'ccMysqlRootPasswd'}");
	DebconfSetSelections("mysql-server-$mysqlVersion", "mysql-server/root_password_again", "password $props{'ccMysqlRootPasswd'}");

	# Install packages.
	# A headless JRE is sufficient for the webapp.
	# "wget" isn't installed by default in the ubuntu docker image.
	# "fastjar" is needed because the JRE doesn't include the jar
	# utility, which the docker image entrypoint needs to check
	# whether a keystore has been generated/configured.
	# "git" is needed for Let's Encrypt.
	my @packages = ("wget", "fastjar", "git", "openjdk-7-jre-headless", "mysql-client-$mysqlVersion", "mysql-server-$mysqlVersion");
	if ($features{'apache'}) {
		push @packages, 'apache2';
	}
	if ($features{'integrated-builder'}) {
		push @packages, 'gcc';
		push @packages, 'g++';
	}

	my @cmd = ("apt-get", "-y", "install");
	push @cmd, @packages;

	RunAdmin(cmd => \@cmd);

	# Find out what the latest CloudCoder version is,
	# creating the global CLOUDCODER_VERSION file.
	# (This requires wget, so we do it after installing software.)
	my $version = GetLatestVersion();

	# Start mysql if it is not already running.
	# This is useful for docker, where daemons don't
	# start automatically when installed.
	my $mysqlStatus = `service mysql status`;
	chomp $mysqlStatus;
	if ($mysqlStatus =~ /stop/) {
		RunAdmin(cmd => ['service', 'mysql', 'start']);
		Run("sleep", "5");
	}
	
	# ----------------------------------------------------------------------
	# Configure MySQL
	# ----------------------------------------------------------------------
	Section("Configuring MySQL...");
	print "Creating cloudcoder user...\n";
	Run("mysql", "--user=root", "--password=$props{'ccMysqlRootPasswd'}",
		"--execute=create user 'cloudcoder'\@'localhost' identified by '$props{'ccMysqlCCPasswd'}'");
	print "Granting permissions on cloudcoderdb to cloudcoder...\n";
	Run("mysql", "--user=root", "--password=$props{'ccMysqlRootPasswd'}",
		"--execute=grant all on cloudcoderdb.* to 'cloudcoder'\@'localhost'");
	
	# ----------------------------------------------------------------------
	# Create cloud user
	# ----------------------------------------------------------------------
	Section("Creating cloud user account...");
	RunAdmin(
		cmd => [ 'adduser', '--disabled-password', '--home', '/home/cloud', '--gecos', '', 'cloud' ]
	);

	# Ensure that /home/cloud is not world-readable
	RunAdmin(cmd => ['chmod', '0700', '/home/cloud']);

	# ----------------------------------------------------------------------
	# Configure apache2
	# ----------------------------------------------------------------------
	if ($features{'apache'}) {
		Section("Configuring apache2...");
		print "Generating SSL configuration...\n";
		EditApache2DefaultSsl($props{'ccHostname'});
		print "Enabling modules...\n";
		RunAdmin(cmd => ['a2enmod', 'proxy']);
		RunAdmin(cmd => ['a2enmod', 'proxy_http']);
		RunAdmin(cmd => ['a2enmod', 'ssl']);
		print "Restarting...\n";
		RunAdmin(cmd => ['service', 'apache2', 'restart']);
	}

	# ----------------------------------------------------------------------
	# Continue as the cloud user to download and configure
	# webapp and builder jarfiles.
	# ----------------------------------------------------------------------
	Section("Continuing as cloud user...");
	Run("cp", $program, "/tmp/bootstrap.pl");
	Run("chmod", "a+x", "/tmp/bootstrap.pl");
	RunAdmin(asUser => 'cloud', cmd => ["/tmp/bootstrap.pl", @origOpts, "step2", StringifyProps("\a", "\a")]);

	# ----------------------------------------------------------------------
	# Copy the configured builder jarfile into the home directory of the current user.
	# ----------------------------------------------------------------------
	my $builderJar = "cloudcoderBuilder-v$version.jar";
	my $home = $ENV{'HOME'};
	my $user = WhoAmI();
	print "Copying configured builder jarfile into $home...\n";
	RunAdmin(cmd => ["cp", "/home/cloud/webapp/$builderJar", $home]);
	RunAdmin(cmd => ["chown", $user, "$builderJar"]);

	# ----------------------------------------------------------------------
	# Shut down mysqld if --no-start was used.
	# (Typically, this is because we're building a docker image.)
	# ----------------------------------------------------------------------
	if (exists $opts{'no-start'}) {
		RunAdmin(cmd => ["service", "mysql", "stop"]);
	}

	# ----------------------------------------------------------------------
	# Add support for integrated builder
	# ----------------------------------------------------------------------
	if ($features{'integrated-builder'}) {
		# Create builder user
		Section("Configuring integrated builder...");
		RunAdmin(
			cmd => [ 'adduser', '--disabled-password', '--home', '/home/builder', '--gecos', '', 'builder' ]
		);

		# Ensure that /home/builder is not world-readable
		RunAdmin(cmd => ['chmod', '0700', '/home/builder']);

		# Create builder directory,
		# copy builder jarfile to builder directory,
		# change ownership to builder user.
		RunAdmin(cmd => ["mkdir", "/home/builder/builder"]);
		RunAdmin(cmd => ["cp", $builderJar, "/home/builder/builder"]);
		RunAdmin(cmd => ["chown", "-R", "builder:builder", "/home/builder/builder"]);

		# Continue as builder user to configure builder jarfile.
		# This script should already be available as /tmp/bootstrap.pl.
		RunAdmin(
			asUser => 'builder',
			cmd => ['/tmp/bootstrap.pl', @origOpts, 'configure-builder', StringifyProps("\a", "\a")]
		);
	}

	# ----------------------------------------------------------------------
	# We're done!
	# ----------------------------------------------------------------------
	Section("CloudCoder installation successful!");

	my $webappRunning = YN(!exists $opts{'no-start'});
	my $apacheInstalled = YN($features{'apache'});
	my $apacheRunning = YN($apacheInstalled && (!exists $opts{'no-start'}));
	my $integratedBuilder = YN($features{'integrated-builder'});
	my $integratedBuilderRunning = YN($integratedBuilder && (!exists $opts{'no-start'}));
	my $localhostOnly = YN(!exists $opts{'no-localhost-only'});

	print <<"SUCCESS1";
It looks like CloudCoder was installed successfully!

Webapp running:             $webappRunning
Webapp localhost-only:      $localhostOnly
Apache installed:           $apacheInstalled
Apache running:             $apacheRunning
Integrated builder:         $integratedBuilder
Integrated builder running: $integratedBuilderRunning

If Apache is running, you should be able to connect to the
webapp at the following URL:

  https://$props{'ccHostname'}/cloudcoder

Note that Apache is configured to use a self-signed SSL certificate,
so you will see a scary SSL warning.  You should get a "real"
SSL certificate and install it using the instructions found
here:

  https://github.com/cloudcoderdotorg/CloudCoder/wiki/Postinstall

If you did not install Apache, then the webapp is listening
for unencrypted connections on port 8081.  However, if configured
as localhost-only (default, recommended) then external
connections are not allowed.  We recommend that you use a
proxy server supporting secure HTTP to connect to the webapp.

If an integrated builder was enabled, and Apache is enabled
and running, you should be able to try working on the demo
exercises.  Try it out!

If you didn't enable an integrated builder, then you can find
a configured builder app as the file $builderJar
in the $home directory.  Execute this on any Linux PC
with the command

  java -jar $builderJar start

to start an external builder.  You can run as many of these
as you need, depending on how many concurrent users you will
have.
SUCCESS1
}

# Step2 does all of the setup as the cloud user, specifically
# downloading and configuring the CloudCoder webapp and builder.
sub Step2 {
	# Complete the installation running as the cloud user
	my $whoami = WhoAmI();
	print "Step2: running as $whoami\n";
	chdir "/home/cloud" || die "Couldn't change directory to /home/cloud: $!\n";

	# Find out what the most recent release version is
	my $version = GetLatestVersion();
	my $appJar = "cloudcoderApp-v$version.jar";
	my $builderJar = "cloudcoderBuilder-v$version.jar";

	# Create webapp directory and change to it
	Run("mkdir", "-p", "webapp");
	chdir "webapp" || die "Couldn't change directory to webapp directory: $!\n";

	# ----------------------------------------------------------------------
	# Download webapp and builder distribution jarfiles
	# ----------------------------------------------------------------------

	# Download webapp and builder release jarfiles
	Section("Downloading $appJar and $builderJar...");
	Run("wget", "$DOWNLOAD_SITE/$appJar");
	Run("wget", "$DOWNLOAD_SITE/$builderJar");

	# ----------------------------------------------------------------------
	# Configure webapp distribution jarfile with
	# generated cloudcoder.properties and keystore
	# ----------------------------------------------------------------------
	Section("Configuring $appJar and $builderJar...");

	# Generate cloudcoder.properties for webapp.
	# Note that there is an interesting issue here:
	# the cloudcoder.submitsvc.oop.host property
	# (controlled by the ccHostname bootstrap property), which defines
	# the hostname to which the builders connect to reach the
	# webapp, needs to be resolvable *by the webapp*.
	# The reason for this is that I added some poorly thought
	# out code to reject connections from arbitrary hosts if non-SSL
	# connections are being used, such that the code only allows
	# connections originating from the same host
	# (as resolved via DNS, or from localhost.)  Unfortunately, when
	# I added this code, I didn't handle UnknownHostException in a
	# useful  way.  So, if the user specified a hostname that isn't
	# currently resolvable, *no builders  can connect*.  Since we
	# want the bootstrap script to support versions of the webapp with
	# this particular quirk, we need to work around the issue
	# here (as opposed to just fixing the code in the webapp).
	#
	# The solution, as seen here, is to specify this property
	# as localhost, guaranteed to be resolvable.
	#
	# Note that we *do* want the "correct" hostname to appear in the
	# cloudcoder.properties used by the "external" builder jarfile,
	# even if the hostname isn't resolvable right away. (Which is
	# typical, since you kind of need to know the webapp instance's
	# IP address in order to create a DNS entry for it :-)
	#
	# Also note that the integrated builder, if enabled, hard-codes
	# the webapp hostname as localhost, for obvious reasons.
	GenerateCloudCoderProperties('ccHostname' => 'localhost');

	# Generate keystore file.
	if (!exists $opts{'defer-keystore'}) {
		# Generate keystore for secure communication between webapp and builders
		GenerateKeystore();
	} else {
		# Create an invalid keystore: before running the webapp for the
		# first time, this script should be run again with 'generate-keystore'
		# as the mode.
		Run("touch keystore.jks");
	}

	# Configure webapp jarfile to use the generated cloudcoder.properties
	# and keystore
	print "Configuring $appJar...\n";
	Run("java", "-jar", $appJar, "configure",
		"--editJar=$appJar",
		"--replace=cloudcoder.properties=cloudcoder.properties",
		"--replace=war/WEB-INF/classes/keystore.jks=keystore.jks");

	# Regenerate cloudcoder.properties to have the "official" webapp
	# hostname.  (See above.)
	GenerateCloudCoderProperties();

	# Configure builder jarfile to use the same cloudcoder.properties
	# and keystore
	print "Configuring $builderJar...\n";
	Run("java", "-jar", $builderJar, "configure",
		"--editJar=$builderJar",
		"--replace=cloudcoder.properties=cloudcoder.properties",
		"--replace=keystore.jks=keystore.jks");

	Run('rm', '-f', 'cloudcoder.properties', 'keystore.jks');

	# ----------------------------------------------------------------------
	# Create the cloudcoderdb database
	# ----------------------------------------------------------------------
	Section("Creating cloudcoderdb database...");
	Run("java", "-jar", $appJar, "createdb", "--props=" . StringifyProps(',', '=') . ",ccRepoUrl=https://cloudcoder.org/repo");

	# ----------------------------------------------------------------------
	# Start the webapp!
	# ----------------------------------------------------------------------
	if (!exists $opts{'no-start'}) {
		Section("Starting the CloudCoder web application");
		Run("java", "-jar", $appJar, "start");
	}
}

# Generate and configure the keystore as a post-installation step.
# The docker entrypoint (dockerrun.pl) will do this as a
# one-time installation step before the webapp is started
# for the first time.  The script should be running as the
# "cloud" user.
sub GenerateAndConfigureKeystore {
	print "Generating and configuring keystore for secure webapp/builder communication...\n";
	die "generate-keystore should be run as the 'cloud' user\n" if (WhoAmI() ne 'cloud');

	# Determine webapp/builder jarfile names based on version
	my $version = GetLatestVersion();
	my $appJar = "cloudcoderApp-v$version.jar";
	my $builderJar = "cloudcoderBuilder-v$version.jar";

	# Change directory to the webapp home directory
	chdir "/home/cloud/webapp" || die "Couldn't chdir to /home/cloud/webapp: $!\n";

	# Generate the keystore
	GenerateKeystore();

	# Install the keystore in the webapp and builder jarfiles
	print "Adding keystore to $appJar...\n";
	Run("java", "-jar", $appJar, "configure",
		"--editJar=$appJar", "--replace=war/WEB-INF/classes/keystore.jks=keystore.jks");
	print "Adding keystore to $builderJar...\n";
	Run("java", "-jar", $builderJar, "configure",
		"--editJar=$builderJar", "--replace=keystore.jks=keystore.jks");
	Run('rm', '-f', 'keystore.jks');

	print "Keystore configuration was successful\n";
}

sub ConfigureIntegratedBuilder {
	my $user = WhoAmI();
	die "Should be run as builder user" if ($user ne 'builder');

	my $version = GetLatestVersion();
	my $builderJar = "cloudcoderBuilder-v$version.jar";

	chdir "/home/builder/builder" || die "Couldn't chdir to /home/builder/builder: $!\n";

	# Update cloudcoder.properties so that the builder uses just 1 thread,
	# and it connects to the webapp on localhost rather than the
	# external hostname.  Also, don't reveal the MySQL password
	# (although the root password is probably vulnerable, hmm.)
	GenerateCloudCoderProperties('ccNumBuilderThreads' => '1', 'ccHostname' => 'localhost',
		'ccMysqlCCPasswd' => 'ImSureYoudLikeToKnow');
	Run("java", "-jar", $builderJar, "configure", "--editJar=$builderJar",
		"--replace=cloudcoder.properties=cloudcoder.properties");

	# If --no-start was not specified, then we'll go ahead
	# and start the builder, since the webapp is already
	# running.
	if (!exists $opts{'no-start'}) {
		print "Starting integrated builder...\n";
		Run("java", "-jar", $builderJar, "start");
	}
}

sub YN {
	return $_[0] ? 'yes' : 'no';
}

sub GenerateKeystore {
	# Create a keystore
	print "Creating a keystore for communication between webapp and builder...\n";
	Run('keytool', '-genkey', '-noprompt',
		'-alias', 'cloudcoder',
		'-storepass', 'changeit',
		'-keystore', 'keystore.jks',
		'-validity', '3600',
		'-keypass', 'changeit',
		'-dname', "CN=None, OU=None, L=None, ST=None, C=None");
}

# Use Let's Encrypt to issue or renew an SSL certificate
sub LetsEncrypt {
	my $home = $ENV{'HOME'};

	# Paranoia
	chdir $home || die "Could not change directory to $home: $!\n";

	# Properties must be specified noninteractively
	if (!$propsLoaded) {
		die "Properties must be specified non-interactively\n" .
			"(E.g., use the --config option.)\n";
	}

	# Get the Let's Encrypt software if not already installed...
	if (! -d "$home/letsencrypt") {
		Run('git', 'clone', 'https://github.com/letsencrypt/letsencrypt');
	}

	# Generate the certificate
	RunAdmin(cmd => [
		"$home/letsencrypt/letsencrypt-auto",
		"-n", # non-interactive
		"--apache", # use Apache plugin for both authentication and cert installation
		"--renew-by-default", # renew 
		"--agree-tos",
		"-m", $props{'ccEmail'},
		"-d", $props{'ccHostname'}
	]);
}

sub Usage {
	print << "USAGE";
./bootstrap.pl [options] [mode [config props]]

Options:
  -n|--dry-run          Do a dry run without executing any commands
  -h|--help             Print usage information
  --enable=<features>   Enable specified features (comma-separated)
  --disable=<features>  Disable specified features (comma-separated)
  --config=<prop file>  Load configuration from specified properties file
                          (for noninteractive configuration)
  --no-start            Don't start the webapp (and if configured,
                          the integrated builder)
  --no-localhost-only   Allow webapp to accept unencrypted HTTP connections
                          from anywhere (not just localhost)
  --defer-keystore      Defer generation of keystore: webapp and builder
                          jarfiles will not have a keystore (and will not
                          be able to communicate): invoke later with
                          "generate-keystore" as the mode to generate
                          the keystore and add it to the webapp/builder
                          jarfiles

Modes:
  start                 Start an installation (this is the default)
  step2                 Continue an installation as 'cloud' user
                          (called automatically from start mode, don't
                          do this manually)
  generate-keystore     Generate keystore for webapp and builder
                          (needed only if --defer-keystore was used
                          during original installation)
  configure-builder     Continue an installation as 'builder' user
                          (called automatically from start mode, don't
                          do this manually)
  letsencrypt           Use letsencrypt to issue and configure, or renew,
                          an SSL certificate; note that config properties
                          must be specified noninteractively (e.g.,
                          using the --config option)

Selectable features are:
USAGE
	printf("%18s %8s %s\n", "Feature", "Default", "Description");
	print "-" x 18, " ", "-" x 8, " ", "-" x 40, "\n";
	for my $feature (sort keys %features) {
		printf("%18s %8s %s\n", $feature, $features{$feature} ? 'enabled' : 'disabled', $featuresHelp{$feature});
	}
	return 1;
}

# Encode %props as a string.
sub StringifyProps {
	my ($pairSep,$keyValSep) = @_;
	my $s = '';

	for my $key (sort keys %props) {
		if ($s ne '') {
			$s .= $pairSep;
		}
		$s .= "$key$keyValSep$props{$key}";
	}

	return $s;
}

# Decode a string containing %props: note that this is hard-coded
# to assume that \a (ASCII/Unicode BEL) is used as the separator.
sub UnstringifyProps {
	my ($s) = @_;
	return split(/\a/, $s);
}

# Load %props from a properties file
sub LoadConfigProperties {
	my ($fname) = @_;
	my $fh = new FileHandle("<$fname") || die "Couldn't open configuration properties file $fname: $!\n";
	while (<$fh>) {
		chomp;
		next if (/^\s*$/ || /^\s*#/);
		if (/^\s*([^=]+)\s*=\s*(.*)$/) {
			my $key = $1;
			my $val = $2;
			$val =~ s/\s+$//g; # trim trailing whitespace, if any
			$props{$key} = $val;
		}
	}
	$fh->close();
}

# Read %props interactively
sub ConfigureInteractively {
	print <<"GREET";
Welcome to the CloudCoder bootstrap script.

By running this script, you will create a basic CloudCoder
installation on a server running Ubuntu Linux.

Make sure to run this script from a user account that has
permission to run the "sudo" command.  If you see the
following prompt:

  sudo password>>

then you will need to type the account password and press
enter.  On some Ubuntu systems, such as Ubuntu server on
Amazon EC2, no password is required for sudo, so don't be
concerned if you don't see the prompt.
GREET
	
	my $readyToStart = Ask("\nReady to start? (yes/no)");
	exit 0 if ((lc $readyToStart) ne 'yes');
	
	print "\nFirst, please enter some configuration information...\n\n";
	
	# Get minimal required configuration information
	$props{'ccUser'} = Ask("What username do you want for your CloudCoder account?");
	$props{'ccPassword'} = Ask("What password do you want for your CloudCoder account?");
	$props{'ccFirstName'} = Ask("What is your first name?");
	$props{'ccLastName'} = Ask("What is your last name?");
	$props{'ccEmail'} = Ask("What is your email address?");
	$props{'ccWebsite'} = Ask("What is the URL of your personal website?");
	$props{'ccInstitutionName'} = Ask("What is the name of your institution?");
	$props{'ccMysqlRootPasswd'} = Ask("What password do you want for the MySQL root user?");
	$props{'ccMysqlCCPasswd'} = Ask("What password do you want for the MySQL cloudcoder user?");
	$props{'ccHostname'} = Ask("What is the hostname of this server?");

	print "\n";
	my $startInstall = Ask("Are you ready to start the installation? (yes/no)");
	exit 0 if ((lc $startInstall) ne 'yes');
}

# Write a cloudcoder.properties file in the current directory
# using information from %props as appropriate.
# Properties in the argument hash override the entries
# in %props.
sub GenerateCloudCoderProperties {
	my %overrides = @_;
	my %cprops = %props;
	foreach my $key (keys %overrides) {
		$cprops{$key} = $overrides{$key};
	}
	if (!exists $cprops{'ccNumBuilderThreads'}) {
		# Default to 2 builder threads, unless otherwise specified.
		$cprops{'ccNumBuilderThreads'} = 2;
	}
	print "Creating cloudcoder.properties...\n";
	my $localhostOnly = (exists $opts{'no-localhost-only'}) ? 'false' : 'true';
	my $pfh = new FileHandle(">cloudcoder.properties");
	print $pfh <<"ENDPROPERTIES";
cloudcoder.db.user=cloudcoder
cloudcoder.db.passwd=$cprops{'ccMysqlCCPasswd'}
cloudcoder.db.databaseName=cloudcoderdb
cloudcoder.db.host=localhost
cloudcoder.db.portStr=
cloudcoder.login.service=database
cloudcoder.submitsvc.oop.host=$cprops{'ccHostname'}
cloudcoder.submitsvc.oop.numThreads=$cprops{'ccNumBuilderThreads'}
cloudcoder.submitsvc.oop.port=47374
cloudcoder.submitsvc.oop.easysandbox.enable=true
cloudcoder.submitsvc.oop.easysandbox.heapsize=8388608
cloudcoder.submitsvc.ssl.cn=None
cloudcoder.submitsvc.ssl.keystore=keystore.jks
cloudcoder.submitsvc.ssl.keystore.password=changeit
cloudcoder.webserver.port=8081
cloudcoder.webserver.contextpath=/cloudcoder
cloudcoder.webserver.localhostonly=$localhostOnly
ENDPROPERTIES
	$pfh->close();
}

sub Ask {
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

sub Section {
	my ($name) = @_;
	print "\n";
	print "#" x 72, "\n";
	print " >>> $name <<<\n";
	print "#" x 72, "\n\n";
}

sub RunAdmin {
	my %params = @_;
	die "RunAdmin with no command\n" if (! exists $params{'cmd'});

	# Set environment variables (saving previous values)
	my %origEnv = ();
	if (exists $params{'env'}) {
		foreach my $var (keys %{$params{'env'}}) {
			my $val = $params{'env'}->{$var};
			$origEnv{$var} = $val;
			$ENV{$var} = $val;
		}
	}

	my @sudo = ('sudo', '-p', 'sudo password>> ');
	my @cmd;
	my $asUser = exists $params{'asUser'};
	if ($asUser) {
		@cmd = (@sudo, '-u', $params{'asUser'}, @{$params{'cmd'}});
	} else {
		@cmd = (@sudo, @{$params{'cmd'}});
	}

	my $result;
	if ($dryRun) {
		print "cmd: ", join(' ', @cmd), "\n";
		$result = 1;
	} else {
		print "Running admin command: ", join(' ', @cmd), "\n";
		$result = system(@cmd)/256 == 0;
	}

	# Restore previous values
	foreach my $var (keys %origEnv) {
		$ENV{$var} = $origEnv{$var};
	}

	if (!$result) {
		my $prog = $cmd[$asUser ? 5 : 3];
		die "Admin command $prog failed\n";
	}
}

sub Run {
	if ($dryRun) {
		print "cmd: ", join(' ', @_), "\n";
	} else {
		system(@_)/256 == 0 || die "Command $_[0] failed\n";
	}
}

sub FindMysqlVersion {
	my $fh = new FileHandle("apt-cache search mysql-server|");
	my $version;
	while (<$fh>) {
		chomp;
		if (/^mysql-server-(\d+(\.\d+)*)\s/) {
			$version = $1;
			last;
		}
	}
	$fh->close();

	die "Couldn't not find mysql version\n" if (!defined $version);
	return $version;
}

sub DebconfSetSelections {
	my ($package, $prop, $value) = @_;
	my $cmd = "echo '$package $prop $value' | sudo -p 'sudo password>> ' debconf-set-selections";
	if ($dryRun) {
		print "cmd: $cmd\n";
	} else {
		system($cmd)/256 == 0 || die "Couldn't run debconf-set-selections\n";
	}
}

sub EditApache2DefaultSsl {
	my ($ccHostname) = @_;

	# Determine Apache conf file name, and target sites-available target file names.
	my @apacheConfCandidates = (
		# Ubuntu server 12.04 LTS, other Debian?
		["/etc/apache2/sites-available/default-ssl", "/etc/apache2/sites-enabled/cloudcoder-ssl"],
		# Ubuntu server 14.04 LTS
		["/etc/apache2/sites-available/default-ssl.conf", "/etc/apache2/sites-enabled/001-cloudcoder-ssl.conf"],
	);
	my $pair;
	foreach my $candidate (@apacheConfCandidates) {
		if (-e $candidate->[0]) {
			$pair = $candidate;
			last;
		}
	}
	die "Could not find Apache configuration file!" if (! defined $pair);
	my $apacheConf = $pair->[0];
	my $targetConf = $pair->[1];

	# Edit Apache SSL conf file to add hostname
	# and transparent proxy support for CloudCoder webapp
	my $in = new FileHandle("<$apacheConf");
	my $out = new FileHandle(">/tmp/default-ssl-modified");

	my $alreadyModified = 0;
	my $modCount = 0;

	while (<$in>) {
		chomp;
		print $out "$_\n";
		if (/^\s*<VirtualHost/) {
			print $out <<"ENDSERVERNAME";
	# Modified by CloudCoder bootstrap.pl
	ServerName $ccHostname
ENDSERVERNAME
			$modCount++;
		} elsif (/^\s*ServerAdmin/) {
			print $out <<"ENDPROXY";
	
	# Transparently proxy requests for /cloudcoder to the
	# CloudCoder Jetty server
	ProxyPass /cloudcoder http://localhost:8081/cloudcoder
	ProxyPassReverse /cloudcoder http://localhost:8081/cloudcoder
	<Proxy http://localhost:8081/cloudcoder>
		Order Allow,Deny
		Allow from all
	</Proxy>
ENDPROXY
			$modCount++;
		} elsif (/^\s*# Modified by CloudCoder/) {
			$alreadyModified = 1;
		}
	}
	$in->close();
	$out->close();

	if ($alreadyModified) {
		print "$apacheConf Already modified?\n";
		return;
	}

	if ($modCount != 2) {
		die "$apacheConf is not in expected format\n";
	}

	RunAdmin(cmd => ['cp', '/tmp/default-ssl-modified', $apacheConf]);
	RunAdmin(cmd => ['ln', '-s', $apacheConf, $targetConf]);
}

sub GetLatestVersion {
	my $versionFileDir = "/usr/local/share/cloudcoder";
	my $versionFile = "$versionFileDir/CLOUDCODER_VERSION";

	if (! -r $versionFile) {
		# Save the CloudCoder version number in a file called
		# /usr/local/share/cloudcoder/CLOUDCODER_VERSION.
		# It's not a good idea to fetch the version dynamically every time,
		# since it could change if a CloudCoder release happens while an installation
		# is underway.  We assume that the first time the version file
		# is written, this script will be running as a sudo-capable user,
		# and that subsequent times the file will exist and be world-readable.
		system("sudo -p 'sudo password>> ' mkdir -p $versionFileDir")/256 == 0
			|| die "Couldn't make $versionFileDir directory\n";
		system("sudo -p 'sudo password>> ' chmod 0755 $versionFileDir")/256 == 0
			|| die "Couldn't make $versionFileDir world-readable\n";
		system("sudo -p 'sudo password>> ' wget --quiet --output-document=$versionFile $DOWNLOAD_SITE/LATEST")/256 == 0
			|| die "Couldn't determine latest CloudCoder version\n";
		system("sudo -p 'sudo password>> ' chmod 644 $versionFile")/256 == 0
			|| die "Couldn't make $versionFile world-readable\n";
	}

	my $fh = new FileHandle("<$versionFile");
	my $version;
	while (<$fh>) {
		if (/^\s*(\d+(\.\d+)*)\s*$/ || /^\s*v(\d+(\.\d+)*)\s*$/) {
			$version = $1;
			last;
		}
	}
	$fh->close();
	die "Could not determine latest CloudCoder release version\n" if (!defined $version);
	return $version;
}

sub WhoAmI {
	my $whoami = `whoami`;
	chomp $whoami;
	return $whoami;
}

# vim:ts=2:
