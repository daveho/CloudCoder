#! /usr/bin/perl -w

use strict;
use FileHandle;
use Getopt::Long qw{:config bundling no_ignore_case no_auto_abbrev};

# Bootstrap CloudCoder on an Ubuntu server

####################################################################
# Global data
####################################################################

# Selectable features (all are enabled by default)
my %features = (
	'apache' => 1,
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

# See if any features are being disabled
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
	my @packages = ("wget", "fastjar", "openjdk-7-jre-headless", "mysql-client-$mysqlVersion", "mysql-server-$mysqlVersion");
	if ($features{'apache'}) {
		push @packages, 'apache2';
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
	# We're done!
	# ----------------------------------------------------------------------
	Section("CloudCoder installation successful!");
	print <<"SUCCESS1";
It looks like CloudCoder was installed successfully.
SUCCESS1

	# If apache was installed, the webapp should be reachable
	# via https.  Otherwise, only unencrypted HTTP on port
	# 8081 is available.
	if ($features{'apache'}) {
		print <<"SUCCESS2a";

You should be able to test your new installation by opening the
following web page:

  https://$props{'ccHostname'}/cloudcoder
SUCCESS2a
	} else {
		print <<"SUCCESS2b";

You did not install apache (for SSL support).
CloudCoder is listening for unencrypted connections on
port 8081, on localhost only (so connections from outside
will not be accepted.)  You should use a proxy server
supporting secure HTTP to make CloudCoder publicly
reachable.
SUCCESS2b
	}

	print <<"SUCCESS3";

Note that no builders are running, so you won't be able to
test submissions yet.  The builder jar file ($builderJar)
is in the $home directory: you will need to copy
it to the server(s) which will be responsible for building
and testing submissions.
SUCCESS3
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

	# Generate cloudcoder.properties
	print "Creating cloudcoder.properties...\n";
	my $localhostOnly = (exists $opts{'no-localhost-only'}) ? 'false' : 'true';
	my $pfh = new FileHandle(">cloudcoder.properties");
	print $pfh <<"ENDPROPERTIES";
cloudcoder.db.user=cloudcoder
cloudcoder.db.passwd=$props{'ccMysqlCCPasswd'}
cloudcoder.db.databaseName=cloudcoderdb
cloudcoder.db.host=localhost
cloudcoder.db.portStr=
cloudcoder.login.service=database
cloudcoder.submitsvc.oop.host=$props{'ccHostname'}
cloudcoder.submitsvc.oop.numThreads=2
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

sub Usage {
	print << "USAGE";
./bootstrap.pl [options] [mode [config props]]

Options:
  -n|--dry-run          Do a dry run without executing any commands
  -h|--help             Print usage information
  --disable=<features>  Disable specified features (comma-separated)
  --config=<prop file>  Load configuration from specified properties file
                          (for noninteractive configuration)
  --no-start            Don't start the webapp
  --no-localhost-only   Allow webapp to accept unencrypted HTTP connections
                          from anywhere (not just localhost)
  --defer-keystore      Defer generation of keystore: webapp and builder
                          jarfiles will not have a keystore (and will not
                          be able to communicate): invoke later with
                          "generate-keystore" as the mode to generate
                          the keystore and add it to the webapp/builder
                          jarfiles

Selectable features (all enabled by default) are:
USAGE
	for my $feature (sort keys %features) {
		print "  $feature\n";
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
		system("mkdir -p $versionFileDir")/256 == 0
			|| die "Couldn't make $versionFileDir directory\n";
		system("chmod 0755 $versionFileDir")/256 == 0
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
