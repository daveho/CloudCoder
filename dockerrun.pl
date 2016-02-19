#! /usr/bin/perl -w

# Run CloudCoder, and mysql, and apache2 in Docker, attempting to
# shut each down gracefully when a SIGTERM is received.

use strict;
use POSIX qw(pause);
use IO::Handle;

my $DATA_DIR = "/usr/local/share/cloudcoder";

STDOUT->autoflush(1);
STDERR->autoflush(1);

# ----------------------------------------------------------------------
# Main script
# ----------------------------------------------------------------------

print "Starting CloudCoder docker container...\n";

my $done = 0;

$SIG{TERM} = \&SigtermHandler;

my $version = `cat $DATA_DIR/CLOUDCODER_VERSION`;
chomp $version;

# Check to see whether a keystore has been generated for
# webapp/builder communiction.
CheckKeystore();

# Check to see whether we are still using the default
# initial snakeoil SSL cert, and if so, generate a
# new one.
CheckSnakeoilSSL();

# Start mysqld, CloudCoder, and apache2.
Run("service", "mysql", "start");
Run("sudo", "-u", "cloud", "/bin/bash", "-c",
	"cd /home/cloud/webapp && java -jar cloudcoderApp-$version.jar start");
Run("service", "apache2", "start");

# Start integrated CloudCoder builder
Run("sudo", "-u", "builder", "/bin/bash", "-c",
	"cd /home/builder/builder && java -jar cloudcoderBuilder-$version.jar start");

print "mysqld, CloudCoder webapp, apache2, and integrated CloudCoder builder are running...\n";

# Wait for SIGTERM.
# Note that there is a race here if SIGTERM arrives
# after $done is checked but before pause() is executed.
if (!$done) {
	pause();
}
print "SIGTERM received, shutting down...\n";

# Shut down integrated CloudCoder builder
Run("sudo", "-u", "builder", "/bin/bash", "-c",
	"cd /home/builder/builder && java -jar cloudcoderBuilder-$version.jar shutdown");

# Shut down apache2, CloudCoder webapp, and mysqld.
Run("service", "apache2", "stop");
Run("sudo", "-u", "cloud", "/bin/bash", "-c",
	"cd /home/cloud/webapp && java -jar cloudcoderApp-$version.jar shutdown");
Run("service", "mysql", "stop");

print "Done\n";

# ----------------------------------------------------------------------
# Subroutines
# ----------------------------------------------------------------------

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
	print "Checking keystore...\n";
	my $keystoreSize = `fastjar tvf /home/cloud/webapp/cloudcoderApp-$version.jar | grep '/keystore\.jks' | tr -s ' ' | sed -e 's/^ *//' |  cut -d ' ' -f 1`;
	chomp $keystoreSize;
	print "Keystore size is $keystoreSize\n";
	if (!($keystoreSize =~ /^\d+$/) || $keystoreSize == 0) {
		print "No keystore found in webapp/builder jarfiles, generating them.\n";
		print "This is expected when CloudCoder is run (in a Docker container)\n";
		print "for the first time.\n";
		Run("cp", "bootstrap.pl", "/tmp");
		Run("chmod", "755", "/tmp/bootstrap.pl");
		Run("sudo", "-u", "cloud", "/tmp/bootstrap.pl", "generate-keystore");
	}
}

# Check to see whether the initial "snakeoil" SSL certificate
# needs to be regenerated.  It would be really bad for anyone
# to use the one that comes with the image, since the
# private key used for the initial one would then be known.
sub CheckSnakeoilSSL {
	print "Checking snakeoil SSL cert...\n";
	if (! -e "$DATA_DIR/SNAKEOIL_CERT_GENERATED") {
		print "Regenerating snakeoil cert...\n";
		$ENV{'DEBIAN_FRONTEND'} = 'noninteractive';
		Run("make-ssl-cert", "generate-default-snakeoil", "--force-overwrite");
		Run("touch", "$DATA_DIR/SNAKEOIL_CERT_GENERATED");
	}
}

# vim:ts=2:
