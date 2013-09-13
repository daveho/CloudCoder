#! /usr/bin/perl -w

use FileHandle;
use strict;

my $DEBUG=0;

#
# Create a new distribution of CloudCoder
#
# Finds the latest tag prefixed with v in the repostiory
#
# builds the code
#
# Copies cloudcoderApp.jar and cloudcoderBuilder.jar to S3
#
# updates LATEST file at S3
#
# updates the downloads on the wiki and commits it
# 

# TODO: Put these in a config file or allow them to be changed on command-line
my $aws="./aws";
my $bucket="cloudcoder-binaries";
my $webapp="CloudCoderWebServer/cloudcoderApp.jar";
my $builder="CloudCoderBuilder2/cloudcoderBuilder.jar";
my $webappSrc='CloudCoder/src';
my $builderSrc='CloudCoderBuilder2/src';
my $builderBin='CloudCoderBuilder2/bin';
my $wiki="../CloudCoder.wiki";
my $downloadFile='Downloads.md';
my $downloads="$wiki/$downloadFile";

my $comment='';
my $version=0;
my $override=0;
my $build=1;
while (scalar(@ARGV) > 0) {
  my $arg = shift @ARGV;
  if ($arg =~ /^--wiki=(.*)$/) {
    $wiki = $1;
  } elsif ($arg =~ /^--comment=(.*)$/) {
    $comment = $1;
  } elsif ($arg eq '--debug' or $arg eq '-d' or $arg eq '-D') {
    $DEBUG = 1;
  } elsif ($arg eq '--override') {
    # override of checking 
    $override = 1;
  } elsif ($arg eq '--skipbuild') {
    $build = 0;
  } else {
    die "Unknown option: $arg\n";
  }
}

#
# TODO:  If the version is given and is a new version, then tag with the version
#

# Get the latest version
$version=LatestVersion();
debug("Latest version is $version");

if (not $override and CheckPreviousReleases($version, $downloads)) {
  die "It looks like there is already a release for Version $version!
If you want to over-ride and re-release $version, please re-run 
this command with the --override switch\n";
}

# my $msg="You are about to release $version version!
# Please make sure that you are releasing from cloudcoderdotorg/CloudCoder!
# Please make sure there are no uncommitted changes!
# Do you want to proceed?
# ";
# print $msg;

#
# Create VERSION file
# Copy VERSION into the proper folders
#
print "Creating VERSION file\n";
WriteStringToFile($version, 'VERSION');
print "Done creating VERSION\n";
Run('cp', 'VERSION', "$webappSrc");
Run('cp', 'VERSION', "$builderSrc");
Run('cp', 'VERSION', "$builderBin");
print "Done copying VERSION to $webappSrc and $builderSrc";

# TODO: Assert the VERSION files match to allow skipping
# the building
# TODO: Assert there are no uncommitted changes
# Build the binaries
if ($build==1) {
  print "Building code:\n./build.pl\n";
  print `./fetchdeps.pl`;
  print `./build.pl`;
  print "\n\nDONE BUILDING\n\n";
}

# Create LATEST file
print "Creating LATEST file\n";
WriteStringToFile($version, 'LATEST');
print "Done creating LATEST\n";

# upload files to S3
print "Uploading to S3\n";
Run2("$aws", 'put', '"x-amz-acl: public-read"', "$bucket/LATEST", "LATEST");
Run2("$aws", 'put', '"x-amz-acl: public-read"', "$bucket/bootstrap.pl", 'bootstrap.pl');
Run2("$aws", 'put', '"x-amz-acl: public-read"', "$bucket/cloudcoderApp-$version.jar", "$webapp");
Run2("$aws", 'put', '"x-amz-acl: public-read"', "$bucket/cloudcoderBuilder-$version.jar", "$builder");

print "Done uploading to S3\n";

# update the Wiki repository
print "git pull origin master for $wiki";
GitPullOriginMaster($wiki);
print "Done pulling for $wiki";

# update the Downloads.md file
print "Updating Downloads of CloudCoder.wiki\n";
UpdateDownloads($version, $downloads, $comment);
print "Done updating Downloads of CloudCoder.wiki\n";

print "Committing new Downloads.md";
GitAddCommit($version, $wiki, $downloadFile);
print "Done committing Downloads.md";

