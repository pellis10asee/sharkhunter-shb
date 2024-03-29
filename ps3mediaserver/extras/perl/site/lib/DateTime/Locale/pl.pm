###########################################################################
#
# This file is auto-generated by the Perl DateTime Suite locale
# generator (0.05).  This code generator comes with the
# DateTime::Locale distribution in the tools/ directory, and is called
# generate-from-cldr.
#
# This file as generated from the CLDR XML locale data.  See the
# LICENSE.cldr file included in this distribution for license details.
#
# This file was generated from the source file pl.xml
# The source file version number was 1.122, generated on
# 2009/06/15 03:46:24.
#
# Do not edit this file directly.
#
###########################################################################

package DateTime::Locale::pl;

use strict;
use warnings;
use utf8;

use base 'DateTime::Locale::root';

sub cldr_version { return "1\.7\.1" }

{
    my $am_pm_abbreviated = [ "AM", "PM" ];
    sub am_pm_abbreviated { return $am_pm_abbreviated }
}
{
    my $date_format_full = "EEEE\,\ d\ MMMM\ y";
    sub date_format_full { return $date_format_full }
}

{
    my $date_format_long = "d\ MMMM\ y";
    sub date_format_long { return $date_format_long }
}

{
    my $date_format_medium = "dd\-MM\-yyyy";
    sub date_format_medium { return $date_format_medium }
}

{
    my $date_format_short = "dd\-MM\-yy";
    sub date_format_short { return $date_format_short }
}

{
    my $day_format_abbreviated = [ "pon\.", "wt\.", "śr\.", "czw\.", "pt\.", "sob\.", "niedz\." ];
    sub day_format_abbreviated { return $day_format_abbreviated }
}

sub day_format_narrow { $_[0]->day_stand_alone_narrow() }

{
    my $day_format_wide = [ "poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota", "niedziela" ];
    sub day_format_wide { return $day_format_wide }
}

sub day_stand_alone_abbreviated { $_[0]->day_format_abbreviated() }

{
    my $day_stand_alone_narrow = [ "P", "W", "Ś", "C", "P", "S", "N" ];
    sub day_stand_alone_narrow { return $day_stand_alone_narrow }
}

sub day_stand_alone_wide { $_[0]->day_format_wide() }

{
    my $era_abbreviated = [ "p\.n\.e\.", "n\.e\." ];
    sub era_abbreviated { return $era_abbreviated }
}

sub era_narrow { $_[0]->era_abbreviated() }

{
    my $era_wide = [ "p\.n\.e\.", "n\.e\." ];
    sub era_wide { return $era_wide }
}
{
    my $first_day_of_week = "1";
    sub first_day_of_week { return $first_day_of_week }
}

{
    my $month_format_abbreviated = [ "sty", "lut", "mar", "kwi", "maj", "cze", "lip", "sie", "wrz", "paź", "lis", "gru" ];
    sub month_format_abbreviated { return $month_format_abbreviated }
}

sub month_format_narrow { $_[0]->month_stand_alone_narrow() }

{
    my $month_format_wide = [ "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca", "lipca", "sierpnia", "września", "października", "listopada", "grudnia" ];
    sub month_format_wide { return $month_format_wide }
}

sub month_stand_alone_abbreviated { $_[0]->month_format_abbreviated() }

{
    my $month_stand_alone_narrow = [ "s", "l", "m", "k", "m", "c", "l", "s", "w", "p", "l", "g" ];
    sub month_stand_alone_narrow { return $month_stand_alone_narrow }
}
{
    my $month_stand_alone_wide = [ "styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec", "lipiec", "sierpień", "wrzesień", "październik", "listopad", "grudzień" ];
    sub month_stand_alone_wide { return $month_stand_alone_wide }
}
{
    my $quarter_format_abbreviated = [ "K1", "K2", "K3", "K4" ];
    sub quarter_format_abbreviated { return $quarter_format_abbreviated }
}

sub quarter_format_narrow { $_[0]->quarter_stand_alone_narrow() }

