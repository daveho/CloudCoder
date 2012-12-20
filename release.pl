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
my $downloads="$wiki/Downloads.md";

my $comment='';
my $version=0;
if (scalar(@ARGV) > 0) {
  my $arg = shift @ARGV;
  if ($arg =~ /^--wiki=(.*)$/) {
    $wiki = $1;
  } elsif ($arg =~ /^--comment=(.*)$/) {
    $comment = $1;
  } elsif ($arg eq '--debug' or $arg eq '-d' or $arg eq '-D') {
    $DEBUG = 1;
  } else {
    die "Unknown option: $arg\n";
  }
}

# TODO:  If the version is given and is a new version, then tag with the version

# Get the latest version
$version=LatestVersion();
debug("Latest version is $version");

my $msg="You are about to release $version version!

Please make sure that you are releasing from cloudcoderdotorg/CloudCoder!

Please make sure there are no uncommitted changes!

Do you want to proceed?
";
#print $msg;

# TODO: Assert there are no uncommitted changes...
# Build the binaries
Run("./build.pl");

# Create LATEST file
WriteStringToFile($version, 'LATEST');

# upload files to S3
#Run("$aws", 'put', "$bucket/cloudcoderApp-$version.jar", "$webapp");
#Run("$aws", 'put', "$bucket/cloudcoderBuilder-$version.jar", "$builder");
Run("$aws", 'put', "$bucket/LATEST", "LATEST");

# update the Downloads.md file
UpdateDownloads($version, $downloads, $comment);


sub UpdateDownloads {
  my $version=shift @_;
  my $downloads=shift @_;
  my $comment='';
  if (scalar @_ > 0) {
    my $comment=shift @_;
  }

  my @lines=();
  open(FH, "$downloads") or die "Can't open $downloads for read: $!";
  while (<FH>) {
    chomp($_);
    push (@lines, $_);
  }
  close FH or die "Cannot close $downloads: $!"; 

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
