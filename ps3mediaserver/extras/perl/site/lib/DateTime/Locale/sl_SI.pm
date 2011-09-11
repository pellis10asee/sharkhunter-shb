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
# This file was generated from the source file sl_SI.xml
# The source file version number was 1.51, generated on
# 2009/05/05 23:06:40.
#
# Do not edit this file directly.
#
###########################################################################

package DateTime::Locale::sl_SI;

use strict;
use warnings;
use utf8;

use base 'DateTime::Locale::sl';

sub cldr_version { return "1\.7\.1" }

{
    my $first_day_of_week = "1";
    sub first_day_of_week { return $first_day_of_week }
}

{
    my $glibc_date_format = "\%d\.\ \%m\.\ \%Y";
    sub glibc_date_format { return $glibc_date_format }
}

{
    my $glibc_date_1_format = "\%a\ \%b\ \%e\ \%H\:\%M\:\%S\ \%Z\ \%Y";
    sub glibc_date_1_format { return $glibc_date_1_format }
}

{
    my $glibc_datetime_format = "\%a\ \%d\ \%b\ \%Y\ \%T\ \%Z";
    sub glibc_datetime_format { return $glibc_datetime_format }
}

{
    my $glibc_time_format = "\%T";
    sub glibc_time_format { return $glibc_time_format }
}

1;

__END__


=pod

=encoding utf8

=head1 NAME

DateTime::Locale::sl_SI

=head1 SYNOPSIS

  use DateTime;

  my $dt = DateTime->now( locale => 'sl_SI' );
  print $dt->month_name();

=head1 DESCRIPTION

This is the DateTime locale package for Slovenian Slovenia.

=head1 DATA

This locale inherits from the L<DateTime::Locale::sl> locale.

It contains the following data.

=head2 Days

=head3 Wide (format)

  ponedeljek
  torek
  sreda
  četrtek
  petek
  sobota
  nedelja

=head3 Abbreviated (format)

  pon
  tor
  sre
  čet
  pet
  sob
  ned

=head3 Narrow (format)

  p
  t
  s
  č
  p
  s
  n

=head3 Wide (stand-alone)

  ponedeljek
  torek
  sreda
  četrtek
  petek
  sobota
  nedelja

=head3 Abbreviated (stand-alone)

  pon
  tor
  sre
  čet
  pet
  sob
  ned

=head3 Narrow (stand-alone)

  p
  t
  s
  č
  p
  s
  n

=head2 Months

=head3 Wide (format)

  januar
  februar
  marec
  april
  maj
  junij
  julij
  avgust
  september
  oktober
  november
  december

=head3 Abbreviated (format)

  jan
  feb
  mar
  apr
  maj
  jun
  jul
  avg
  sep
  okt
  nov
  dec

=head3 Narrow (format)

  j
  f
  m
  a
  m
  j
  j
  a
  s
  o
  n
  d

=head3 Wide (stand-alone)

  januar
  februar
  marec
  april
  maj
  junij
  julij
  avgust
  september
  oktober
  november
  december

=head3 Abbreviated (stand-alone)

  jan
  feb
  mar
  apr
  maj
  jun
  jul
  avg
  sep
  okt
  nov
  dec

=head3 Narrow (stand-alone)

  j
  f
  m
  a
  m
  j
  j
  a
  s
  o
  n
  d

=head2 Quarters

=head3 Wide (format)

  1. četrtletje
  2. četrtletje
  3. četrtletje
  4. četrtletje

=head3 Abbreviated (format)

  Q1
  Q2
  Q3
  Q4

=head3 Narrow (format)

  1
  2
  3
  4

=head3 Wide (stand-alone)

  1. četrtletje
  2. četrtletje
  3. četrtletje
  4. četrtletje

=head3 Abbreviated (stand-alone)

  Q1
  Q2
  Q3
  Q4

=head3 Narrow (stand-alone)

  1
  2
  3
  4

=head2 Eras

=head3 Wide

  pred našim štetjem
  naše štetje

=head3 Abbreviated

  pr. n. št.
  po Kr.

=head3 Narrow

  pr. n. št.
  po Kr.

=head2 Date Formats

