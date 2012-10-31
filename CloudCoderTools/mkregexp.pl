#! /usr/bin/perl -w

use strict;

my @pats = ();

my $numfmt = '';

while (<>) {
	chomp;
	while (length($_) > 0) {
		if (!/%/) {
			Text($_);
			$_ = '';
		} elsif (/^([^%]*)%(([a-z]+)(\(([^\)]+)\))?)(.*)$/) {
			my $pfx = $1;
			my $directive_name = $3;
			my $directive_opts = $5;
			my $rest = $6;
			#print "pfx=$pfx, directive=$directive, rest=$rest\n";
			if (length($pfx) > 0) {
				Text($pfx);
			}
			Directive($directive_name, $directive_opts);
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
	$regex .= '\s+' if (!First());
	foreach my $chunk (@chunks) {
		$regex .= "\\Q$chunk\\E";
	}

	push @pats, $regex;
}

sub Directive {
	my ($name, $opts) = @_;

	if ($name eq 'numfmt') {
		$numfmt = $opts;
	} elsif ($name eq 'numrange') {
		$opts =~ s,\s,,g;
		my ($min, $max, $incr) = split(',', $opts);
		$incr = 1 if (!defined $incr);

		for (my $i = $min; $i <= $max; $i += $incr) {
			Num($i);
		}
	}
}

sub Num {
	my ($val) = @_;
	my $regex = '';
	$regex .= '\s+' if (!First());
	if ($numfmt eq 'int') {
		$regex .= '(0*)';
		$regex .= int($val);
	}
	push @pats, $regex;
}

