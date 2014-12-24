#! /usr/bin/perl -w

use FileHandle;
use strict;

# Fetch external dependencies and copy them to specified locations,
# as specified by a deps file.
#
# The deps file should be a series of entries in the following form:
#
# http://something.com/blah/awesome-lib.jar
#    dir1/awesome-lib.jar
#    dir2/yeah/awesome-lib.jar
#
# Each file to download should be on a line with no leading spaces.
# Each target (file to which the downloaded file should be copied)
# should be on a line with at least one leading space.
#
# The default deps file is "default.deps", but can be overridden with
# the --deps=filename option.

# Handle command line options.
my $depsFile = 'default.deps';
my $mode = 'fetch';
my $use_curl = 0;
if (scalar(@ARGV) > 0) {
	my $arg = shift @ARGV;
	if ($arg =~ /^--deps=(.*)$/) {
		$depsFile = $1;
	} elsif ($arg eq '--delete') {
		$mode = 'delete';
	} elsif ($arg eq '--list-targets') {
		$mode = 'list';
	} elsif ($arg eq '--check') {
		$mode = 'check';
	} elsif ($arg eq '--curl') {
		$use_curl = 1;
	} else {
		die "Unknown option: $arg\n";
	}
}

# Read deps file.
my @deps = ReadDeps($depsFile);
#PrintDeps(@deps);
#exit 0;

if ($mode eq 'fetch') {
	FetchAll();
} elsif ($mode eq 'delete') {
	DeleteTargets();
} elsif ($mode eq 'list') {
	ListTargets(@deps);
} elsif ($mode eq 'check') {
	CheckTargets(@deps);
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
	my $downloaded = 0;
	my $copied = 0;
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
				$downloaded++;
			}
			# Copy it to all needed targets
			foreach my $neededTarget (@neededTargets) {
				Copy($t, $neededTarget);
				$copied++;
			}
		}
	}
	print "$downloaded file(s) downloaded\n";
	print "$copied file(s) copied\n";
}

sub Download {
	my ($jar, $toFile) = @_;
	EnsureDirExists($toFile);
	my $file = File($jar);
	print "Fetching $jar...\n";
	if ($use_curl) {
		Run('curl', '-o', $toFile, $jar);
	} else {
		Run('wget', $jar, "--output-document=$toFile");
	}
}

sub Copy {
	my ($file, $target) = @_;
	print "Copying $file to $target...\n";
	EnsureDirExists($target);
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

sub ReadDeps {
	my ($filename) = @_;
	my @deps = ();
	my %vars = ();
	my $fh = new FileHandle("<$filename") || die "Couldn't open deps file $filename: $!\n";
	my $download;
	my $targets = [];
	while (<$fh>) {
		chomp;

		# Comment?
		if (/^\s*#/) {
			next;
		}

		# Blank line?
		if (/^\s*$/) {
			next;
		}

		# Is this a variable definition?
		if (/^([A-Za-z_][A-Za-z_0-9]*)\s*=\s*(\S*)\s*$/) {
			$vars{$1} = $2;
			next;
		}

		# Perform variable substitutions
		s,\${([A-Za-z_][A-Za-z_0-9]*)},$vars{$1},ge;

		if (/^(\S+)/) {
			# Download
			if (defined $download) {
				# Push previous download/targets
				push @deps, [ $download, $targets ];
			}
			# Start new download/targets
			$download = $1;
			$targets = [];
		} elsif (/^\s+(.*)/) {
			# Add target to current download
			push @{$targets}, $1;
		}
	}
	push @deps, [ $download, $targets ];
	$fh->close();
	return @deps;
}

sub PrintDeps {
	my @deps = @_;
	foreach my $d (@deps) {
		print "download: ", $d->[0], "\n";
		my @targets = @{$d->[1]};
		foreach my $target (@targets) {
			print "  target: $target\n";
		}
	}
	exit 0;
}

sub ListTargets {
	my @deps = @_;
	foreach my $d (@deps) {
		foreach my $target (@{$d->[1]}) {
			print "$target\n";
		}
	}
}

sub EnsureDirExists {
	my ($toFile) = @_;
	my $dir = Dir($toFile);
	if ($dir ne '' && (! -d $dir)) {
		Run('mkdir', '-p', $dir);
	}
}

# Check each jar directory to check whether there are
# any jarfiles that are not either
#
#   (1) part of the explicit download set, or
#   (2) explicitly ignored in the .gitignore (indicating that
#       the presence of the jarfile is expected, perhaps
#       copied by a means other than this script)
#
# If any jar files not matching (1) or (2) are found,
# report them, since they may be stale (and could cause
# problems with any build artifacts they might accidentally
# be included in).
#
# Causes the script to exit with an exit code of 0
# if no unexpected jarfiles are found, or 1 if at least
# one unexpected jarfile is found.
sub CheckTargets {
	my @deps = @_;
	my %jardirs = ();

	# Build a map of jar directories to sets of jarfiles
	# downloaded by this script.
	foreach my $tuple (@deps) {
		my @targets = @{$tuple->[1]};
		foreach my $target (@targets) {
			if ($target =~ m,^(.*)/([^/\s]+)\s*$,) {
				my $expectedJarFiles = $jardirs{$1};
				if (!defined $expectedJarFiles) {
					$expectedJarFiles = {};
					$jardirs{$1} = $expectedJarFiles;
				}
				$expectedJarFiles->{$2} = 1;
			}
		}
	}

	# Check each jar directory for unexpected jar files.
	my $unexpectedJarCount = 0;
	foreach my $jardir (sort keys %jardirs) {
		#print "Jar dir: $jardir\n";
		my %ignoredJarFiles = GetIgnoredJarFiles($jardir);
		my %expectedJarFiles = %{$jardirs{$jardir}};

		my $contents_fh = new FileHandle("ls '$jardir'|") || die;
		while (<$contents_fh>) {
			chomp;
			if (/^(.*\.jar)\s*$/) {
				my $found = $1;
				if ((!exists $ignoredJarFiles{$found}) && (!exists $expectedJarFiles{$found})) {
					print "Warning: unexpected jar file $jardir/$found\n";
					$unexpectedJarCount++;
				}
			}
		}
	}
	exit ($unexpectedJarCount > 0 ? 1 : 0);
}

# Get the set of explicitly ignored jar files in given directory.
sub GetIgnoredJarFiles {
	my ($jardir) = @_;
	my %ignoredJarFiles = ();
	if (my $fh = new FileHandle("<$jardir/.gitignore")) {
		while (<$fh>) {
			chomp;
			if (m,/([^\*]+\.jar)\s*$,) {
				$ignoredJarFiles{$1} = 1;
			}
		}
	}
	return %ignoredJarFiles;
}

# vim:ts=2:
