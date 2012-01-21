#! /usr/bin/perl

# Perl script that ProcessRunner uses to run a subprocess
# and determine its final status (failed to execute, terminated
# by a signal, or exited normally).

# It is not possible to determine if a process was terminated by
# a signal in Java, hence the need for this helper script.

use FileHandle;

my @cmdArgs = @ARGV;
die if (scalar(@cmdArgs) < 1);

# Get the file that the process exit status should be written to.
my $procStatFile = $ENV{'CC_PROC_STAT_FILE'};

my $rc = system(@cmdArgs);

my $fh = new FileHandle(">$procStatFile") || die;

if ($rc == -1) {
	print $fh "failed_to_execute\n";
	print $fh "-1\n";
} elsif ($rc == & 127) {
	my $signum = ($rc & 127);
	print $fh "terminated_by_signal\n";
	print $fh "$signum\n";
} else {
	my $exitCode = ($rc >> 8);
	print $fh "exited\n";
	print $fh "$exitCode\n";
}
$fh->flush();
$fh->close();
