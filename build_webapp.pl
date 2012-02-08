#! /usr/bin/perl -w

use strict;

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
system("./configure_webapp.pl")/256 == 0 || die "Couldn't configure the webapp\n";

print "==> Done!\n";

