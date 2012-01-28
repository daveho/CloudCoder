#! /usr/bin/perl -w

use strict;
use FileHandle;

# Script to collect all configuration information needed to
# build and deploy the CloudCoder webapp and/or Builder.
# Generates a local.properties file.

my @propertyNames = ();
my %properties = ();

print "Welcome to the CloudCoder configuration script!\n";
print "Please enter the information needed to configure CloudCoder for your system.\n";
print "You can just hit enter to accept the default value (if there is one).\n";
print "\n";

askprop("Where is your GWT SDK installed (the directory with webAppCreator in it)?",
	"gwt.sdk", undef);
askprop("What MySQL username will the webapp use to connect to the database?",
	"cloudcoder.db.user", undef);
askprop("What MySQL password will the webapp use to connect to the database?",
	"cloudcoder.db.passwd", undef);
askprop("What MySQL database will contain the CloudCoder tables?",
	"cloudcoder.db.databaseName", "cloudcoder");
askprop("What host will CloudCoder connect to to access the MySQL database?",
	"cloudcoder.db.host", "localhost");
askprop("If MySQL is running on a non-standard port, enter :XXXX (e.g, :8889 for MAMP).\n" .
	"Just hit enter if MySQL is running on the standard port.",
	"cloudcoder.db.portStr", undef);
askprop("What host will the CloudCoder webapp be running on?\n" .
	"This information is only needed for the Builder, just hit enter\n" .
	"if you're only configuring the webapp, or if the Builder will run\n" .
	"on the same host as the webapp.",
	"cloudcoder.submitsvc.oop.host", "localhost");
askprop("What port will the CloudCoder webapp use to listen for connections from\n" .
	"Builders?",
	"cloudcoder.submitsvc.oop.port", "47374");

my $confirm = ask("Write configuration file (local.properties)?", "yes");
if ((lc $confirm) ne 'yes') {
	print "Properties not written\n";
	exit 1;
}

print "Writing properties...";
STDOUT->flush();
my $fh = new FileHandle(">local.properties");
foreach my $property (@propertyNames) {
	print $fh "$property=$properties{$property}\n";
}
$fh->close();
print "Done!\n";

sub ask {
	my ($question, $defval) = @_;

	print "$question\n";
	if (defined $defval) {
		print "[default: $defval] ";
	}
	print "==> ";

	my $value = <>;
	chomp $value;

	if ((defined $defval) && $value =~ /^\s*$/) {
		$value = $defval;
	}

	return $value;
}

sub askprop {
	my ($question, $property, $defval) = @_;

	my $value = ask($question, $defval);

	push @propertyNames, $property;
	$properties{$property} = $value;

	print "\n";
}

