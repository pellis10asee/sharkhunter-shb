# !!!!!!!   DO NOT EDIT THIS FILE   !!!!!!!
# This file is machine-generated by mktables from the Unicode
# database, Version 5.2.0.  Any changes made here will be lost!

# !!!!!!!   INTERNAL PERL USE ONLY   !!!!!!!
# This file is for internal use by the Perl program only.  The format and even
# the name or existence of this file are subject to change without notice.
# Don't use it directly.

# This file returns the 2048 code points in Unicode Version 5.2.0 that match
# any of the following regular expression constructs:
# 
#         \p{General_Category=Surrogate}
#         \p{Gc=Cs}
#         \p{Category=Surrogate}
#         \p{Is_General_Category=Cs}
#         \p{Is_Gc=Surrogate}
#         \p{Is_Category=Cs}
# 
#         \p{Surrogate}
#         \p{Is_Surrogate}
#         \p{Cs}
#         \p{Is_Cs}
# 
#     Note: Mostly not usable in Perl.
# 
# perluniprops.pod should be consulted for the syntax rules for any of these,
# including if adding or subtracting white space, underscore, and hyphen
# characters matters or doesn't matter, and other permissible syntactic
# variants.  Upper/lower case distinctions never matter.
# 
# A colon can be substituted for the equals sign, and anything to the left of
# the equals (or colon) can be combined with anything to the right.  Thus,
# for example,
#         \p{Is_Category: Surrogate}
# is also valid.
# 
# Surrogates are used exclusively for I/O in UTF-16, and should not appear in
# Unicode text, and hence their use will generate (usually fatal) messages
# 
# The format of the lines of this file is: START\tSTOP\twhere START is the
# starting code point of the range, in hex; STOP is the ending point, or if
# omitted, the range has just one code point.  Numbers in comments in
# [brackets] indicate how many code points are in the range.

return <<'END';
D800	DFFF	 # [2048]
END