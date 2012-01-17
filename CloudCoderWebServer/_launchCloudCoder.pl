#! /usr/bin/perl -w

# Launch CloudCoder from the command line using a Jetty server.
# The server will listen on a FIFO for a shutdown command.
# (See shutdown.sh.)

use strict qw(refs vars);
use FileHandle;

my $MAIN_CLASS = "org.cloudcoder.webserver.CloudCoderWebServer";

my $app = 'cloudCoder';

# Set the classpath by recursively finding all jar files
my @jars = ();
my $find_fh = new FileHandle("(find lib -name '*\\.jar' -print) |") || die;
while(<$find_fh>) {
	chomp;
	push @jars, $_;
}
my $classpath = join(':', @jars);
print "$classpath\n" if (exists $ENV{'DEBUG'});

# Create pidfile
my $pid = $$;
system("echo '$pid' > $app.pid")/256 == 0 || die;

# Create administrative FIFO
my $fifo = "$app-$pid.fifo";
system("mkfifo '$fifo'")/256 == 0 || die;

# Launch the application, passing through any command line arguments
my $cmd = "java -classpath bin:'$classpath' -D$app.fifo='$fifo' $MAIN_CLASS";
if (scalar(@ARGV) > 0) {
	$cmd .= ' ';
	$cmd .= join(' ', @ARGV);
}
print "cmd: $cmd\n" if (exists $ENV{'DEBUG'});
exec($cmd) || die;
