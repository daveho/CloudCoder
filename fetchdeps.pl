#! /usr/bin/perl

# Fetch all external jarfiles needed by CloudCoder and copy
# them to the correct place.  We have some fairly heavy external
# dependencies, and github has a limit on repository size.

# List of all external jarfiles and the targets (filenames to which
# they should be copied.)
my @deps = (
	[ "http://mirrors.ibiblio.org/pub/mirrors/maven2/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar",
	  [ "CloudCoderLogging/lib/jsr305.jar" ] ],
	[ "http://search.maven.org/remotecontent?filepath=com/google/guava/guava/13.0.1/guava-13.0.1.jar",
	  [ "CloudCoderLogging/lib/guava.jar" ] ],
	[ "http://mirrors.ibiblio.org/maven2/log4j/log4j/1.2.16/log4j-1.2.16.jar",
	  [ "CloudCoder/war/WEB-INF/lib/log4j-1.2.16.jar",
	    "CloudCoderLogging/lib/log4j-1.2.16.jar" ] ],
	[ "http://owasp-java-html-sanitizer.googlecode.com/svn-history/r118/trunk/distrib/lib/owasp-java-html-sanitizer.jar",
	  [ "CloudCoderLogging/lib/owasp-java-html-sanitizer.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.4/slf4j-api-1.6.4.jar",
	  [ "CloudCoderLogging/lib/slf4j-api-1.6.4.jar" ] ],
	[ "http://repo2.maven.org/maven2/org/slf4j/slf4j-log4j12/1.6.4/slf4j-log4j12-1.6.4.jar",
	  [ "CloudCoderLogging/lib/slf4j-log4j12-1.6.4.jar" ] ],
	[ "http://repo1.maven.org/maven2/commons-io/commons-io/2.1/commons-io-2.1.jar",
	  [ "CloudCoderBuilder/lib/commons-io-2.1.jar",
	    "CloudCoderBuilder2/lib/commons-io-2.1.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/python/jython-standalone/2.5.2/jython-standalone-2.5.2.jar",
	  [ "CloudCoderBuilder/lib/jython.jar",
	    "CloudCoderBuilder2/lib/jython.jar" ] ],
	[ "https://github.com/downloads/daveho/Daemon/daemon-0.1.jar",
	  [ "CloudCoderJetty/lib/daemon/daemon.jar",
	    "CloudCoderBuilder2/lib/daemon.jar",
	    "CloudCoderBuilder/lib/daemon.jar" ] ],
	[ "http://jruby.org.s3.amazonaws.com/downloads/1.7.0/jruby-complete-1.7.0.jar",
	  [ "CloudCoderBuilder2/lib/jruby-complete-1.7.0.jar" ] ],
);

my $delete = 0;
if (scalar(@ARGV) > 0) {
	my $arg = shift @ARGV;
	if ($arg eq '--delete') {
		$delete = 1;
	} else {
		die "Unknown option: $arg\n";
	}
}

if ($delete) {
	DeleteTargets();
} else {
	FetchAll();
}

sub DeleteTargets {
	print "Deleting all targets...\n";
	foreach my $dep (@deps) {
		my @targets = @{$dep->[1]};
		foreach my $target (@targets) {
			print "  $target\n";
			Run('rm', '-f', $target);
		}
	}
	print "done\n";
}

sub FetchAll {
	foreach my $dep (@deps) {
		my $download = $dep->[0];
		my @targets = @{$dep->[1]};
		my @neededTargets = ();
	
		# Check to see which targets are needed.
		foreach my $target (@targets) {
			if (! -e $target) {
				push @neededTargets, $target;
			}
		}
	
		# If there are needed targets...
		if (scalar(@neededTargets) > 0) {
			# Download the jar
			my $t = Download($download);
			# Copy it to all needed targets
			foreach my $neededTarget (@neededTargets) {
				Copy($t, $neededTarget);
			}
		}
	}
}

sub Download {
	my ($jar) = @_;
	Run('mkdir', '-p', 'deps');
	my $file = File($jar);
	print "Fetching $jar...\n";
	Run('wget', $jar, "--output-document=deps/$file");
	return "deps/$file";
}

sub Copy {
	my ($file, $target) = @_;
	print "Copying $file to $target...\n";
	Run('cp', $file, $target);
}

sub File {
	my ($jar) = @_;
	die if !($jar =~ m,/([^/]+)$,);
	return $1;
}

sub Run {
	my @cmd = @_;
	system(@cmd)/256 == 0 || die "Could not run command: " . join(@cmd, ' ') . "\n";
}
