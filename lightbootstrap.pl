#! /usr/bin/perl

# Just install Debian/Ubuntu packages needed to run the CloudCoder webapp.
# Also:
# - creates the cloudcoder database user and grants permission on cloudcoderdb

my $dryRun = 1;
my $ccdbpasswd = 'xyz'; # change this

Run('sudo apt-get install openjdk-7-jre-headless mysql-client mysql-server apache2 unzip');

Run('sudo a2enmod proxy');
Run('sudo a2enmod proxy_http');
Run('sudo a2enmod ssl');

print "Creating cloudcoder db user account...\n";
Run("mysql --user=root --pass --execute=\"create user 'cloudcoder'\@'localhost' identified by '$ccdbpasswd'\"");
Run("mysql --user=root --pass --execute=\"grant all on cloudcoderdb.* to 'cloudcoder'\@'localhost'\"");

sub Run {
	if ($dryRun) {
		print "cmd: ", join(' ', @_), "\n";
	} else {
		system(@_)/256 == 0 || die "Command $_[0] failed\n";
	}
}


# vim:ts=2:
