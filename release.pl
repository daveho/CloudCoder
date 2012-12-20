#! /usr/bin/perl -w

use FileHandle;
use strict;

my $DEBUG=0;

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
my $wiki="../CloudCoder.wiki";
my $downloadFile='Downloads.md';
my $downloads="$wiki/$downloadFile";

my $comment='';
my $version=0;
my $override=0;
if (scalar(@ARGV) > 0) {
  my $arg = shift @ARGV;
  if ($arg =~ /^--wiki=(.*)$/) {
    $wiki = $1;
  } elsif ($arg =~ /^--comment=(.*)$/) {
    $comment = $1;
  } elsif ($arg eq '--debug' or $arg eq '-d' or $arg eq '-D') {
    $DEBUG = 1;
  } elsif ($arg eq '--override') {
    $override = 1;
  } else {
    die "Unknown option: $arg\n";
  }
}

# TODO:  If the version is given and is a new version, then tag with the version

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

# TODO: Assert there are no uncommitted changes...
# Build the binaries
print "Building code:\n./build.pl\n";
print `./fetchdeps.pl`;
print `./build.pl`;
print "\n\nDONE BUILDING\n\n";

# Create LATEST file
print "Creating LATEST file\n";
WriteStringToFile($version, 'LATEST');
print "Done creating LATEST\n";

# upload files to S3
print "Uploading to S3\n";
Run("$aws", 'put', "$bucket/cloudcoderApp-$version.jar", "$webapp");
Run("$aws", 'put', "$bucket/cloudcoderBuilder-$version.jar", "$builder");
Run("$aws", 'put', "$bucket/LATEST", "LATEST");
print "Done uploading to S3\n";

# update the Downloads.md file
print "Updating Downloads of CloudCoder.wiki\n";
UpdateDownloads($version, $downloads, $comment);
print "Done updating Downloads of CloudCoder.wiki\n";

print "Commiting new Downloads.md";
GitAddCommit($version, $wiki, $downloadFile);
print "Done committing Downloads.md";

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
      push(@newlines, $webappLink);
      push(@newlines, '');
      push(@newlines, $line);
    } else {
      push(@newlines, $line);
    }
  }

  WriteStringToFile(join("\n", @newlines), $downloads);
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
  system(@cmd)/256 == 0 || die ("Could not run command: " . join(@cmd, ' ') . "\n");
}