=head3 Full

   2008-02-05T18:30:30 = torek, 05. februar 2008
   1995-12-22T09:05:02 = petek, 22. december 1995
  -0010-09-15T04:44:23 = sobota, 15. september -10

=head3 Long

   2008-02-05T18:30:30 = 05. februar 2008
   1995-12-22T09:05:02 = 22. december 1995
  -0010-09-15T04:44:23 = 15. september -10

=head3 Medium

   2008-02-05T18:30:30 = 5. feb. 2008
   1995-12-22T09:05:02 = 22. dec. 1995
  -0010-09-15T04:44:23 = 15. sep. -010

=head3 Short

   2008-02-05T18:30:30 = 5. 02. 08
   1995-12-22T09:05:02 = 22. 12. 95
  -0010-09-15T04:44:23 = 15. 09. -10

=head3 Default

   2008-02-05T18:30:30 = 5. feb. 2008
   1995-12-22T09:05:02 = 22. dec. 1995
  -0010-09-15T04:44:23 = 15. sep. -010

=head2 Time Formats

=head3 Full

   2008-02-05T18:30:30 = 18:30:30 UTC
   1995-12-22T09:05:02 = 9:05:02 UTC
  -0010-09-15T04:44:23 = 4:44:23 UTC

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

   2008-02-05T18:30:30 = torek, 05. februar 2008 18:30:30 UTC
   1995-12-22T09:05:02 = petek, 22. december 1995 9:05:02 UTC
  -0010-09-15T04:44:23 = sobota, 15. september -10 4:44:23 UTC

=head3 Long

   2008-02-05T18:30:30 = 05. februar 2008 18:30:30 UTC
   1995-12-22T09:05:02 = 22. december 1995 09:05:02 UTC
  -0010-09-15T04:44:23 = 15. september -10 04:44:23 UTC

=head3 Medium

   2008-02-05T18:30:30 = 5. feb. 2008 18:30:30
   1995-12-22T09:05:02 = 22. dec. 1995 09:05:02
  -0010-09-15T04:44:23 = 15. sep. -010 04:44:23

=head3 Short

   2008-02-05T18:30:30 = 5. 02. 08 18:30
   1995-12-22T09:05:02 = 22. 12. 95 09:05
  -0010-09-15T04:44:23 = 15. 09. -10 04:44

=head3 Default

   2008-02-05T18:30:30 = 5. feb. 2008 18:30:30
   1995-12-22T09:05:02 = 22. dec. 1995 09:05:02
  -0010-09-15T04:44:23 = 15. sep. -010 04:44:23

=head2 Available Formats

=head3 d (d)

   2008-02-05T18:30:30 = 5
   1995-12-22T09:05:02 = 22
  -0010-09-15T04:44:23 = 15

=head3 EEEd (d EEE)

   2008-02-05T18:30:30 = 5 tor
   1995-12-22T09:05:02 = 22 pet
  -0010-09-15T04:44:23 = 15 sob

=head3 HHmm (HH:mm)

   2008-02-05T18:30:30 = 18:30
   1995-12-22T09:05:02 = 09:05
  -0010-09-15T04:44:23 = 04:44

=head3 HHmmss (HH:mm:ss)

   2008-02-05T18:30:30 = 18:30:30
   1995-12-22T09:05:02 = 09:05:02
  -0010-09-15T04:44:23 = 04:44:23

=head3 Hm (H:mm)

   2008-02-05T18:30:30 = 18:30
   1995-12-22T09:05:02 = 9:05
  -0010-09-15T04:44:23 = 4:44

=head3 hm (h:mm a)

   2008-02-05T18:30:30 = 6:30 pop.
   1995-12-22T09:05:02 = 9:05 dop.
  -0010-09-15T04:44:23 = 4:44 dop.

=head3 Hms (H:mm:ss)

   2008-02-05T18:30:30 = 18:30:30
   1995-12-22T09:05:02 = 9:05:02
  -0010-09-15T04:44:23 = 4:44:23

=head3 hms (h:mm:ss a)

   2008-02-05T18:30:30 = 6:30:30 pop.
   1995-12-22T09:05:02 = 9:05:02 dop.
  -0010-09-15T04:44:23 = 4:44:23 dop.

