#! /usr/bin/perl -w

use strict;

my @pats = ();

while (<>) {
	chomp;
	while (length($_) > 0) {
		if (!/%/) {
			Text($_);
			$_ = '';
		} elsif (/^([^%]*)%([a-z]+(\([^\)]+\))?)(.*)$/) {
			my $pfx = $1;
			my $directive = $2;
			my $rest = $4;
			#print "pfx=$pfx, directive=$directive, rest=$rest\n";
			if (length($pfx) > 0) {
				Text($pfx);
			}
			Directive($directive);
			$_ = $rest;
		} else {
			die "unmatched: $_\n";
		}
	}
}

foreach my $pat (@pats) {
	print "$pat\n";
}

sub First {
	return scalar(@pats) == 0;
}

sub Text {
	my ($txt) = @_;
	return if ($txt =~ /^\s*$/);
	$txt =~ s,^\s+,,;
	$txt =~ s,\s+$,,;

	my @chunks = split(/\s+/, $txt);

	my $regex = '';
	if (!First()) {
		$regex .= '\s+';
	}
	foreach my $chunk (@chunks) {
		$regex .= "\\Q$chunk\\E";
	}

	push @pats, $regex;
}

sub Directive {
	my ($directive) = @_;
	push @pats, "[$directive]";
}