{
    my $quarter_format_wide = [ "I\ kwartał", "II\ kwartał", "III\ kwartał", "IV\ kwartał" ];
    sub quarter_format_wide { return $quarter_format_wide }
}
{
    my $quarter_stand_alone_abbreviated = [ "1\ kw\.", "2\ kw\.", "3\ kw\.", "4\ kw\." ];
    sub quarter_stand_alone_abbreviated { return $quarter_stand_alone_abbreviated }
}
{
    my $quarter_stand_alone_narrow = [ "1", "2", "3", "4" ];
    sub quarter_stand_alone_narrow { return $quarter_stand_alone_narrow }
}

sub quarter_stand_alone_wide { $_[0]->quarter_format_wide() }

{
    my $time_format_full = "HH\:mm\:ss\ zzzz";
    sub time_format_full { return $time_format_full }
}

{
    my $time_format_long = "HH\:mm\:ss\ z";
    sub time_format_long { return $time_format_long }
}

{
    my $time_format_medium = "HH\:mm\:ss";
    sub time_format_medium { return $time_format_medium }
}

{
    my $time_format_short = "HH\:mm";
    sub time_format_short { return $time_format_short }
}

{
    my $_format_for_HHmm = "HH\:mm";
    sub _format_for_HHmm { return $_format_for_HHmm }
}

{
    my $_format_for_HHmmss = "HH\:mm\:ss";
    sub _format_for_HHmmss { return $_format_for_HHmmss }
}

{
    my $_format_for_Hm = "H\:mm";
    sub _format_for_Hm { return $_format_for_Hm }
}

{
    my $_format_for_M = "L";
    sub _format_for_M { return $_format_for_M }
}

{
    my $_format_for_MEd = "E\,\ M\-d";
    sub _format_for_MEd { return $_format_for_MEd }
}

{
    my $_format_for_MMM = "LLL";
    sub _format_for_MMM { return $_format_for_MMM }
}

{
    my $_format_for_MMMEd = "d\ MMM\ E";
    sub _format_for_MMMEd { return $_format_for_MMMEd }
}

{
    my $_format_for_MMMMEd = "d\ MMMM\ E";
    sub _format_for_MMMMEd { return $_format_for_MMMMEd }
}

{
    my $_format_for_MMMMd = "d\ MMMM";
    sub _format_for_MMMMd { return $_format_for_MMMMd }
}

{
    my $_format_for_MMMd = "MMM\ d";
    sub _format_for_MMMd { return $_format_for_MMMd }
}

{
    my $_format_for_MMdd = "MM\-dd";
    sub _format_for_MMdd { return $_format_for_MMdd }
}

{
    my $_format_for_Md = "d\.M";
    sub _format_for_Md { return $_format_for_Md }
}

{
    my $_format_for_d = "d";
    sub _format_for_d { return $_format_for_d }
}

{
    my $_format_for_hhmm = "hh\:mm\ a";
    sub _format_for_hhmm { return $_format_for_hhmm }
}

{
    my $_format_for_hhmmss = "hh\:mm\:ss\ a";
    sub _format_for_hhmmss { return $_format_for_hhmmss }
}

{
    my $_format_for_mmss = "mm\:ss";
    sub _format_for_mmss { return $_format_for_mmss }
}

{
    my $_format_for_ms = "mm\:ss";
    sub _format_for_ms { return $_format_for_ms }
}

{
    my $_format_for_y = "y";
    sub _format_for_y { return $_format_for_y }
}

{
    my $_format_for_yM = "yyyy\-M";
    sub _format_for_yM { return $_format_for_yM }
}

{
    my $_format_for_yMEd = "EEE\,\ d\.M\.yyyy";
    sub _format_for_yMEd { return $_format_for_yMEd }
}

{
    my $_format_for_yMMM = "y\ MMM";
    sub _format_for_yMMM { return $_format_for_yMMM }
}

{
    my $_format_for_yMMMEd = "EEE\,\ d\ MMM\ y";
    sub _format_for_yMMMEd { return $_format_for_yMMMEd }
}

{
    my $_format_for_yMMMM = "LLLL\ y";
    sub _format_for_yMMMM { return $_format_for_yMMMM }
}

