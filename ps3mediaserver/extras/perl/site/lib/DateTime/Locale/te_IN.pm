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
# This file was generated from the source file te_IN.xml
# The source file version number was 1.56, generated on
# 2009/05/05 23:06:40.
#
# Do not edit this file directly.
#
###########################################################################

package DateTime::Locale::te_IN;

use strict;
use warnings;
use utf8;

use base 'DateTime::Locale::te';

sub cldr_version { return "1\.7\.1" }

{
    my $first_day_of_week = "7";
    sub first_day_of_week { return $first_day_of_week }
}

{
    my $glibc_date_format = "\%B\ \%d\ \%A\ \%Y";
    sub glibc_date_format { return $glibc_date_format }
}

{
    my $glibc_datetime_format = "\%B\ \%d\ \%A\ \%Y\ \%p\%I\.\%M\.\%S\ \%Z";
    sub glibc_datetime_format { return $glibc_datetime_format }
}

{
    my $glibc_time_format = "\%p\%I\.\%M\.\%S\ \%Z";
    sub glibc_time_format { return $glibc_time_format }
}

{
    my $glibc_time_12_format = "\%p\%I\.\%M\.\%S\ \%Z";
    sub glibc_time_12_format { return $glibc_time_12_format }
}

1;

__END__


=pod

=encoding utf8

=head1 NAME

DateTime::Locale::te_IN

=head1 SYNOPSIS

  use DateTime;

  my $dt = DateTime->now( locale => 'te_IN' );
  print $dt->month_name();

=head1 DESCRIPTION

This is the DateTime locale package for Telugu India.

=head1 DATA

This locale inherits from the L<DateTime::Locale::te> locale.

It contains the following data.

=head2 Days

=head3 Wide (format)

  సోమవారం
  మంగళవారం
  బుధవారం
  గురువారం
  శుక్రవారం
  శనివారం
  ఆదివారం

=head3 Abbreviated (format)

  సోమ
  మంగళ
  బుధ
  గురు
  శుక్ర
  శని
  ఆది

=head3 Narrow (format)

  సో
  మ
  భు
  గు
  శు
  శ
  ఆ

=head3 Wide (stand-alone)

  సోమవారం
  మంగళవారం
  బుధవారం
  గురువారం
  శుక్రవారం
  శనివారం
  ఆదివారం

=head3 Abbreviated (stand-alone)

  సోమ
  మంగళ
  బుధ
  గురు
  శుక్ర
  శని
  ఆది

=head3 Narrow (stand-alone)

  సో
  మ
  భు
  గు
  శు
  శ
  ఆ

=head2 Months

=head3 Wide (format)

  జనవరి
  ఫిబ్రవరి
  మార్చి
  ఏప్రిల్
  మే
  జూన్
  జూలై
  ఆగస్టు
  సెప్టెంబర్
  అక్టోబర్
  నవంబర్
  డిసెంబర్

=head3 Abbreviated (format)

  జనవరి
  ఫిబ్రవరి
  మార్చి
  ఏప్రిల్
  మే
  జూన్
  జూలై
  ఆగస్టు
  సెప్టెంబర్
  అక్టోబర్
  నవంబర్
  డిసెంబర్

=head3 Narrow (format)

  జ
  ఫి
  మ
  ఎ
  మె
  జు
  జు
  ఆ
  సె
  అ
  న
  డి

=head3 Wide (stand-alone)

  జనవరి
  ఫిబ్రవరి
  మార్చి
  ఏప్రిల్
  మే
  జూన్
  జూలై
  ఆగస్టు
  సెప్టెంబర్
  అక్టోబర్
  నవంబర్
  డిసెంబర్

=head3 Abbreviated (stand-alone)

  1
  2
  3
  4
  5
  6
  7
  8
  9
  10
  11
  12

=head3 Narrow (stand-alone)

  జ
  ఫి
  మ
  ఎ
  మె
  జు
  జు
  ఆ
  సె
  అ
  న
  డి

=head2 Quarters

=head3 Wide (format)

  ఒకటి 1
  రెండు 2
  మూడు 3
  నాలుగు 4

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

  ఒకటి 1
  రెండు 2
  మూడు 3
  నాలుగు 4

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

  ఈసాపూర్వ.
  సన్.

=head3 Abbreviated

  BCE
  CE

=head3 Narrow

  BCE
  CE

=head2 Date Formats

=head3 Full

   2008-02-05T18:30:30 = మంగళవారం 5 ఫిబ్రవరి 2008
   1995-12-22T09:05:02 = శుక్రవారం 22 డిసెంబర్ 1995
  -0010-09-15T04:44:23 = శనివారం 15 సెప్టెంబర్ -10

=head3 Long

   2008-02-05T18:30:30 = 5 ఫిబ్రవరి 2008
   1995-12-22T09:05:02 = 22 డిసెంబర్ 1995
  -0010-09-15T04:44:23 = 15 సెప్టెంబర్ -10

=head3 Medium

   2008-02-05T18:30:30 = 5 ఫిబ్రవరి 2008
   1995-12-22T09:05:02 = 22 డిసెంబర్ 1995
  -0010-09-15T04:44:23 = 15 సెప్టెంబర్ -10

=head3 Short

   2008-02-05T18:30:30 = 05-02-08
   1995-12-22T09:05:02 = 22-12-95
  -0010-09-15T04:44:23 = 15-09--10

=head3 Default

   2008-02-05T18:30:30 = 5 ఫిబ్రవరి 2008
   1995-12-22T09:05:02 = 22 డిసెంబర్ 1995
  -0010-09-15T04:44:23 = 15 సెప్టెంబర్ -10

=head2 Time Formats

=head3 Full

   2008-02-05T18:30:30 = 6:30:30 pm UTC
   1995-12-22T09:05:02 = 9:05:02 am UTC
  -0010-09-15T04:44:23 = 4:44:23 am UTC

=head3 Long

   2008-02-05T18:30:30 = 6:30:30 pm UTC
   1995-12-22T09:05:02 = 9:05:02 am UTC
  -0010-09-15T04:44:23 = 4:44:23 am UTC

=head3 Medium

   2008-02-05T18:30:30 = 6:30:30 pm
   1995-12-22T09:05:02 = 9:05:02 am
  -0010-09-15T04:44:23 = 4:44:23 am

=head3 Short

   2008-02-05T18:30:30 = 6:30 pm
   1995-12-22T09:05:02 = 9:05 am
  -0010-09-15T04:44:23 = 4:44 am

=head3 Default

   2008-02-05T18:30:30 = 6:30:30 pm
   1995-12-22T09:05:02 = 9:05:02 am
  -0010-09-15T04:44:23 = 4:44:23 am

=head2 Datetime Formats

=head3 Full

   2008-02-05T18:30:30 = మంగళవారం 5 ఫిబ్రవరి 2008 6:30:30 pm UTC
   1995-12-22T09:05:02 = శుక్రవారం 22 డిసెంబర్ 1995 9:05:02 am UTC
  -0010-09-15T04:44:23 = శనివారం 15 సెప్టెంబర్ -10 4:44:23 am UTC

=head3 Long

   2008-02-05T18:30:30 = 5 ఫిబ్రవరి 2008 6:30:30 pm UTC
   1995-12-22T09:05:02 = 22 డిసెంబర్ 1995 9:05:02 am UTC
  -0010-09-15T04:44:23 = 15 సెప్టెంబర్ -10 4:44:23 am UTC

=head3 Medium

   2008-02-05T18:30:30 = 5 ఫిబ్రవరి 2008 6:30:30 pm
   1995-12-22T09:05:02 = 22 డిసెంబర్ 1995 9:05:02 am
  -0010-09-15T04:44:23 = 15 సెప్టెంబర్ -10 4:44:23 am

=head3 Short

   2008-02-05T18:30:30 = 05-02-08 6:30 pm
   1995-12-22T09:05:02 = 22-12-95 9:05 am
  -0010-09-15T04:44:23 = 15-09--10 4:44 am

=head3 Default

   2008-02-05T18:30:30 = 5 ఫిబ్రవరి 2008 6:30:30 pm
   1995-12-22T09:05:02 = 22 డిసెంబర్ 1995 9:05:02 am
  -0010-09-15T04:44:23 = 15 సెప్టెంబర్ -10 4:44:23 am

=head2 Available Formats

=head3 d (d)

   2008-02-05T18:30:30 = 5
   1995-12-22T09:05:02 = 22
  -0010-09-15T04:44:23 = 15

=head3 EEEd (d EEE)

   2008-02-05T18:30:30 = 5 మంగళ
   1995-12-22T09:05:02 = 22 శుక్ర
  -0010-09-15T04:44:23 = 15 శని

=head3 Hm (H:mm)

   2008-02-05T18:30:30 = 18:30
   1995-12-22T09:05:02 = 9:05
  -0010-09-15T04:44:23 = 4:44

=head3 hm (h:mm a)

   2008-02-05T18:30:30 = 6:30 pm
   1995-12-22T09:05:02 = 9:05 am
  -0010-09-15T04:44:23 = 4:44 am

=head3 Hms (H:mm:ss)

   2008-02-05T18:30:30 = 18:30:30
   1995-12-22T09:05:02 = 9:05:02
  -0010-09-15T04:44:23 = 4:44:23