=head3 M (L)

   2008-02-05T18:30:30 = 2
   1995-12-22T09:05:02 = 12
  -0010-09-15T04:44:23 = 9

=head3 Md (d. M.)

   2008-02-05T18:30:30 = 5. 2.
   1995-12-22T09:05:02 = 22. 12.
  -0010-09-15T04:44:23 = 15. 9.

=head3 MEd (E, M-d)

   2008-02-05T18:30:30 = tor, 2-5
   1995-12-22T09:05:02 = pet, 12-22
  -0010-09-15T04:44:23 = sob, 9-15

=head3 MMM (LLL)

   2008-02-05T18:30:30 = feb
   1995-12-22T09:05:02 = dec
  -0010-09-15T04:44:23 = sep

=head3 MMMd (MMM d)

   2008-02-05T18:30:30 = feb 5
   1995-12-22T09:05:02 = dec 22
  -0010-09-15T04:44:23 = sep 15

=head3 MMMEd (E MMM d)

   2008-02-05T18:30:30 = tor feb 5
   1995-12-22T09:05:02 = pet dec 22
  -0010-09-15T04:44:23 = sob sep 15

=head3 MMMMd (d. MMMM)

   2008-02-05T18:30:30 = 5. februar
   1995-12-22T09:05:02 = 22. december
  -0010-09-15T04:44:23 = 15. september

=head3 MMMMdd (dd. MMMM)

   2008-02-05T18:30:30 = 05. februar
   1995-12-22T09:05:02 = 22. december
  -0010-09-15T04:44:23 = 15. september

=head3 MMMMEd (E MMMM d)

   2008-02-05T18:30:30 = tor februar 5
   1995-12-22T09:05:02 = pet december 22
  -0010-09-15T04:44:23 = sob september 15

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

=head3 yM (y-M)

   2008-02-05T18:30:30 = 2008-2
   1995-12-22T09:05:02 = 1995-12
  -0010-09-15T04:44:23 = -10-9

=head3 yMEd (EEE, y-M-d)

   2008-02-05T18:30:30 = tor, 2008-2-5
   1995-12-22T09:05:02 = pet, 1995-12-22
  -0010-09-15T04:44:23 = sob, -10-9-15

=head3 yMMM (y MMM)

   2008-02-05T18:30:30 = 2008 feb
   1995-12-22T09:05:02 = 1995 dec
  -0010-09-15T04:44:23 = -10 sep

=head3 yMMMEd (EEE, y MMM d)

   2008-02-05T18:30:30 = tor, 2008 feb 5
   1995-12-22T09:05:02 = pet, 1995 dec 22
  -0010-09-15T04:44:23 = sob, -10 sep 15

=head3 yMMMM (y MMMM)

   2008-02-05T18:30:30 = 2008 februar
   1995-12-22T09:05:02 = 1995 december
  -0010-09-15T04:44:23 = -10 september

=head3 yQ (y Q)

   2008-02-05T18:30:30 = 2008 1
   1995-12-22T09:05:02 = 1995 4
  -0010-09-15T04:44:23 = -10 3

=head3 yQQQ (y QQQ)

   2008-02-05T18:30:30 = 2008 Q1
   1995-12-22T09:05:02 = 1995 Q4
  -0010-09-15T04:44:23 = -10 Q3

=head3 yyQ (Q/yy)

   2008-02-05T18:30:30 = 1/08
   1995-12-22T09:05:02 = 4/95
  -0010-09-15T04:44:23 = 3/-10

=head3 yyyyM (M/yyyy)

   2008-02-05T18:30:30 = 2/2008
   1995-12-22T09:05:02 = 12/1995
  -0010-09-15T04:44:23 = 9/-010

=head3 yyyyMMMM (MMMM y)

   2008-02-05T18:30:30 = februar 2008
   1995-12-22T09:05:02 = december 1995
  -0010-09-15T04:44:23 = september -10

=head2 Miscellaneous

=head3 Prefers 24 hour time?

Yes

=head3 Local first day of the week

ponedeljek


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
