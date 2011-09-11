# This file is auto-generated by the Perl DateTime Suite time zone
# code generator (0.07) This code generator comes with the
# DateTime::TimeZone module distribution in the tools/ directory

#
# Generated from ../DateTime/data/Olson/2011i/africa.  Olson data version 2011i
#
# Do not edit this file directly.
#
package DateTime::TimeZone::Africa::Mbabane;
{
  $DateTime::TimeZone::Africa::Mbabane::VERSION = '1.36';
}

use strict;

use Class::Singleton;
use DateTime::TimeZone;
use DateTime::TimeZone::OlsonDB;

@DateTime::TimeZone::Africa::Mbabane::ISA = ( 'Class::Singleton', 'DateTime::TimeZone' );

my $spans =
[
    [
DateTime::TimeZone::NEG_INFINITY,
60026392536,
DateTime::TimeZone::NEG_INFINITY,
60026400000,
7464,
0,
'LMT'
    ],
    [
60026392536,
DateTime::TimeZone::INFINITY,
60026399736,
DateTime::TimeZone::INFINITY,
7200,
0,
'SAST'
    ],
];

sub olson_version { '2011i' }

sub has_dst_changes { 0 }

sub _max_year { 2021 }

sub _new_instance
{
    return shift->_init( @_, spans => $spans );
}



1;

