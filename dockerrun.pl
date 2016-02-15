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

# Start mysqld and CloudCoder.
Run("service", "mysql", "start");
Run("sudo", "-u", "cloud", "/bin/bash", "-c",
	"cd /home/cloud/webapp && java -jar cloudcoderApp-v*.jar start");

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
	"cd /home/cloud/webapp && java -jar cloudcoderApp-v*.jar shutdown");
Run("service", "mysql", "stop");

print "Done\n";

sub SigtermHandler {
	$done = 1;
}

sub Run {
	system(@_)/256 == 0 || die "Command $_[0] failed\n";
}

# vim:ts=2:
