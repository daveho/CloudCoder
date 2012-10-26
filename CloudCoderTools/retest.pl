#! /usr/bin/perl -w

# Script to retest a submission.

use strict;
use FileHandle;

if (scalar(@ARGV) != 1) {
	print STDERR "Usage: ./retest.pl (<submission receipt id> | @<file with list of ids>)\n";
	exit 1;
}

if (! -r '../cloudcoder.properties') {
	print STDERR "../cloudcoder.properties does not exist: do you run configure.pl?\n";
	exit 1;
}

my $submissionReceiptId = shift @ARGV;

my @classpath = ();

push @classpath, "./bin";
addAllJarsInDir("./lib");
push @classpath, "../CloudCoder/war/WEB-INF/classes";
addAllJarsInDir('../CloudCoder/war/WEB-INF/lib');

#print "classpath=", join(':', @classpath), "\n";

system("java", "-classpath", join(':', @classpath), 'org.cloudcoder.importer.ReTest', '../cloudcoder.properties', $submissionReceiptId)/256 == 0
	|| die "Failed";

sub addAllJarsInDir {
	my ($dir) = @_;
	my $find_fh = new FileHandle("(find '$dir' -name '*\\.jar' -print) |") || die;
	while(<$find_fh>) {
		chomp;
		push @classpath, $_;
	}
	$find_fh->close();
}
