#! /usr/bin/perl -w

use strict;
use FileHandle;

# Read configuration from local.properties
my %properties = ();
my $prop_fh = new FileHandle("<local.properties") || die "Couldn't open local.properties\n";
while (<$prop_fh>) {
	chomp;
	if (/([^=]+)=(.*)$/) {
		$properties{$1} = $2;
	}
}
$prop_fh->close();

print "==> Building CloudCoder webapp...\n";
system("cd CloudCoder && ant clean && ant build")/256 == 0
	|| die "Couldn't build webapp\n";

print "==> Building CloudCoderWebServer...\n";
system("cd CloudCoderWebServer && ant clean && ant build")/256 == 0
	|| die "Couldn't build CloudCoderWebServer\n";

print "==> Copying webapp...\n";
system("cd CloudCoder && ./copy_webapp.sh")/256 == 0
	|| die "Coudln't copy the webapp\n";

print "==> Configuring the webapp\n";
my $webxml_in = new FileHandle("<CloudCoder/war/WEB-INF/web.xml") || die;
my $webxml_out = new FileHandle(">CloudCoderWebServer/apps/cloudCoder/WEB-INF/web.xml") || die;
while (<$webxml_in>) {
	chomp;
	my $line = $_;
	if (m,^\s*\<param-name\>(.*)\</param-name\>,) {
		if (exists $properties{$1}) {
			print "Setting $1 property...\n";
			my $value = $properties{$1};
			$line = "    <param-name>$1</param-name><param-value>$value</param-value>";
		}
	}
	print $webxml_out "$line\n";
}
$webxml_out->close();
$webxml_in->close();

print "==> Done!\n";