{
    my $_format_for_yQ = "yyyy\ Q";
    sub _format_for_yQ { return $_format_for_yQ }
}

{
    my $_format_for_yQQQ = "y\ QQQ";
    sub _format_for_yQQQ { return $_format_for_yQQQ }
}

{
    my $_format_for_yyMM = "MM\/yy";
    sub _format_for_yyMM { return $_format_for_yyMM }
}

{
    my $_format_for_yyMMM = "MMM\ yy";
    sub _format_for_yyMMM { return $_format_for_yyMMM }
}

{
    my $_format_for_yyQ = "Q\ yy";
    sub _format_for_yyQ { return $_format_for_yyQ }
}

{
    my $_format_for_yyyyMM = "yyyy\-MM";
    sub _format_for_yyyyMM { return $_format_for_yyyyMM }
}

{
    my $_format_for_yyyyMMMM = "LLLL\ y";
    sub _format_for_yyyyMMMM { return $_format_for_yyyyMMMM }
}

{
    my $_available_formats =
        {
          "HHmm" => "HH\:mm",
          "HHmmss" => "HH\:mm\:ss",
          "Hm" => "H\:mm",
          "M" => "L",
          "MEd" => "E\,\ M\-d",
          "MMM" => "LLL",
          "MMMEd" => "d\ MMM\ E",
          "MMMMEd" => "d\ MMMM\ E",
          "MMMMd" => "d\ MMMM",
          "MMMd" => "MMM\ d",
          "MMdd" => "MM\-dd",
          "Md" => "d\.M",
          "d" => "d",
          "hhmm" => "hh\:mm\ a",
          "hhmmss" => "hh\:mm\:ss\ a",
          "mmss" => "mm\:ss",
          "ms" => "mm\:ss",
          "y" => "y",
          "yM" => "yyyy\-M",
          "yMEd" => "EEE\,\ d\.M\.yyyy",
          "yMMM" => "y\ MMM",
          "yMMMEd" => "EEE\,\ d\ MMM\ y",
          "yMMMM" => "LLLL\ y",
          "yQ" => "yyyy\ Q",
          "yQQQ" => "y\ QQQ",
          "yyMM" => "MM\/yy",
          "yyMMM" => "MMM\ yy",
          "yyQ" => "Q\ yy",
          "yyyyMM" => "yyyy\-MM",
          "yyyyMMMM" => "LLLL\ y"
        };
    sub _available_formats { return $_available_formats }
}

1;

__END__


=pod

=encoding utf8

=head1 NAME

DateTime::Locale::pl

=head1 SYNOPSIS

  use DateTime;

  my $dt = DateTime->now( locale => 'pl' );
  print $dt->month_name();

=head1 DESCRIPTION

This is the DateTime locale package for Polish.

=head1 DATA

This locale inherits from the L<DateTime::Locale::root> locale.

It contains the following data.

=head2 Days

=head3 Wide (format)

  poniedziałek
  wtorek
  środa
  czwartek
  piątek
  sobota
  niedziela

=head3 Abbreviated (format)

  pon.
  wt.
  śr.
  czw.
  pt.
  sob.
  niedz.

=head3 Narrow (format)

  P
  W
  Ś
  C
  P
  S
  N

=head3 Wide (stand-alone)

  poniedziałek
  wtorek
  środa
  czwartek
  piątek
  sobota
  niedziela

=head3 Abbreviated (stand-alone)

  pon.
  wt.
  śr.
  czw.
  pt.
  sob.
  niedz.

=head3 Narrow (stand-alone)

  P
  W
  Ś
  C
  P
  S
  N

=head2 Months

=head3 Wide (format)

  stycznia
  lutego
  marca
  kwietnia
  maja
  czerwca
  lipca
  sierpnia
  września
  października
  listopada
  grudnia

=head3 Abbreviated (format)

  sty
  lut
  mar
  kwi
  maj
  cze
  lip
  sie
  wrz
  paź
  lis
  gru

=head3 Narrow (format)

  s
  l
  m
  k
  m
  c
  l
  s
  w
  p
  l
  g