=head3 hms (h:mm:ss a)

   2008-02-05T18:30:30 = 6:30:30 pm
   1995-12-22T09:05:02 = 9:05:02 am
  -0010-09-15T04:44:23 = 4:44:23 am

=head3 M (L)

   2008-02-05T18:30:30 = 2
   1995-12-22T09:05:02 = 12
  -0010-09-15T04:44:23 = 9

=head3 Md (M-d)

   2008-02-05T18:30:30 = 2-5
   1995-12-22T09:05:02 = 12-22
  -0010-09-15T04:44:23 = 9-15

=head3 MEd (E, M-d)

   2008-02-05T18:30:30 = మంగళ, 2-5
   1995-12-22T09:05:02 = శుక్ర, 12-22
  -0010-09-15T04:44:23 = శని, 9-15

=head3 MMdd (dd-MM)

   2008-02-05T18:30:30 = 05-02
   1995-12-22T09:05:02 = 22-12
  -0010-09-15T04:44:23 = 15-09

=head3 MMM (LLL)

   2008-02-05T18:30:30 = 2
   1995-12-22T09:05:02 = 12
  -0010-09-15T04:44:23 = 9

=head3 MMMd (MMM d)

   2008-02-05T18:30:30 = ఫిబ్రవరి 5
   1995-12-22T09:05:02 = డిసెంబర్ 22
  -0010-09-15T04:44:23 = సెప్టెంబర్ 15

=head3 MMMEd (E MMM d)

   2008-02-05T18:30:30 = మంగళ ఫిబ్రవరి 5
   1995-12-22T09:05:02 = శుక్ర డిసెంబర్ 22
  -0010-09-15T04:44:23 = శని సెప్టెంబర్ 15

=head3 MMMMd (d MMMM)

   2008-02-05T18:30:30 = 5 ఫిబ్రవరి
   1995-12-22T09:05:02 = 22 డిసెంబర్
  -0010-09-15T04:44:23 = 15 సెప్టెంబర్

=head3 MMMMEd (E MMMM d)

   2008-02-05T18:30:30 = మంగళ ఫిబ్రవరి 5
   1995-12-22T09:05:02 = శుక్ర డిసెంబర్ 22
  -0010-09-15T04:44:23 = శని సెప్టెంబర్ 15

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

   2008-02-05T18:30:30 = మంగళ, 2008-2-5
   1995-12-22T09:05:02 = శుక్ర, 1995-12-22
  -0010-09-15T04:44:23 = శని, -10-9-15

=head3 yMMM (y MMM)

   2008-02-05T18:30:30 = 2008 ఫిబ్రవరి
   1995-12-22T09:05:02 = 1995 డిసెంబర్
  -0010-09-15T04:44:23 = -10 సెప్టెంబర్

=head3 yMMMEd (EEE, y MMM d)

   2008-02-05T18:30:30 = మంగళ, 2008 ఫిబ్రవరి 5
   1995-12-22T09:05:02 = శుక్ర, 1995 డిసెంబర్ 22
  -0010-09-15T04:44:23 = శని, -10 సెప్టెంబర్ 15

=head3 yMMMM (y MMMM)

   2008-02-05T18:30:30 = 2008 ఫిబ్రవరి
   1995-12-22T09:05:02 = 1995 డిసెంబర్
  -0010-09-15T04:44:23 = -10 సెప్టెంబర్

=head3 yQ (y Q)

   2008-02-05T18:30:30 = 2008 1
   1995-12-22T09:05:02 = 1995 4
  -0010-09-15T04:44:23 = -10 3

=head3 yQQQ (y QQQ)

   2008-02-05T18:30:30 = 2008 Q1
   1995-12-22T09:05:02 = 1995 Q4
  -0010-09-15T04:44:23 = -10 Q3

=head3 yyQ (Q yy)

   2008-02-05T18:30:30 = 1 08
   1995-12-22T09:05:02 = 4 95
  -0010-09-15T04:44:23 = 3 -10

=head3 yyyyMM (MM-yyyy)

   2008-02-05T18:30:30 = 02-2008
   1995-12-22T09:05:02 = 12-1995
  -0010-09-15T04:44:23 = 09--010

=head3 yyyyMMMM (MMMM y)

   2008-02-05T18:30:30 = ఫిబ్రవరి 2008
   1995-12-22T09:05:02 = డిసెంబర్ 1995
  -0010-09-15T04:44:23 = సెప్టెంబర్ -10

=head2 Miscellaneous

=head3 Prefers 24 hour time?

No

=head3 Local first day of the week

ఆదివారం


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
