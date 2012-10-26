#! /usr/bin/perl -w

# Script to test batches of programs against a CloudCoder
# problem specified in an XML file, and print the test results
# in a simple plain text format.

use strict;
use FileHandle;

if (scalar(@ARGV) != 2) {
	print STDERR "Usage: ./batchTester.pl <problem xml> <file with list of program filenames>\n";
	exit 1;
}

my $problemXml = shift @ARGV;
my $fileNameList = shift @ARGV;

my @classpath = ();

push @classpath, "./bin";
addAllJarsInDir("./lib");
push @classpath, "../CloudCoder/war/WEB-INF/classes";
addAllJarsInDir('../CloudCoder/war/WEB-INF/lib');

#print "classpath=", join(':', @classpath), "\n";

system("java", "-classpath", join(':', @classpath), 'org.cloudcoder.importer.BatchTester', $problemXml, $fileNameList)/256 == 0
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