=head3 Wide (stand-alone)

  styczeń
  luty
  marzec
  kwiecień
  maj
  czerwiec
  lipiec
  sierpień
  wrzesień
  październik
  listopad
  grudzień

=head3 Abbreviated (stand-alone)

  sty
  lut
  mar
  kwi
  maj
  cze
  lip
  sie
  wrz
  paź
  lis
  gru

=head3 Narrow (stand-alone)

  s
  l
  m
  k
  m
  c
  l
  s
  w
  p
  l
  g

=head2 Quarters

=head3 Wide (format)

  I kwartał
  II kwartał
  III kwartał
  IV kwartał

=head3 Abbreviated (format)

  K1
  K2
  K3
  K4

=head3 Narrow (format)

  1
  2
  3
  4

=head3 Wide (stand-alone)

  I kwartał
  II kwartał
  III kwartał
  IV kwartał

=head3 Abbreviated (stand-alone)

  1 kw.
  2 kw.
  3 kw.
  4 kw.

=head3 Narrow (stand-alone)

  1
  2
  3
  4

=head2 Eras

=head3 Wide

  p.n.e.
  n.e.

=head3 Abbreviated

  p.n.e.
  n.e.

=head3 Narrow

  p.n.e.
  n.e.

=head2 Date Formats

=head3 Full

   2008-02-05T18:30:30 = wtorek, 5 lutego 2008
   1995-12-22T09:05:02 = piątek, 22 grudnia 1995
  -0010-09-15T04:44:23 = sobota, 15 września -10

=head3 Long

   2008-02-05T18:30:30 = 5 lutego 2008
   1995-12-22T09:05:02 = 22 grudnia 1995
  -0010-09-15T04:44:23 = 15 września -10

=head3 Medium

   2008-02-05T18:30:30 = 05-02-2008
   1995-12-22T09:05:02 = 22-12-1995
  -0010-09-15T04:44:23 = 15-09--010

=head3 Short

   2008-02-05T18:30:30 = 05-02-08
   1995-12-22T09:05:02 = 22-12-95
  -0010-09-15T04:44:23 = 15-09--10

=head3 Default

   2008-02-05T18:30:30 = 05-02-2008
   1995-12-22T09:05:02 = 22-12-1995
  -0010-09-15T04:44:23 = 15-09--010

=head2 Time Formats

=head3 Full

   2008-02-05T18:30:30 = 18:30:30 UTC
   1995-12-22T09:05:02 = 09:05:02 UTC
  -0010-09-15T04:44:23 = 04:44:23 UTC

=head3 Long

   2008-02-05T18:30:30 = 18:30:30 UTC
   1995-12-22T09:05:02 = 09:05:02 UTC
  -0010-09-15T04:44:23 = 04:44:23 UTC

=head3 Medium

   2008-02-05T18:30:30 = 18:30:30
   1995-12-22T09:05:02 = 09:05:02
  -0010-09-15T04:44:23 = 04:44:23

=head3 Short

   2008-02-05T18:30:30 = 18:30
   1995-12-22T09:05:02 = 09:05
  -0010-09-15T04:44:23 = 04:44

=head3 Default

   2008-02-05T18:30:30 = 18:30:30
   1995-12-22T09:05:02 = 09:05:02
  -0010-09-15T04:44:23 = 04:44:23

=head2 Datetime Formats

=head3 Full

   2008-02-05T18:30:30 = wtorek, 5 lutego 2008 18:30:30 UTC
   1995-12-22T09:05:02 = piątek, 22 grudnia 1995 09:05:02 UTC
  -0010-09-15T04:44:23 = sobota, 15 września -10 04:44:23 UTC

=head3 Long

   2008-02-05T18:30:30 = 5 lutego 2008 18:30:30 UTC
   1995-12-22T09:05:02 = 22 grudnia 1995 09:05:02 UTC
  -0010-09-15T04:44:23 = 15 września -10 04:44:23 UTC

=head3 Medium

   2008-02-05T18:30:30 = 05-02-2008 18:30:30
   1995-12-22T09:05:02 = 22-12-1995 09:05:02
  -0010-09-15T04:44:23 = 15-09--010 04:44:23

