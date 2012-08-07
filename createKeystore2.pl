#! /usr/bin/perl

use strict;
use FileHandle;

my $PASSWD = 'changeit';

eval {
	msg("Creating keys/certificates for mutual authentication between builders and webapp\n");
	createKeyAndCert('webapp');
	createKeyAndCert('builder');
	
	msg("Importing builder cert into webapp keystore...");
	importCert('webapp', 'builder');
	msg("done\n");
	
	msg("Importing webapp cert into builder keystore...");
	importCert('builder', 'webapp');
	msg("done\n");
};
run('rm', '-f', "/tmp/webapp$$.cer");
run('rm', '-f', "/tmp/builder$$.cer");

sub run {
	system(@_)/256 == 0 || die "Command $_[0] failed\n";
}

sub redirect {
	my $outfile = pop @_;
	system(join(' ', @_) . "> $outfile")/256 == 0 || die "Command $_[0] failed\n";
}

sub msg {
	print $_[0];
	STDOUT->flush();
}

sub createKeyAndCert {
	my ($name) = @_;

	my $keystore = "${name}keystore.jks";
	my $keyAlias = "${name}key";

	msg("Creating ${name} private key...");
	
	run('rm', '-f', $keystore);
	run('keytool', '-genkey', '-noprompt',
		'-alias', $keyAlias,
		'-storepass', $PASSWD,
		'-keystore', $keystore,
		'-validity', '3600',
		'-keypass', $PASSWD,
		'-dname', "CN=None, OU=None, L=None, ST=None, C=None");
	msg("done\n");
	
	msg("Creating certificate from ${name} key...");
	redirect('keytool', '-exportcert', '-rfc',
		'-alias', $keyAlias,
		'-keystore', $keystore,
		'-storepass', $PASSWD,
		'-keypass', $PASSWD,
		"/tmp/${name}$$.cer");
	msg("done\n");
}

sub importCert {
	my ($keystoreName, $certName) = @_;

	run('keytool', '-noprompt', '-import',
		'-alias', "${certName}key",
		'-file', "/tmp/${certName}$$.cer",
		'-keystore', "${keystoreName}keystore.jks",
		'-storepass', 'changeit',
		'-keypass', 'changeit');
}
