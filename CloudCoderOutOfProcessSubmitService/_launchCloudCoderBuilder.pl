#! /usr/bin/perl -w

# Launch CloudCoder Builder from the command line.
# The server will listen on a FIFO for a shutdown command.
# (See shutdown.sh.)

use strict qw(refs vars);
use FileHandle;

my $MAIN_CLASS = "org.cloudcoder.submitsvc.oop.builder.Builder";

my $app = 'cloudCoderBuilder';

my @cp_entries = ();

# Recursively find all jar files
my $find_fh = new FileHandle("(find lib -name '*\\.jar' -print) |") || die;
while(<$find_fh>) {
	chomp;
	push @cp_entries, $_;
}

# Add war/WEB-INF/classes directory from CloudCoder to classpath,
# to get classes shared between the webapp and the builder.
push @cp_entries, "../CloudCoder/war/WEB-INF/classes";

my $classpath = join(':', @cp_entries);
print "$classpath\n" if (exists $ENV{'DEBUG'});

# Create pidfile
my $pid = $$;
system("echo '$pid' > $app.pid")/256 == 0 || die;

# Create administrative FIFO
my $fifo = "$app-$pid.fifo";
system("mkfifo '$fifo'")/256 == 0 || die;

# Launch the application
my $cmd = "java -classpath bin:'$classpath' -D$app.fifo='$fifo' $MAIN_CLASS";
# Append command line arguments, if any
if (scalar(@ARGV) > 0) {
	$cmd .= " ";
	$cmd .= join(' ', @ARGV);
}
print "cmd: $cmd\n" ; #if (exists $ENV{'DEBUG'});
exec($cmd) || die;