=head3 Short

   2008-02-05T18:30:30 = 05-02-08 18:30
   1995-12-22T09:05:02 = 22-12-95 09:05
  -0010-09-15T04:44:23 = 15-09--10 04:44

=head3 Default

   2008-02-05T18:30:30 = 05-02-2008 18:30:30
   1995-12-22T09:05:02 = 22-12-1995 09:05:02
  -0010-09-15T04:44:23 = 15-09--010 04:44:23

=head2 Available Formats

=head3 d (d)

   2008-02-05T18:30:30 = 5
   1995-12-22T09:05:02 = 22
  -0010-09-15T04:44:23 = 15

=head3 EEEd (d EEE)

   2008-02-05T18:30:30 = 5 wt.
   1995-12-22T09:05:02 = 22 pt.
  -0010-09-15T04:44:23 = 15 sob.

=head3 HHmm (HH:mm)

   2008-02-05T18:30:30 = 18:30
   1995-12-22T09:05:02 = 09:05
  -0010-09-15T04:44:23 = 04:44

=head3 hhmm (hh:mm a)

   2008-02-05T18:30:30 = 06:30 PM
   1995-12-22T09:05:02 = 09:05 AM
  -0010-09-15T04:44:23 = 04:44 AM

=head3 HHmmss (HH:mm:ss)

   2008-02-05T18:30:30 = 18:30:30
   1995-12-22T09:05:02 = 09:05:02
  -0010-09-15T04:44:23 = 04:44:23

=head3 hhmmss (hh:mm:ss a)

   2008-02-05T18:30:30 = 06:30:30 PM
   1995-12-22T09:05:02 = 09:05:02 AM
  -0010-09-15T04:44:23 = 04:44:23 AM

=head3 Hm (H:mm)

   2008-02-05T18:30:30 = 18:30
   1995-12-22T09:05:02 = 9:05
  -0010-09-15T04:44:23 = 4:44

=head3 hm (h:mm a)

   2008-02-05T18:30:30 = 6:30 PM
   1995-12-22T09:05:02 = 9:05 AM
  -0010-09-15T04:44:23 = 4:44 AM

=head3 Hms (H:mm:ss)

   2008-02-05T18:30:30 = 18:30:30
   1995-12-22T09:05:02 = 9:05:02
  -0010-09-15T04:44:23 = 4:44:23

=head3 hms (h:mm:ss a)

   2008-02-05T18:30:30 = 6:30:30 PM
   1995-12-22T09:05:02 = 9:05:02 AM
  -0010-09-15T04:44:23 = 4:44:23 AM

=head3 M (L)

   2008-02-05T18:30:30 = 2
   1995-12-22T09:05:02 = 12
  -0010-09-15T04:44:23 = 9

=head3 Md (d.M)

   2008-02-05T18:30:30 = 5.2
   1995-12-22T09:05:02 = 22.12
  -0010-09-15T04:44:23 = 15.9

=head3 MEd (E, M-d)

   2008-02-05T18:30:30 = wt., 2-5
   1995-12-22T09:05:02 = pt., 12-22
  -0010-09-15T04:44:23 = sob., 9-15

=head3 MMdd (MM-dd)

   2008-02-05T18:30:30 = 02-05
   1995-12-22T09:05:02 = 12-22
  -0010-09-15T04:44:23 = 09-15

=head3 MMM (LLL)

   2008-02-05T18:30:30 = lut
   1995-12-22T09:05:02 = gru
  -0010-09-15T04:44:23 = wrz

=head3 MMMd (MMM d)

   2008-02-05T18:30:30 = lut 5
   1995-12-22T09:05:02 = gru 22
  -0010-09-15T04:44:23 = wrz 15

=head3 MMMEd (d MMM E)

   2008-02-05T18:30:30 = 5 lut wt.
   1995-12-22T09:05:02 = 22 gru pt.
  -0010-09-15T04:44:23 = 15 wrz sob.