print "\n\nReleased $version\n\n";

sub ReadLinesFromTextFile {
  my @lines=();
  my $filename=shift @_;
  open(FH, "$filename") or die "Can't open $filename for read: $!";
  while (<FH>) {
    chomp($_);
    push (@lines, $_);
  }
  close FH or die "Cannot close $filename: $!"; 
  return @lines;
}

sub CheckPreviousReleases {
  my $version=shift @_;
  my $downloads=shift @_;
  my @lines=ReadLinesFromTextFile($downloads);
  foreach my $line (@lines) {
    $line =~ s/<br\/>//g;
    $line =~ s/<br>//g;
    if ($line eq $version) {
      return 1;
    }
  }
  return 0;
}

sub UpdateDownloads {
  my $version=shift @_;
  my $downloads=shift @_;
  my $comment='';
  if (scalar @_ > 0) {
    my $comment=shift @_;
  }

  my @lines=ReadLinesFromTextFile($downloads);
  #
  # If we already have the version number in a line, don't update
  #
  for my $line (@lines) {
    if ($line =~ $version) {
      return;
    }
  }


  my $webappLink="> [cloudcoderApp-$version.jar](https://s3.amazonaws.com/$bucket/cloudcoderApp-$version.jar)";
  my $builderLink="> [cloudcoderBuilder-$version.jar](https://s3.amazonaws.com/$bucket/cloudcoderBuilder-$version.jar)";

  my $timestamp=`date '+%Y-%m-%d %H:%M'`;
  chomp($timestamp);

  my @newlines=();
  my $foundFirstDivider=0;
  foreach my $line (@lines) {
    if ($line =~ /========/ and $foundFirstDivider==0) {
      #print "$line is where we should start";
      $foundFirstDivider=1;
      push(@newlines, $line);
      push(@newlines, '');
      push(@newlines, "$version<br/>");
      push(@newlines, "$timestamp<br/>");
      if ($comment ne '') {
	push(@newlines, $comment);
      }
      push(@newlines, '');
      push(@newlines, $webappLink);
      push(@newlines, '');
      push(@newlines, $builderLink);
      push(@newlines, '');
      push(@newlines, $line);
    } else {
      push(@newlines, $line);
    }
  }

  WriteStringToFile(join("\n", @newlines), $downloads);
}

sub GitPullOriginMaster {
  my $dir=shift @_;
  print `cd $dir ; git pull origin master`;
  if ($?) {
    die "Unable to git pull origin master in $dir";
  }
}

sub GitAddCommit {
  my $version=shift @_;
  my $dir=shift @_;
  my $file=shift @_;
  print `cd $dir ; git add $file`;
  if ($?) {
    die "Unable to git add $file for $dir";
  }
  print `cd $dir ; git commit -m "Updated Downloads.md for release $version"`;
  if ($?) {
    die "Unable to git commit for $dir";
  }
  print `cd $dir ; git push origin master`;
  if ($?) {
    die "Unable to git push origin master for $dir"
  }
}

sub WriteStringToFile {
  my $string=shift @_;
  my $filename=shift @_;
  open(FILE, ">$filename") or die("Cannot open $filename for writing");
  print FILE "$string\n";
  close(FILE) or die("Cannot close $filename");
}

sub LatestVersion {
  my $tags=`git tag -l`;
  my $max='';
  foreach my $tag (split /\n/, $tags) {
    my $first=substr($tag, 0, 1);
    if ($first eq 'v' and $tag gt $max) {
      $max=$tag;
    }
  }
  if ($max eq '') {
    die "Cannot find any tags in the repository";
  }
  return $max;
}

sub CheckVersion {
  # Make sure that the version has not already been released
  my $tags=`git tag -l`;
  for (split /^/, $tags) {
    print "$_";
  }
}

sub debug {
  my $msg=shift @_;
  if ($DEBUG==1) {
    print "$msg\n";
  }
}

sub Run {
  my @cmd = @_;
  debug('' . join(' ', @cmd));
  system(@cmd)/256 == 0 || die ("Could not run command: " . join(' ', @cmd) . "\n");
}

sub Run2 {
  my $cmd=join(' ', @_) . "\n";
  print "$cmd\n";
  print `$cmd`;
  if ($?) {
    die "ERROR: unable to execute $cmd";
  }
}
