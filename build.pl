#! /usr/bin/perl -w

use strict;

print "==> Building CloudCoderWebServer...\n";
system("cd CloudCoderWebServer && ant clean && ant jar")/256 == 0
	|| die "Couldn't build CloudCoderWebServer\n";

print "==> Building CloudCoderBuilder...\n";
system("cd CloudCoderBuilder2 && ant clean && ant jar")/256 == 0
	|| die "Couldn't build CloudCoderBuilder\n";

print "==> Done!\n";

