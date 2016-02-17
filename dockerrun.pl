#! /usr/bin/perl -w

use strict;
use POSIX qw(pause);
use IO::Handle;

STDOUT->autoflush(1);
STDERR->autoflush(1);

print "Starting CloudCoder docker container...\n";

# Run CloudCoder (and mysql) in Docker, attempting to shut
# both down gracefully when a SIGTERM is received.

my $done = 0;

$SIG{TERM} = \&SigtermHandler;

my $version = `cat /usr/local/share/cloudcoder/CLOUDCODER_VERSION`;
chomp $version;

CheckKeystore();

# Start mysqld and CloudCoder.
Run("service", "mysql", "start");
Run("sudo", "-u", "cloud", "/bin/bash", "-c",
	"cd /home/cloud/webapp && java -jar cloudcoderApp-$version.jar start");

print "mysqld and CloudCoder webapp are running...\n";

# Wait for SIGTERM.
# Note that there is a race here if SIGTERM arrives
# after $done is checked but before pause() is executed.
if (!$done) {
	pause();
}
print "SIGTERM received, shutting down...\n";

# Shut down CloudCoder and mysqld.
Run("sudo", "-u", "cloud", "/bin/bash", "-c",
	"cd /home/cloud/webapp && java -jar cloudcoderApp-$version.jar shutdown");
Run("service", "mysql", "stop");

print "Done\n";

sub SigtermHandler {
	$done = 1;
}

sub Run {
	system(@_)/256 == 0 || die "Command $_[0] failed\n";
}

# Check the CloudCoder webapp jarfile to see if a keystore
# has been configured.  If not, generate one and add it
# to the webapp and builder jarfiles.
sub CheckKeystore {
	my $keystoreSize = `fastjar tvf /home/cloud/webapp/cloudcoderApp-$version.jar | grep '/keystore\.jks' | tr -s ' ' | sed -e 's/^ *//' |  cut -d ' ' -f 1`;
	chomp $keystoreSize;
	if ($keystoreSize == 0) {
		print "No keystore found in webapp/builder jarfiles, generating them.\n";
		print "This is expected when CloudCoder is run (in a Docker container)\n";
		print "for the first time.\n";
		Run("cp", "bootstrap.pl", "/tmp");
		Run("chmod", "755", "/tmp/bootstrap.pl");
		Run("sudo", "-u", "cloud", "/tmp/bootstrap.pl", "generate-keystore");
	}
}

# vim:ts=2:
