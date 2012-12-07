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
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-ajp/7.4.4.v20110707/jetty-ajp-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-ajp-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-annotations/7.4.4.v20110707/jetty-annotations-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-annotations-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-client/7.4.4.v20110707/jetty-client-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-client-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-continuation/7.4.4.v20110707/jetty-continuation-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-continuation-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-deploy/7.4.4.v20110707/jetty-deploy-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-deploy-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-http/7.4.4.v20110707/jetty-http-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-http-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-io/7.4.4.v20110707/jetty-io-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-io-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-jmx/7.4.4.v20110707/jetty-jmx-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-jmx-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-jndi/7.4.4.v20110707/jetty-jndi-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-jndi-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-overlay-deployer/7.4.4.v20110707/jetty-overlay-deployer-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-overlay-deployer-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-plus/7.4.4.v20110707/jetty-plus-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-plus-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-policy/7.4.4.v20110707/jetty-policy-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-policy-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-rewrite/7.4.4.v20110707/jetty-rewrite-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-rewrite-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-security/7.4.4.v20110707/jetty-security-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-security-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-server/7.4.4.v20110707/jetty-server-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-server-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlet/7.4.4.v20110707/jetty-servlet-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-servlet-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlets/7.4.4.v20110707/jetty-servlets-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-servlets-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util/7.4.4.v20110707/jetty-util-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-util-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-webapp/7.4.4.v20110707/jetty-webapp-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-webapp-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-websocket/7.4.4.v20110707/jetty-websocket-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-websocket-7.4.4.v20110707.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-xml/7.4.4.v20110707/jetty-xml-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jetty-xml-7.4.4.v20110707.jar" ] ],
#	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-server/7.4.4.v20110707/servlet-api-2.5.jar",
#	  [ "CloudCoderJetty/lib/jetty/servlet-api-2.5.jar" ] ],
	[ "http://repo1.maven.org/maven2/javax/servlet/servlet-api/2.5/servlet-api-2.5.jar",
	  [ "CloudCoderJetty/lib/jetty/servlet-api-2.5.jar" ] ],
	[ "http://download.eclipse.org/jetty/orbit/com.sun.el_1.0.0.v201004190952.jar",
	  [ "CloudCoderJetty/lib/jetty/jsp/com.sun.el_1.0.0.v201004190952.jar" ] ],
	[ "http://download.eclipse.org/jetty/orbit/ecj-3.6.jar",
	  [ "CloudCoderJetty/lib/jetty/jsp/ecj-3.6.jar" ] ],
	[ "http://download.eclipse.org/jetty/orbit/javax.el_2.1.0.v201004190952.jar",
	  [ "CloudCoderJetty/lib/jetty/jsp/javax.el_2.1.0.v201004190952.jar" ] ],
	[ "http://download.eclipse.org/jetty/orbit/javax.servlet.jsp_2.1.0.v201004190952.jar",
	  [ "CloudCoderJetty/lib/jetty/jsp/javax.servlet.jsp_2.1.0.v201004190952.jar" ] ],
	[ "http://download.eclipse.org/jetty/orbit/javax.servlet.jsp.jstl_1.2.0.v201004190952.jar",
	  [ "CloudCoderJetty/lib/jetty/jsp/javax.servlet.jsp.jstl_1.2.0.v201004190952.jar" ] ],
	[ "http://download.eclipse.org/jetty/orbit/org.apache.jasper.glassfish_2.1.0.v201007080150.jar",
	  [ "CloudCoderJetty/lib/jetty/jsp/org.apache.jasper.glassfish_2.1.0.v201007080150.jar" ] ],
	[ "http://download.eclipse.org/jetty/orbit/org.apache.taglibs.standard.glassfish_1.2.0.v201004190952.jar",
	  [ "CloudCoderJetty/lib/jetty/jsp/org.apache.taglibs.standard.glassfish_1.2.0.v201004190952.jar" ] ],
	[ "http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-jsp-2.1/7.4.4.v20110707/jetty-jsp-2.1-7.4.4.v20110707.jar",
	  [ "CloudCoderJetty/lib/jetty/jsp/jetty-jsp-2.1-7.4.4.v20110707.jar" ] ],

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
			# Download the jar (if it's not already in the deps directory)
			my $jar = File($download);
			my $t = "deps/$jar";
			if (! -e $t) {
				Download($download, $t);
			}
			# Copy it to all needed targets
			foreach my $neededTarget (@neededTargets) {
				Copy($t, $neededTarget);
			}
		}
	}
}

sub Download {
	my ($jar, $toFile) = @_;
	my $dir = Dir($jar);
	if ($dir ne '' && (! -d $dir)) {
		Run('mkdir', '-p', $dir);
	}
	my $file = File($jar);
	print "Fetching $jar...\n";
	Run('wget', $jar, "--output-document=$toFile");
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

sub Dir {
	my ($jar) = @_;
	my $dir = ($jar =~ m,^(.*)/[^/]+$,) ? $1 : '';
	return $1;
}

sub Run {
	my @cmd = @_;
	system(@cmd)/256 == 0 || die ("Could not run command: " . join(@cmd, ' ') . "\n");
}

# vim:ts=2:
