#! /usr/bin/perl -w

use strict;
use FileHandle;

# Bootstrap CloudCoder on an Ubuntu server

my $program = $0;
#print "program=$program\n";
#exit 0;

my $mode = 'start';

if (scalar(@ARGV) > 0) {
	$mode = shift @ARGV;
}

if ($mode eq 'start') {
	Start();
} elsif ($mode eq 'step2') {
	Step2();
} else {
	die "Unknown mode: $mode\n";
}

sub Start {
	print <<"GREET";
Welcome to the CloudCoder bootstrap script.

By running this script, you will create a basic CloudCoder
installation on a server running Ubuntu Linux.

Make sure to run this script from a user account that has
permission to run the "sudo" command.  If you see the
following prompt:

  sudo password>>

then you will need to type the account password and press
enter.
GREET
	
	my $readyToStart = ask("\nReady to start? (yes/no)");
	exit 0 if ((lc $readyToStart) ne 'yes');
	
	print "\nFirst, please enter some configuration information...\n\n";
	
	# Get minimal required configuration information
	my $ccUser = ask("What username do you want for your CloudCoder account?");
	my $ccPasswd = ask("What password do you want for your CloudCoder account?");
	my $ccFirstName = ask("What is your first name?");
	my $ccLastName = ask("What is your last name?");
	my $ccEmail = ask("What is your email address?");
	my $ccWebsite = ask("What is the URL of your personal website?");
	my $ccMysqlRootPasswd = ask("What password do you want for the MySQL root user?");
	my $ccMysqlCCPasswd = ask("What password do you want for the MySQL cloudcoder user?");
	my $ccHostname = ask("What is the hostname of this server?");
	
	# Install/configure required packages
	print "\n";
	section("Installing required packages");
	RunAdmin(
		env => { 'DEBIAN_FRONTEND' => 'noninteractive' },
		cmd => ["apt-get", "update"]
	);
	RunAdmin(
		env => { 'DEBIAN_FRONTEND' => 'noninteractive' },
		cmd => ["apt-get", "-y", "install", "openjdk-6-jdk", "mysql-client", "mysql-server", "apache2"]
	);
	RunAdmin(cmd => ["mysqladmin", "-u", "root", "password", $ccMysqlRootPasswd]);
	
	# Configure MySQL
	print "\n";
	section("Configuring MySQL");
	Run("mysql", "--user=root", "--pass=$ccMysqlRootPasswd",
		"--execute=create user 'cloudcoder'\@'localhost' identified by '$ccMysqlCCPasswd'");
	Run("mysql", "--user=root", "--pass=$ccMysqlRootPasswd",
		"--execute=grant all on cloudcoderdb.* to 'cloudcoder'\@'localhost'");
	
	# Create cloud user
	RunAdmin(
		cmd => [ 'adduser', '--disabled-password', '--home', '/home/cloud', '--gecos', '', 'cloud' ]
	);

	# Configure apache2
	RunAdmin(cmd => ['a2enmod', 'proxy']);
	RunAdmin(cmd => ['a2enmod', 'proxy_http']);

	# Continue as the cloud user to complete the installation
	# TODO
}

sub Step2 {
	# Complete the installation running as the cloud user
}

sub ask {
	my ($question, $defval) = @_;

	print "$question\n";
	if (defined $defval) {
		print "[default: $defval] ";
	}
	print "==> ";

	my $value = <STDIN>;
	chomp $value;

	if ((defined $defval) && $value =~ /^\s*$/) {
		$value = $defval;
	}

	return $value;
}

sub section {
	my ($name) = @_;
	print "#" x 72, "\n";
	print " >>> $name <<<\n";
	print "#" x 72, "\n\n";
}

sub RunAdmin {
	my %params = @_;
	die "RunAdmin with no command\n" if (! exists $params{'cmd'});

	# Set environment variables (saving previous values)
	my %origEnv = ();
	if (exists $params{'env'}) {
		foreach my $var (keys %{$params{'env'}}) {
			my $val = $params{'env'}->{$var};
			$origEnv{$var} = $val;
			$ENV{$var} = $val;
		}
	}

	my @cmd = ('sudo', '-p', 'sudo password>> ', @{$params{'cmd'}});

	print "cmd: ", join(' ', @cmd), "\n";
	#my $result = system(@cmd)/256 == 0;
	my $result = 1;

	# Restore previous values
	foreach my $var (keys %origEnv) {
		$ENV{$var} = $origEnv{$var};
	}

	die "Admin command $cmd[3] failed\n" if (!$result);
}

sub Run {
#	system(@_)/256 == 0 || die "Command $_[0] failed\n";
	print "cmd: ", join(' ', @_), "\n";
}