=head3 MMMMd (d MMMM)

   2008-02-05T18:30:30 = 5 lutego
   1995-12-22T09:05:02 = 22 grudnia
  -0010-09-15T04:44:23 = 15 września

=head3 MMMMEd (d MMMM E)

   2008-02-05T18:30:30 = 5 lutego wt.
   1995-12-22T09:05:02 = 22 grudnia pt.
  -0010-09-15T04:44:23 = 15 września sob.

=head3 mmss (mm:ss)

   2008-02-05T18:30:30 = 30:30
   1995-12-22T09:05:02 = 05:02
  -0010-09-15T04:44:23 = 44:23

=head3 ms (mm:ss)

   2008-02-05T18:30:30 = 30:30
   1995-12-22T09:05:02 = 05:02
  -0010-09-15T04:44:23 = 44:23

=head3 y (y)

   2008-02-05T18:30:30 = 2008
   1995-12-22T09:05:02 = 1995
  -0010-09-15T04:44:23 = -10

=head3 yM (yyyy-M)

   2008-02-05T18:30:30 = 2008-2
   1995-12-22T09:05:02 = 1995-12
  -0010-09-15T04:44:23 = -010-9

=head3 yMEd (EEE, d.M.yyyy)

   2008-02-05T18:30:30 = wt., 5.2.2008
   1995-12-22T09:05:02 = pt., 22.12.1995
  -0010-09-15T04:44:23 = sob., 15.9.-010

=head3 yMMM (y MMM)

   2008-02-05T18:30:30 = 2008 lut
   1995-12-22T09:05:02 = 1995 gru
  -0010-09-15T04:44:23 = -10 wrz

=head3 yMMMEd (EEE, d MMM y)

   2008-02-05T18:30:30 = wt., 5 lut 2008
   1995-12-22T09:05:02 = pt., 22 gru 1995
  -0010-09-15T04:44:23 = sob., 15 wrz -10

=head3 yMMMM (LLLL y)

   2008-02-05T18:30:30 = luty 2008
   1995-12-22T09:05:02 = grudzień 1995
  -0010-09-15T04:44:23 = wrzesień -10

=head3 yQ (yyyy Q)

   2008-02-05T18:30:30 = 2008 1
   1995-12-22T09:05:02 = 1995 4
  -0010-09-15T04:44:23 = -010 3

=head3 yQQQ (y QQQ)

   2008-02-05T18:30:30 = 2008 K1
   1995-12-22T09:05:02 = 1995 K4
  -0010-09-15T04:44:23 = -10 K3

=head3 yyMM (MM/yy)

   2008-02-05T18:30:30 = 02/08
   1995-12-22T09:05:02 = 12/95
  -0010-09-15T04:44:23 = 09/-10

=head3 yyMMM (MMM yy)

   2008-02-05T18:30:30 = lut 08
   1995-12-22T09:05:02 = gru 95
  -0010-09-15T04:44:23 = wrz -10

=head3 yyQ (Q yy)

   2008-02-05T18:30:30 = 1 08
   1995-12-22T09:05:02 = 4 95
  -0010-09-15T04:44:23 = 3 -10

=head3 yyyyMM (yyyy-MM)

   2008-02-05T18:30:30 = 2008-02
   1995-12-22T09:05:02 = 1995-12
  -0010-09-15T04:44:23 = -010-09

=head3 yyyyMMMM (LLLL y)

   2008-02-05T18:30:30 = luty 2008
   1995-12-22T09:05:02 = grudzień 1995
  -0010-09-15T04:44:23 = wrzesień -10

=head2 Miscellaneous

=head3 Prefers 24 hour time?

Yes

=head3 Local first day of the week

poniedziałek


=head1 SUPPORT

See L<DateTime::Locale>.

=head1 AUTHOR

Dave Rolsky <autarch@urth.org>

=head1 COPYRIGHT

Copyright (c) 2008 David Rolsky. All rights reserved. This program is
free software; you can redistribute it and/or modify it under the same
terms as Perl itself.

This module was generated from data provided by the CLDR project, see
the LICENSE.cldr in this distribution for details on the CLDR data's
license.

=cut
