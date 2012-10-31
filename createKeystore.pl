#! /usr/bin/perl

use strict;
use FileHandle;

my $keystore = shift @ARGV || die;
my $passwd = shift @ARGV || die;
my $commonName = shift @ARGV || die;

print "Creating public/private keypair for secure communication between the builder and the server...";
STDOUT->flush();

run('rm', '-f', $keystore);
run('keytool', '-genkey', '-noprompt',
	'-alias', 'cloudcoder',
	'-storepass', $passwd,
	'-keystore', $keystore,
	'-validity', '3600',
	'-keypass', $passwd,
	'-dname', "CN=$commonName, OU=None, L=None, ST=None, C=None");
print "Done!\n";

sub run {
	system(@_)/256 == 0 || die "Command $_[0] failed\n";
}
