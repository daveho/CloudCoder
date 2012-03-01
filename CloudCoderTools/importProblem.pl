#! /usr/bin/perl -w

# Script to import a CloudCoder problem from an XML file.
# Assumes that the classes in the CloudCoderImporter project
# have been built.

use strict;
use FileHandle;

if (scalar(@ARGV) != 2) {
	print STDERR "Usage: ./importProblem.pl <problem xml> <course id>\n";
	exit 1;
}

if (! -r '../local.properties') {
	print STDERR "../local.properties does not exist: do you run configure.pl?\n";
	exit 1;
}

my $problemXml = shift @ARGV;
my $courseId = shift @ARGV;

my @classpath = ();

push @classpath, "./bin";
addAllJarsInDir("./lib");
push @classpath, "../CloudCoder/war/WEB-INF/classes";
addAllJarsInDir('../CloudCoder/war/WEB-INF/lib');

#print "classpath=", join(':', @classpath), "\n";

system("java", "-classpath", join(':', @classpath), 'org.cloudcoder.importer.ImportProblem', '../local.properties', $problemXml, $courseId)/256 == 0
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
