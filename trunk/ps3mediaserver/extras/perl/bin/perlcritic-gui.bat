@rem = '--*-Perl-*--
@echo off
if "%OS%" == "Windows_NT" goto WinNT
perl -x -S "%0" %1 %2 %3 %4 %5 %6 %7 %8 %9
goto endofperl
:WinNT
perl -x -S %0 %*
if NOT "%COMSPEC%" == "%SystemRoot%\system32\cmd.exe" goto endofperl
if %errorlevel% == 9009 echo You do not have Perl in your PATH.
if errorlevel 1 goto script_failed_so_exit_with_non_zero_val 2>nul
goto endofperl
@rem ';
#!/usr/bin/perl -w
#line 15

use strict;

our $VERSION = "10.0.0";

use ActiveState::Tkx qw($mw);
use ActiveState::Tkx::Util qw(mydie set_application_icon add_help accel_bind selection_event focus_event);
use ActiveState::PerlCritic::Util qw(get_image appdata_location);
use ActiveState::Tkx::TextSyntaxTags qw(update_syntax_tags);
use ActiveState::OSType qw(IS_WIN32 IS_DARWIN IS_UNIX);
use ActiveState::Handy qw(file_content);
use ActiveState::Path qw(abs_path);
use ActiveState::Run qw(shell_quote);

use File::Basename qw(basename dirname);
use File::Temp qw();
use File::Spec::Functions qw(devnull catfile);
use Getopt::Long qw(GetOptions);
use Config qw(%Config);
use Pod::Text;
use Storable ();

BEGIN {
    # These can fail during intialization
    eval {
	require ActiveState::PerlCritic::UserProfile;
    };
    if ($@) {
	mydie("Failed to initialize the PerlCritic application\n\n$@");
    }
}

sub perlcritic_cmd {
    return shell_quote("perlcritic", @_) unless wantarray;
    return "perlcritic", @_;
}

my $progname = $0;
$progname =~ s,.*[\\/],,;
$progname =~ s/\.pl$//;

our $DEBUG;
my $opt_run,
my $opt_version;
my $from_app;
GetOptions(
    'debug' => \$DEBUG,
    'version' => \$opt_version,
    'from-app!' => \$from_app,
    'run' => \$opt_run,
) || usage();

my $opt_profile = shift;
my $opt_source = shift;
usage() if @ARGV;

if ($opt_profile) {
    unless (-e $opt_profile) {
	my $d = dirname($opt_profile);
	mydie("Bad profile: Directory $d does not exist\n") unless -d $d;
    }
    $opt_profile = abs_path($opt_profile);
    $opt_profile = catfile($opt_profile, ".perlcriticrc") if -d $opt_profile;
}

unless ($opt_source) {
    if ($opt_profile) {
	my $d = dirname($opt_profile);
	$opt_source = $d if looks_like_source_directory($d);
    }
}

if (IS_WIN32) {
    Win32::SetChildShowWindow(0);
    if (Win32::IsWin95()) {
        # backticks and popen won't work without this
        open(STDIN, "<", "nul");
        open(STDOUT, ">", "nul");
        open(STDERR, ">", "nul");
    }
}

my $perlcritic_version = do {
    my $cmd = perlcritic_cmd("--version");
    qx($cmd);
};
chomp($perlcritic_version);

if ($opt_version) {
    version();
    exit;
}

$ActiveState::Browser::HTML_DIR = $Config{installhtmldir} || "$Config{prefix}/html";
my $IS_AQUA = Tkx::tk("windowingsystem") eq "aqua";

if ($IS_AQUA) {
    Tkx::set("::tk::mac::useThemedToplevel" => 1);
    # The console can pop up unexpectedly from the tkkit
    Tkx::catch("console hide");
    eval {
        Tkx::package_require('tclCarbonProcesses');
        my $psn = Tkx::carbon__getCurrentProcess();
        Tkx::carbon__setFrontProcess($psn);
        Tkx::carbon__setProcessName($psn, "Perl Critic");
    };
    warn $@ if $@ && $DEBUG;
    Tkx::event_add('<<PopupMenu>>', '<Button-2>', '<Control-Button-1>');
} else {
    Tkx::event_add('<<PopupMenu>>', '<Button-3>');
}

if ($DEBUG) {
    $Tcl::STACK_TRACE = 1;

    # This allows us to control PAI with TDK's Inspector
    # Remove before release
    unless (Tkx::catch('package require comm')) {
	my $port = Tkx::comm__comm('self');
	print STDERR "COMM PORT $port\n";
    }
}

my $DIRTY = 0;
my %output;
my $last_theme = 'All';
my $last_file = "";
my $last_viewed = "";
my $last_dir = "";
my $STATUS = "";
my $status_afterid = "";
my $status_delay = 500;
my $profile;
my $tmp_profile_file;
my $UNTITLED = "<UNTITLED>";

# Colors tied to severity 1..5
my @COLORS = ('#FFFFCC', '#FFFF88', '#FFCCCC', '#FF8888', '#FF5555');

my $APPDATA_LOCATION = eval { appdata_location('PerlCritic') };
if ($@) {
    mydie("Error getting ApplicationData area: $@");
}
my $PREFS_FILE = catfile($APPDATA_LOCATION, "prefs-1.sto");
my %globalPrefs;
if (-f $PREFS_FILE) {
    my $h = Storable::retrieve($PREFS_FILE);
    %globalPrefs = %$h;
}

$globalPrefs{view_toolbar} = 1 unless defined($globalPrefs{view_toolbar});
$globalPrefs{view_statusbar} = 1 unless defined($globalPrefs{view_statusbar});
$globalPrefs{view_stats} = 0 unless defined($globalPrefs{view_stats});
$globalPrefs{view_line} = 1 unless defined($globalPrefs{view_line});
$globalPrefs{view_tree_policy} = 0 unless defined($globalPrefs{view_tree_policy});
$globalPrefs{view_tree_severity} = 1 unless defined($globalPrefs{view_tree_severity});
$globalPrefs{view_tree_line} = 1 unless defined($globalPrefs{view_tree_line});
$globalPrefs{view_tree_col} = 0 unless defined($globalPrefs{view_tree_col});
$globalPrefs{mrufiles} = [] unless defined($globalPrefs{mrufiles});

@{$globalPrefs{mrufiles}} = grep -f $_, @{$globalPrefs{mrufiles}};

unless ($opt_profile) {
    my $f = $globalPrefs{lastProfileFile};
    $opt_profile = $f if $f && -f $f;
}


# --- exposed widgets ---

my %win;

# --- set up GUI ---

if (IS_DARWIN && !$from_app && !$DEBUG) {
    my $app = dirname(__FILE__) . "/PerlCritic.app";
    system("/usr/bin/open", $app);
    die "Failed to open $app" if $? != 0;
    exit;
}

Tkx::package_require('Tk', '8.5'); # use Tk with ttk in core
Tkx::package_require('treectrl');
Tkx::package_require('tooltip');
Tkx::package_require('widget::scrolledwindow');
Tkx::package_require('widget::statusbar');
Tkx::package_require('widget::toolbar');
Tkx::package_require('img::png');

style_init();

if (my $geo = $globalPrefs{geometry}) {
    # $geo =~ s/^\d+x\d+//;
    #XXX: Don't let the window go off-screen.
    #$mw->g_wm_geometry($geo);
}
my $wm_state = delete $globalPrefs{wm_state} || "normal";
if ($wm_state eq 'zoomed') {
    #$mw->g_wm_state('zoomed');
}

$mw->configure(-menu => mk_menu($mw));
$mw->g_wm_protocol("WM_DELETE_WINDOW", \&exit_program);
$mw->g_wm_title("PerlCritic");
set_application_icon(mw => $mw, basename => "perlcritic");

# TOOLBAR
my $tbar = $mw->new_widget__toolbar(-separator => 'bottom');
$win{toolbar} = $tbar;

my $tbrun = $tbar->new_ttk__button(
    -text => "Run",
    -style => 'Toolbutton',
    -image => [get_image('control_play_blue'), # a |> play image
	       disabled => get_image('control_play'), # grey play image
	       alternate => get_image('control_stop_blue')], # a stop image
    -state => 'disabled',
    -command => [\&run_perlcritic],
    );
Tkx::tooltip__tooltip($tbrun, "Run PerlCritic");
$win{tbrunbutton} = $tbrun;

my $diskimg = get_image('disk');
# create a darker (disabled) version of diskimg
my $darkimg = Tkx::image_create_photo(
    -width => Tkx::image_width($diskimg),
    -height => Tkx::image_height($diskimg),
    );
Tkx::eval("$darkimg copy $diskimg");
Tkx::imagetint($darkimg, '#000', 50);
my $tbsave = $tbar->new_ttk__button(
    -style => 'Toolbutton',
    -image => [$diskimg, disabled => $darkimg],
    -command => [\&save],
    -state => 'disabled',
    );
Tkx::tooltip__tooltip($tbsave, "Save profile");
$win{save} = $tbsave;

# A combobox serves as MRU file list and mirrors MRU menu
my $tblbl = $tbar->new_ttk__label(-text => 'Profile:');
my $profile_name;
my $tbprofile;
if ($IS_AQUA) {
    $tbprofile = $tbar->new_ttk__menubutton(
	-textvariable => \$profile_name,
	-direction => 'below',
	-menu => $win{mru},
	);
} else {
    $tbprofile = $tbar->new_ttk__combobox(
	-textvariable => \$profile_name,
	-state => 'readonly',
	-postcommand => sub {
	    $tbprofile->configure(-values => $globalPrefs{mrufiles});
	},
	);
    $tbprofile->g_bind('<<ComboboxSelected>>',
		       sub { load_profile($tbprofile->get); });
}
$win{profile} = $tbprofile;

my $tbfile = $tbar->new_ttk__button(
    -style => 'Toolbutton',
    -image => get_image('script'), # a file image
    -command => [\&open_profile],
    );
Tkx::tooltip__tooltip($tbfile, "Open profile");
my $tbdir = $tbar->new_ttk__button(
    -style => 'Toolbutton',
    -image => get_image('folder'), # a directory image
    -command => [\&open_profile_dir],
    );
Tkx::tooltip__tooltip($tbdir, "Open profile directory");
my $tbuser = $tbar->new_ttk__button(
    -style => 'Toolbutton',
    -image => get_image('folder_user'), # a directory image with user
    -command => [\&open_default],
    );
Tkx::tooltip__tooltip($tbuser, "Open default user profile");

$tbar->add($tbrun);
$tbar->add_separator();
$tbar->add($tbfile);
$tbar->add($tbdir);
$tbar->add($tbuser);
$tbar->add($tbsave);
$tbar->add($tblbl);
$tbar->add($tbprofile, -weight => 1, -sticky => 'ew');

# PANEWINDOW FOR MAIN BODY
my $pane = $mw->new_ttk__panedwindow(
    -orient => "horizontal",
);

# LEFT HAND SIDE
my $lhs = $mw->new_ttk__frame();

# Combobox to filter based on theme
$win{themes} = $lhs->new_ttk__combobox(-state => 'readonly');
my $tlbl = $lhs->new_ttk__label(
    -text => "Filter:"
);
$win{themes}->g_bind('<<ComboboxSelected>>', \&filter_tree);

# Tree widget for policies and namespaces
my $tsw = mk_tree($lhs); # will populate $win{tree}

$tlbl->g_grid(-row => 0, -column => 0, -padx => 2, -sticky => "ew");
$win{themes}->g_grid(-row => 0, -column => 1, -sticky => "ew");
$tsw->g_grid(-row => 1, -columnspan => 2, -sticky => "nsew",
	     -pady => [2, 0]);
$lhs->g_grid_rowconfigure("1", -weight => 1);
$lhs->g_grid_columnconfigure("1", -weight => 1);

# RIGHT HAND SIDE
my $rhs = $mw->new_ttk__notebook(-padding => 0);
$win{notebook} = $rhs;

# "DETAILS" Text widget for descriptions
my $details = $rhs->new_ttk__frame(-padding => 4);
$win{attrs} = $details->new_ttk__frame();
my $dsw = $details->new_widget__scrolledwindow(
    -borderwidth => 1,
    -relief => "sunken",
);
$win{desc} = $dsw->new_text(
    -height => 10,
    -width => 70,
    -highlightthickness => 0,
    -borderwidth => 0,
    -padx => 10, -pady => 5,
    -wrap => 'word',
);
$dsw->setwidget($win{desc});
$win{desc}->configure(-state => 'disabled');
$win{desc}->tag_configure('header', -font => 'ASfontBold1');
$win{desc}->tag_configure('abstract',
			  -lmargin1 => 20, -lmargin2 => 20, -rmargin => 5,
			  -font => 'ASfont');
$win{desc}->tag_configure('code',
			  -foreground => "#660000", -wrap => 'none');
$win{desc}->tag_raise('sel');

$win{attrs}->g_grid(-row => 0, -sticky => "nsew");
$dsw->g_grid(-row => 1, -sticky => "nsew");
$details->g_grid_rowconfigure("1", -weight => 1);
$details->g_grid_columnconfigure("0", -weight => 1);

# "RUN" Run tab
my $runf = $rhs->new_ttk__frame(-padding => 4);

my $sfrm = $runf->new_ttk__frame();
my $rlbl = $sfrm->new_ttk__label(-text => "Sources:");
my $rent = $sfrm->new_ttk__entry(
    -state => 'readonly',
    -textvariable => \$globalPrefs{lastSourceFile},
    );
my $rdir = $sfrm->new_ttk__button(
    -style => 'Toolbutton',
    -image => get_image('folder'), # a directory image
    -command => [\&open_run_dirfile, 'Directory'],
    );
Tkx::tooltip__tooltip($rdir, "Select sources directory to run PerlCritic on (recursive includes Perl files)");
my $rfile = $sfrm->new_ttk__button(
    -style => 'Toolbutton',
    -image => get_image('script_code'), # a file image
    -command => [\&open_run_dirfile, 'File'],
    );
Tkx::tooltip__tooltip($rfile, "Select single file for PerlCritic");

my $rsep = $sfrm->new_ttk__separator(-orient => 'vertical');
my $rrun = $sfrm->new_ttk__button(
    -text => "Run",
    -style => 'Toolbutton',
    -image => [get_image('control_play_blue'), # a |> play image
	       disabled => get_image('control_play'), # grey play image
	       alternate => get_image('control_stop_blue')], # a stop image
    -state => 'disabled',
    -command => [\&run_perlcritic],
    );
Tkx::tooltip__tooltip($rrun, "Run PerlCritic over specified sources");
$win{runbutton} = $rrun;

Tkx::grid($rlbl, $rent, $rfile, $rdir, $rsep, $rrun, -sticky => "ew");
$sfrm->g_grid_columnconfigure("1", -weight => 1);
$rsep->g_grid_configure(-sticky => 'ns', -padx => 4);

$rlbl = $sfrm->new_ttk__label(-text => "Strictness:");
my @strictvals = ("Brutal (report all violations)",
		  "Cruel (report severity 2 to 5 violations)",
		  "Harsh (report severity 3, 4 and 5 violation)",
		  "Stern (report severity 4 and 5 violations)",
		  "Gentle (report severity 5 violations only)");
my $strictness;
sub strictness_num {
    my $old = $strictness;
    if (@_) {
        my $n = shift;
        die "strictness value $n out of range" if $n < 1 || $n > @strictvals;
        $strictness = $strictvals[$n - 1];
    }
    return $old && $old =~ /(\d+)/ ? $1 : 1;
}

sub update_strictness {
    $profile->param("severity", strictness_num());
    dirty();
}

my $smenu;
if ($IS_AQUA) {
    $smenu = $sfrm->new_ttk__menubutton(
	-textvariable => \$strictness,
	-direction => 'flush',
	);
    my $menu = $smenu->new_menu(-tearoff => 0);
    $menu->add_radiobutton(
	-label => $_, -value => $_,
	-variable => \$strictness,
        -command => \&update_strictness,
	) for @strictvals;
     $smenu->configure(-menu => $menu);
} else {
    $smenu = $sfrm->new_ttk__combobox(
	-values => \@strictvals,
	-textvariable => \$strictness,
	-state => "readonly",
	-exportselection => 0,
	);
    $smenu->g_bind("<<ComboboxSelected>>", \&update_strictness);
}
Tkx::grid($rlbl, $smenu, -sticky => "ew", -pady => [2, 4]);

my $rpane = $runf->new_ttk__panedwindow(-orient => 'vertical');

# This tricks out the themed treeview to prevent the border which is not
# as "theme appropriate" on all platforms.  I consider this a core nit. JH
Tkx::eval('
    ttk::style layout Treeview {
	Treeview.padding -sticky news -children {
	    Treeview.treearea -sticky news
	}
    }
');

# Treeview multi-column list of warnings / errors
$dsw = $rpane->new_widget__scrolledwindow(
    -borderwidth => 1,
    -relief => "sunken",
);
my $rtree = $dsw->new_ttk__treeview(
    -height => 6,
    -columns => ['file', 'line', 'col', 'reason', 'policy', 'severity'],
    #-show => 'headings',
    -selectmode => 'browse',
);
$win{runtree} = $rtree;
$dsw->setwidget($win{runtree});
$rpane->add($dsw, -weight => 1);
runtree_cols();

$rtree->heading('#0', -text => "File");
$rtree->column('file', -stretch => 1, -width => 200);
$rtree->heading('line', -text => "Line");
$rtree->column('line', -stretch => 0, -width => 40, -anchor => 'e');
$rtree->heading('col', -text => "Col");
$rtree->column('col', -stretch => 0, -width => 25, -anchor => 'e');
$rtree->heading('reason', -text => "Reason");
$rtree->column('reason', -stretch => 1, -width => 240);
$rtree->heading('policy', -text => "Policy");
$rtree->column('policy', -stretch => 0, -width => 160);
$rtree->heading('severity', -text => "Sev");
$rtree->column('severity', -stretch => 0, -width => 30, -anchor => 'c');

$rtree->g_bind('<<TreeviewSelect>>', [\&runtree_select]);
$rtree->g_bind('<Double-Button-1>',
	       [\&runtree_action, Tkx::Ev('%x', '%y'), 'double']);
$rtree->g_bind('<<PopupMenu>>',
	       [\&runtree_action, Tkx::Ev('%x', '%y', '%X', '%Y'), 'popup']);

# Code view
$dsw = $rpane->new_widget__scrolledwindow(
    -borderwidth => 1,
    -relief => "sunken",
);
my $rtext = $dsw->new_text(
    -height => 12,
    -width => 80,
    -highlightthickness => 0,
    -borderwidth => 0,
    -padx => 10, -pady => 5,
    -state => 'disabled',
    -wrap => 'none',
);
$win{source} = $rtext;
$dsw->setwidget($win{source});
$rpane->add($dsw, -weight => 2);

for (1..5) {
    # XXX We need tile 0.8.3 or Tk 8.6 (or backport of Tk 8.6 treeview fixes)
    # XXX to get correct highlighting of selection on colored treeview items
    #$rtree->tag_configure("sev$_", -background => $COLORS[$_-1]);
    $rtext->tag_configure("sev$_", -background => $COLORS[$_-1]);
}
$rtext->tag_configure('line', -elide => !$globalPrefs{view_line});
$rtext->tag_configure('curline', -underline => 1);
$rtext->tag_raise("sel");

# Verbose statistics output
$dsw = $rpane->new_widget__scrolledwindow(
    -borderwidth => 1,
    -relief => "sunken",
);
$win{run} = $dsw->new_text(
    -height => 4,
    -width => 80,
    -highlightthickness => 0,
    -borderwidth => 0,
    -padx => 10, -pady => 5,
    -state => 'disabled',
    -wrap => 'none',
);
$dsw->setwidget($win{run});
$rpane->add($dsw, -weight => 1);
$rpane->forget($dsw) unless $globalPrefs{view_stats};

# Remember to control view_stats
$win{rundsw} = $dsw;
$win{runpane} = $rpane;

$sfrm->g_grid(-sticky => "ew");
$rpane->g_grid(-sticky => 'nsew');
$runf->g_grid_rowconfigure("1", -weight => 1);
$runf->g_grid_columnconfigure("0", -weight => 1);

$rhs->add(
    $details,
    -text => "Details",
);

$rhs->add(
    $runf,
    -text => "Run",
);

# Statusbar and outer widget geometry
my $stat = $mw->new_widget__statusbar(
    -name => "status",
);
my $lstat = $stat->new_ttk__label(-width => 1, -textvariable => \$STATUS);
$win{progress} = $stat->new_ttk__progressbar(
    -orient => "horizontal",
    -mode => "indeterminate",
    );
$win{status} = $lstat;
$win{statusbar} = $stat;
$stat->add($lstat, -weight => 1, -sticky => "ew");

$pane->add($lhs, -weight => 1);
$pane->add($rhs, -weight => 2);

# Only need menu/body separator on only Windows
$mw->new_ttk__separator(
    -orient => "horizontal",
)->g_grid(-row => 0, -column => 0, -sticky => "ew")
    if (IS_WIN32 && (Tkx::ttk__style_theme_names() !~ /xpnative/));

$tbar->g_grid(-row => 1, -column => 0, -sticky => "ew");
$pane->g_grid(-row => 2, -column => 0, -sticky => "nsew",
	      -padx => 4, -pady => 2);
$stat->g_grid(-row => 5, -column => 0, -sticky => "ew");

$tbar->g_grid_remove() unless $globalPrefs{view_toolbar};
$stat->g_grid_remove() unless $globalPrefs{view_statusbar};

$mw->g_grid_rowconfigure("2", -weight => 1);
$mw->g_grid_columnconfigure("0", -weight => 1);

if (defined($globalPrefs{lastTab})) {
    # Ignore errors that may occur through UI changes
    eval { $win{notebook}->select($globalPrefs{lastTab}); };
    warn $@ if $@ && $DEBUG;
}
$win{notebook}->g_bind('<<NotebookTabChanged>>',
		       sub { $globalPrefs{lastTab} = $win{notebook}->select(); });

open_run_dirfile(set => $opt_source || $globalPrefs{lastSourceFile});

Tkx::update('idletasks');

$mw->g_wm_deiconify();
$mw->g_raise();

Tkx::update();

Tkx::after_idle(sub {
    # check that we can obtain the list of policies provided
    eval {
        ActiveState::PerlCritic::UserProfile->new->policies;
    };
    if ($@) {
	mydie($@);
    }
    load_profile($opt_profile);

    dtext_append($win{desc},
        "Select policy from tree at left to see description", "header");
    dtext_append($win{run}, "perlcritic $perlcritic_version\n")
	if $perlcritic_version;
    run_perlcritic() if $opt_run;
});

Tkx::MainLoop();
exit;
#-------------------------------------------------------------------------

sub populate_tree {
    my $t = $win{tree};
    my $name = shift;

    Tkx::ttk__style_layout('Slim.TCheckbutton',
			   ['Checkbutton.indicator', -sticky => 'nsew']);

    eval { $t->item_delete("root descendants") if $t->item_children("root"); };
    warn $@ if $@ && $DEBUG;

    my %themes;
    for my $sname ($profile->policies) {
        my $policy = $profile->policy($sname);
	my @themes = split(" ", $policy->themes);
	push @{$themes{$_}}, $policy for @themes;
	# differentiate policy-added tag names
	@themes = map { "theme_$_" } @themes;
	my @names = split(/::/, $sname);
	my ($isleaf, $item, $chk, $dname, $last, $tag);
	for my $name (@names) {
	    if (($isleaf = ($sname =~ /::\Q$name\E\z/))) {
		$tag = "policy_$name";
	    } else {
		$tag = "group_$name";
	    }
	    if (($item = $t->item_id("tag $tag")) eq "") {
		# need to watch tags => assumes each sub element name is uniq
		$item = $t->item_create(
		    -parent => $last || "root",
		    -open => 0,
		    -button => "auto",
		    -tags => [$tag, @themes],
		);
		if ($isleaf) {
		    $t->item_tag_add($item, 'policy');
		    $t->item_style_set($item, policies => 'styMixed');
		    $t->item_element_configure(
			$item, 'policies', 'elemNum', -data => 0,
			);
		} else {
		    $t->item_tag_add($item, 'group');
		}
		for ($dname = $name) {
		    s/([^A-Z])([A-Z])/$1 $2/g;
		    s/([A-Z][^A-Z])/ $1/g;
		    s/^ //;
		    s/ +/ /g;
		}
		$t->item_text($item, policies => $dname);
	    }
            if ($isleaf) {
                my $state = $policy->state;
		print STDERR "STATE $sname $state\n" if ($DEBUG && $state ne "unconfigured");
                if ($state eq "enabled") {
                    $t->item_state_set($item, "p_enabled !p_unconfigured");
                }
                elsif ($state eq "disabled") {
                    $t->item_state_set($item, "!p_enabled !p_unconfigured");
                }
                else { # unconfigured
                    $t->item_state_set($item, "!p_enabled p_unconfigured");
                }
            }
            else {
                $t->item_state_set($item, "!p_enabled !p_unconfigured");
            }

	    $last = $tag;
	}
	$t->item_tag_add("$item ancestors", \@themes);
    }

    $win{themes}->configure(-values => [
	"All",
	"Configured",
	"Current Run",
	map { "Theme $_" } sort keys %themes,
    ]);
    $win{themes}->set('All');

    tree_select($name) if $name;
}

sub filter_tree {
    my $t = $win{tree};

    my $txt = $win{themes}->get();

    return if $txt eq $last_theme;
    $last_theme = $txt;

    if ($txt eq 'All') {
	$t->item_configure("all", -visible => 1);
	return;
    }

    if ($txt eq 'Configured') {
	# Show anything without p_unconfigured
	$t->item_configure("root descendants", -visible => 0);
	for (Tkx::SplitList($t->item_id("tag policy state !p_unconfigured"))) {
	    $t->item_configure("list {$_ {$_ ancestors}}", -visible => 1);
	}
	return;
    }

    if ($txt eq 'Current Run') {
	$txt = 'currentrun';
    }
    else {
	$txt =~ s/^Theme /theme_/;
    }

    # Make everything invisible, then show the ones with the right tag.
    # Ignore errors from tags that don't exist.
    $t->item_configure("root descendants", -visible => 0);
    eval { $t->item_configure("tag $txt", -visible => 1); };
    warn $@ if $@ && $DEBUG;
}

sub add_tags {
    my $txt = shift;
    my $policy = shift;
    my @themes = @_;

    $txt->configure(-state => "normal");
    $txt->delete("1.0", "end");
    my $deleteimg = get_image('stop'); # [x] delete image
    for my $theme (@themes) {
	my $tag = "theme_$theme";
	$txt->insert("end", " $theme ", ["theme", $tag]);
	$txt->image_create("end", -image => $deleteimg, -name => $tag,
			   -align => 'center');
	$txt->tag_configure("x$tag", -elide => 1);
	$txt->tag_add($tag, $tag);
	$txt->tag_add("x$tag", $tag);
	$txt->tag_bind($tag, "<Enter>", sub {
	    $txt->tag_configure($tag, -background => "#D39999");
	    $txt->tag_configure("x$tag", -elide => 0);
		       });
	$txt->tag_bind($tag, "<Leave>", sub {
	    $txt->tag_configure($tag, -background => "#FFFFFF");
	    $txt->tag_configure("x$tag", -elide => 1);
		       });
	my $t = join(" ", grep($_ ne $theme, @themes));
	$txt->tag_bind("x$tag", "<Button-1>",
		       sub {
			   $policy->themes($t);
			   dirty();
			   populate_tree($policy->name);
			   print STDERR "SET THEMES TO $t\n" if $DEBUG;
		       });
    }
    $txt->configure(-state => "disabled");
}

sub mk_tags {
    my $policy = shift;
    my $frm = shift;
    my $row = shift;
    my $wstate = shift;
    my @defactions = ();

    my $lbl = $frm->new_ttk__label(
	-text => "Themes:",
	-anchor => "w",
	-state => $wstate,
	);

    my $default = "@{$policy->{p}{default_themes}}";
    my $value = ($policy->state eq "unconfigured") ? $default : $policy->themes;
    Tkx::tooltip__tooltip($lbl, "Default themes: $default");

    if ($wstate eq "disabled") {
	my $ent = $frm->new_ttk__entry();
	$ent->insert(0, [split(" ", $value)]);
	$ent->configure(-state => $wstate);
	Tkx::grid($lbl, $ent, -row => $row, -sticky => "ew");
	push(@defactions, [$ent, "delete", 0, "end"]);
	push(@defactions, [$ent, "insert", 0, [$default]]);
	return @defactions;
    }

    my $sfrm = $frm->new_ttk__frame();
    my $pad = Tkx::ttk__style_lookup('TEntry', '-padding');
    $pad = 4 if $IS_AQUA; # aqua glitch
    $pad += 1 if Tkx::set('::ttk::currentTheme') eq "winnative";
    my $tfrm = $sfrm->new_ttk__frame(
	-style => "TEntry",
	-padding => $pad,
	);
    my $txt = $tfrm->new_text(
	-font => 'TkTextFont',
	-highlightthickness => 0,
	-borderwidth => 0,
	-height => 1,
	-wrap => 'none',
	-cursor => "",
	);
    $txt->g_pack(-fill => "both", -expand => 1);
    $txt->tag_configure("sel", -foreground => 'black', -background => "");
    $txt->tag_configure("theme");
    add_tags($txt, $policy, split(" ", $value));
    push(@defactions,
	 [sub {
	     add_tags($txt, $policy, split(" ", $default));
	     $policy->themes($default);
	  }]);

    # Only allow a-z in tag names
    my $ent = $sfrm->new_ttk__entry(
	-validate => "key",
	-validatecommand => [ sub {
            my $t = shift;
            return ($t =~ /^[A-Z]*$/i) ? 1 : 0;
        }, Tkx::Ev("%P")],
	);
    my $btn = $sfrm->new_ttk__button(
	-style => 'Toolbutton',
	-image => get_image('add'),
	-command => sub {
	    my $t = $ent->get();
	    $ent->delete(0, "end");
	    if ($t ne "") {
		my @themes = split(" ", $policy->themes);
		my @uniq = sort keys %{{ map { $_ => 1 } (@themes, $t) }};
		return if @themes == @uniq;
		$policy->themes(join(" ", @uniq));
		dirty();
		populate_tree($policy->name);
		print STDERR "ADD THEME $t\n" if $DEBUG;
	    }
	},
	);
    $ent->g_bind("<Return>", sub { $btn->invoke(); });
    Tkx::tooltip__tooltip($btn, "Add theme to policy");

    Tkx::grid($lbl, $sfrm, -row => $row, -sticky => "ew");
    Tkx::grid($tfrm, $ent, $btn, -sticky => "ew");
    $sfrm->g_grid_columnconfigure("0", -weight => 1);
    Tkx::grid_configure($ent, -padx => [4, 0]);

    return @defactions;
}

sub update_desc {
    my $name = shift;
    my $attrs = $win{attrs};
    my $policy = $profile->policy($name);
    my $lbl;
    my $ent;
    my $default;
    my $value;
    my $row = 0;
    my @defactions = ();

    my $state = $policy->state;
    status_message("$name \[$state in profile\]", 0);

    my $wstate = ($state eq "unconfigured") ? "disabled" : "!disabled";

    Tkx::destroy("$attrs.attrs");
    my $frm = $attrs->new_ttk__frame(-name => 'attrs');

    my $f = $frm->new_ttk__frame;
    $f->new_ttk__label(
	-text => "$name \[$state\]",
	-anchor => "w",
	-font => 'ASfontBold1',
	)->g_pack(-side => "left");
    my $defbutton = $f->new_ttk__button(
	-text => "Restore Defaults",
	-command => sub {
	    for my $action (@defactions) {
		Tkx::eval(@{$action});
	    }
	},
	-state => $wstate,
	)->g_pack(-side => "right");
    $f->g_grid(-row => $row, -column => 0, -columnspan => 2, -sticky => "ew");
    $row++;

    $lbl = $frm->new_ttk__label(-text => "Severity:", -anchor => "w",
				-state => $wstate);
    $f = $frm->new_ttk__frame;
    $default = $policy->{p}{default_severity};
    my $severity = ($state eq "unconfigured") ? $default : $policy->severity;
    my $scale = $f->new_ttk__scale(
        -from => 1, -to => 5,
        -value => $severity,
        -command => sub {
	    print STDERR "$name SEVERITY $severity\n" if $DEBUG;
            $severity = int(shift);
            $policy->severity($severity);
	    dirty();
        },
	);
    $scale->g_pack(-side => "left");
    $scale->state($wstate); # scale doesn't support -state
    $f->new_ttk__label(
        -textvariable => \$severity,
	-state => $wstate,
    )->g_pack(-side => "left", -padx => 10);
    $lbl->g_grid(-row => $row, -column => 0, -sticky => "ew");
    $f->g_grid(-row => $row, -column => 1, -sticky => "ew");
    $row++;
    Tkx::tooltip__tooltip($f, "Default severity: " . $default);
    push(@defactions, [$scale, "set", $default]);

    # tag editor
    push(@defactions, mk_tags($policy, $frm, $row, $wstate));
    $row++;

    for my $param (@{$policy->{p}{parameters}}) {
        my $pname = $param->{name};
        $lbl = $frm->new_ttk__label(
	    -state => $wstate,
	    -text => $pname,
	);
        $lbl->g_grid(-row => $row, -column => 0, -sticky => "ew");
	# Some defaults appear to be undefined
        $default = $param->{default_string};
	$default = "" unless defined($default);
	$value = ($state eq "unconfigured") ? $default : $policy->param($pname);
        Tkx::tooltip__tooltip($lbl, $param->{description} . "\n"
	      . "Default: " . ($default eq "" ? "<empty>" : $default));
        $ent = $frm->new_ttk__entry(
            -validatecommand => [ sub {
		my $value = shift;
                $policy->param($pname => $value);
		dirty();
		print STDERR "$name $pname $value\n" if $DEBUG;
                return 1;
            }, Tkx::Ev("%P")],
        );
        $ent->insert(0, $value);
	# change state and enable validation after setting value
	$ent->configure(-state => $wstate, -validate => 'key');
        $ent->g_grid(-row => $row, -column => 1, -sticky => "ew");
        $row++;
	push(@defactions, [$ent, "delete", 0, "end"]);
	push(@defactions, [$ent, "insert", 0, [$default]]);
    }

    $frm->g_pack(-fill => 'both', -expand => 1, -pady => [0, 4]);
    $frm->g_grid_columnconfigure(1, -weight => 1);

    my $path = "Perl::Critic::Policy::$name.pm";
    $path =~ s!::!/!g;
    my $tmp = File::Temp->new();
    my $parser = Pod::Text->new(-width => 256);
    $parser->parse_from_file($policy->{p}{fname}, $tmp->filename) if $policy->{p}{fname};
    colorize_desc($win{desc}, $tmp);
}

sub colorize_desc {
    my $txt = shift;
    my $tmp = shift;

    # Fill in the Details text widget
    $txt->configure(-state => 'normal');
    $txt->delete("1.0", "end");

    my $line;
    my $runon = 0;
    while ($line = <$tmp>) {
	$line =~ s/\r?\n$//;
	if ($line =~ /^[A-Z][A-Z ]+$/) {
	    $runon = 0;
	    $txt->insert('end', "$line\n", 'header');
	} elsif ($line =~ /^     /) { # more than 4 spaces
	    $runon = 0;
            if (my $prev = $txt->tag_prevrange('code', 'end')) {
                # if there is nothing but whitespace since the last code
                # block, then merge it with this one
                my($beg, $end) = Tkx::SplitList($prev);
                my $s = $txt->get($end, "end");
                if ($s =~ /^\s+\n\z/) {
                    $txt->tag_add('code', $end, 'end');
                }
            }
	    $txt->insert('end', "$line\n", 'code');
	} elsif ($line =~ /^    /) { # just 4 spaces
	    $line =~ s/\s+(.*)(\s+)?$/$1/;
	    if ($runon) {
		$txt->insert('end-2c', " $line", 'abstract');
	    } else {
		$txt->insert('end', "$line\n", 'abstract');
	    }
	    $runon = 1;
	} else {
	    $runon = 0;
	    $txt->insert('end', "$line\n");
	}
    }

    # syntax color code snippets
    my @code = Tkx::SplitList($txt->tag_ranges('code'));
    while (@code) {
        my($beg, $end) = splice(@code, 0, 2);
        next unless $txt->get($beg, $end) =~ /[;{]/;  # probably not code?
        $txt->tag_remove('code', $beg, $end);
        s/\.\d+\z// for $beg, $end; $end--;  # make them line numbers
        update_syntax_tags($txt, range => [$beg, $end]);
    }

    $txt->configure(-state => 'disabled');
}

sub set_item_state {
    my $state = shift;
    my $t = $win{tree};
    my $sel = $t->selection_get();

    for my $item (@_) {
	my $name = name_from_item($item);
	my $policy = $profile->policy($name);
	if ($state eq "unconfigured") {
	    $t->item_state_set($item, '!p_enabled p_unconfigured');
	} elsif ($state eq "enabled") {
	    $t->item_state_set($item, 'p_enabled !p_unconfigured');
	} elsif ($state eq "disabled") {
	    $t->item_state_set($item, '!p_enabled !p_unconfigured');
	}
	# We are only dirty if the state actually changes
	if ($state ne $policy->state($state)) {
	    dirty();
	    update_desc($name) if ($item eq $sel);
	}
    }

    if ((@_ > 1) && $last_theme eq "Configured") {
	# Force a filter update on multi-select configuration changes
	$last_theme = "";
	filter_tree();
    }
}

sub name_from_item {
    my $item = shift;
    my $t = $win{tree};

    my $name = $t->item_text($item, 'policies');
    my $parent = $t->item_parent($item);
    while ($parent) {
	$name = $t->item_text($parent, 'policies') . "::" . $name;
	$parent = $t->item_parent($parent);
    }
    $name =~ s/ //g;

    return $name;
}

sub tree_toggle_item {
    my $x = shift;
    my $y = shift;
    my $t = $win{tree};
    my @id = Tkx::SplitList($t->identify($x, $y));

    $t->item_toggle($id[1]) if @id && $id[0] eq "item";
}

sub tree_check_item {
    my $x = shift;
    my $y = shift;
    my $t = $win{tree};
    my @id = Tkx::SplitList($t->identify($x, $y));

    return unless @id && shift(@id) eq "item";
    my $item = shift(@id);
    return unless @id && shift(@id) eq "column";
    my $col = shift(@id);
    return unless @id && shift(@id) eq "elem";
    my $elem = shift(@id);

    if ($elem eq "elemImg") {
	# Selected a checkbox element, rotate through three possible states:
        #  1) unconfigured
        #  2) enabled
        #  3) disabled

	# only operate on leaf (policy) nodes
	return if $t->item_numchildren($item);

	my $name = name_from_item($item);
	my $policy = $profile->policy($name);
	my $state = $policy->state;
	if ($state eq "unconfigured") {
	    $state = "enabled";
	} elsif ($state eq "enabled") {
	    $state = "disabled";
	} elsif ($state eq "disabled") {
	    $state = "unconfigured";
	}
	set_item_state($state, $item);
    }
    if ($elem eq "elemText") {
	# Raise on ButtonRelease instead of Selection to not conflict
	# with policy selection handling done in runtree_select
	$win{notebook}->select(0); # raise "Details"
    }
}

sub tree_popup {
    my $x = shift;
    my $y = shift;
    my $t = $win{tree};
    my @id = Tkx::SplitList($t->identify($x, $y));

    return unless @id && shift(@id) eq "item";
    my $item = shift(@id);
    return unless @id && shift(@id) eq "column";
    my $col = shift(@id);
    return unless @id && shift(@id) eq "elem";
    my $elem = shift(@id);

    # right-click action menu only over text elements
    return unless $elem eq 'elemText';

    my $m = $win{tree_popup};
    $m->delete(0, 'end');
    my $name = name_from_item($item);

    if ($t->item_numchildren($item)) {
	# Group nodes
	my @kids = Tkx::SplitList($t->item_id("$item descendants tag policy"));
	$m->add_command(
	    -label => "Unconfigure all subpolicies in profile",
	    -command => [\&set_item_state, "unconfigured", @kids],
	    );
	$m->add_command(
	    -label => "Enable all subpolicies in profile",
	    -command => [\&set_item_state, "enabled", @kids],
	    );
	$m->add_command(
	    -label => "Disable all subpolicies in profile",
	    -command => [\&set_item_state, "disabled", @kids],
	    );
    } else {
	# Leaf nodes
	my $policy = $profile->policy($name);
	my $state = $policy->state;
	$m->add_command(
	    -label => "Run this policy only",
	    -command => [\&run_perlcritic, "--single-policy", $name],
	    -state => ($globalPrefs{lastSourceFile} ? "normal" : "disabled"),
	    );
	$m->add_command(
	    -label => "Unconfigure policy in profile",
	    -command => [\&set_item_state, "unconfigured", $item],
	    ) if ($state ne "unconfigured");
	$m->add_command(
	    -label => "Enable policy in profile",
	    -command => [\&set_item_state, "enabled", $item],
	    ) if ($state ne "enabled");
	$m->add_command(
	    -label => "Disable policy in profile",
	    -command => [\&set_item_state, "disabled", $item],
	    ) if ($state ne "disabled");
    }

    $m->add_command(
	-label => "Over element \"$name\"",
	-state => 'disabled',
	) if $DEBUG;

    $m->g_tk___popup(shift, shift); # X and Y were last 2 args
}

sub tree_select {
    my $t = $win{tree};
    my $item = shift;

    $win{filemenu}->entryconfigure(
	"Run*Policy*",
	-state => "disabled",
	-command => [],
	);

    if ($item) {
	# Passed a name, make sure that item is visible if it exists
	$item = $t->item_id("tag policy_" . (split(/::/, $item))[-1]);
	return unless $item;
	$t->item_configure($item, -visible => 1);
	$t->item_expand("$item parent");
	$t->see($item);
	if (!$t->selection_includes($item)) {
	    # Return in this pass as the <Selection> event will reenter
	    # this sub again to update_desc
	    $t->selection_modify($item, $t->selection_get());
	    return;
	}
    } else {
	$item = $t->selection_get();
    }

    return if $item eq "";

    if (!$t->item_children($item)) {
	# no children - must be a leaf
	my $name = name_from_item($item);

	update_desc($name);
	$win{filemenu}->entryconfigure(
	    "Run*Policy*",
	    -state => "normal",
	    -command => [\&run_perlcritic, "--single-policy", $name],
	    ) if $globalPrefs{lastSourceFile} ne "";
    }
}

sub mk_tree {
    my $frm = shift;
    my $tsw = $frm->new_widget__scrolledwindow(
	-borderwidth => 1,
	-relief => "sunken",
    );
    my $t = $tsw->new_treectrl(
	-highlightthickness => 0,
	-borderwidth => 0,
	-showheader => 0,
	-showroot => 0,
	-showbuttons => 1,
	-showlines => 1,
	-selectmode => "browse",
    );
    $tsw->setwidget($t);

    $t->column_create(
	-text => "Policies",
	-tag => "policies",
	-expand => 1,
	-borderwidth => 1,
	-itembackground => ["#FFFFFF", "#eeeeff"],
    );

    # Add a state to note "checked" and "unconfigured" policy items
    $t->state_define('p_enabled');
    $t->state_define('p_unconfigured');
    # Policies triggered in the current run get extra text indicators
    $t->state_define('p_currentrun');

    my $chkimg = get_image("check-on");
    my $disimg = get_image("check-disabled");
    my $unchkimg = get_image("check-off");

    my $selfg = Tkx::ttk__style_lookup("Entry", "-selectforeground");
    my $selbg = Tkx::ttk__style_lookup("Entry", "-selectbackground");
    $t->element_create(
	'elemImg', 'image',
	-image => [$chkimg, 'p_enabled', $disimg, 'p_unconfigured', $unchkimg, ""],
    );
    $t->element_create(
	'elemText', 'text',
	-lines => 1,
	-fill => [$selfg, ['selected', 'focus']],
    );
    $t->element_create(
	'elemNum', 'text',
	-datatype => 'integer',
	-format => '(%d)',
	-lines => 1,
	-font => ['ASfontFixed-1'],
	-fill => 'blue',
    );
    $t->element_create(
	'selRect', 'rect',
	-fill => [$selbg, ['selected', 'focus'],
		  'gray', ['selected', '!focus']],
    );

    my $style = $t->style_create('styMixed');
    $t->style_elements($style, 'selRect elemImg elemText elemNum');
    $t->style_layout(
	$style, 'selRect',
	-union => 'elemImg elemText',
	-iexpand => 'ns',
	-ipadx => 2,
    );
    $t->style_layout(
	$style, 'elemText',
	-squeeze => "x",
	-expand => "ns",
	-padx => 2,
    );
    $t->style_layout(
	$style, 'elemImg',
	-expand => "ns",
	-padx => 1,
    );
    $t->style_layout(
	$style, 'elemNum',
	-expand => "ns",
	-sticky => "e",
	-padx => 2,
	-visible => [0, '!p_currentrun', 1, 'p_currentrun'],
    );
    $style = $t->style_create('styText');
    $t->style_elements($style, 'selRect elemText');
    $t->style_layout(
	$style, 'selRect',
	-union => 'elemText',
	-iexpand => 'ns',
	-ipadx => 2,
    );
    $t->style_layout(
	$style, 'elemText',
	-squeeze => "x",
	-expand => "ns",
	-padx => 2,
    );

    my $height = Tkx::font_metrics($t->cget('-font'), '-linespace');
    $t->configure(
	-treecolumn => "policies",
	-defaultstyle => "styText",
	-itemheight => ($height < 18 ? 18 : $height),
    );

    $t->notify_bind($t, '<Selection>', [\&tree_select]);
    $t->g_bind('<Double-Button-1>', [\&tree_toggle_item, Tkx::Ev('%x', '%y')]);
    $t->g_bind('<ButtonRelease-1>', [\&tree_check_item, Tkx::Ev('%x', '%y')]);
    $t->g_bind('<<PopupMenu>>', [\&tree_popup, Tkx::Ev('%x', '%y', '%X', '%Y')]);

    $win{tree} = $t;
    $win{tree_popup} = $t->new_menu(-tearoff => 0);
    return $tsw;
}

sub view_change {
    my $what = shift;

    if ($what eq 'view_line') {
	$win{source}->tag_configure('line', -elide => !$globalPrefs{view_line});
    } elsif ($what eq 'view_toolbar') {
	if ($globalPrefs{view_toolbar}) {
	    $win{toolbar}->g_grid();
	} else {
	    $win{toolbar}->g_grid_remove();
	}
    } elsif ($what eq 'view_statusbar') {
	if ($globalPrefs{view_statusbar}) {
	    $win{statusbar}->g_grid();
	} else {
	    $win{statusbar}->g_grid_remove();
	}
    } elsif ($what eq 'view_stats') {
	if ($globalPrefs{view_stats}) {
	    $win{runpane}->add($win{rundsw}, -weight => 1);
	} else {
	    $win{runpane}->forget($win{rundsw});
	}
    }
}

sub mk_menu {
    my $mw = shift;
    my $menu = $mw->new_menu;

    # FILE menu
    my $file = $menu->new_menu(
	-tearoff => 0,
	-postcommand => sub {
	    $win{filemenu}->entryconfigure(
		"Recent Files",
		-state => (@{$globalPrefs{mrufiles}} ? 'normal' : 'disabled'),
		);
	},
    );
    $menu->add_cascade(
	-label	 => "File",
	-underline => 0,
        -menu => $file,
    );
    $win{filemenu} = $file;

    $file->add_command(
	-label => "New Profile",
        -underline => 0,
	accel_bind($mw, "<Control-n>", \&new_profile),
    );
    $file->add_command(
	-label => "Open Profile",
        -underline => 5,
	accel_bind($mw, "<Control-o>", \&open_profile),
    );
    $file->add_command(
	-label => "Open Profile Directory",
        -underline => 13,
	accel_bind($mw, "<Control-d>", \&open_profile_dir),
    );
    $file->add_command(
	-label => "Open Default User Profile",
        -underline => 13,
	accel_bind($mw, "<Control-u>", \&open_default),
    );
    $file->add_command(
        -label => "Revert Profile",
        -command => sub {
            $profile->revert;
            dirty(0);
            sync_profile();
        },
        -state => "disabled",
    );
    $file->add_command(
	-label => "Save Profile",
        -underline => 0,
	-command => \&save,
	-state => "disabled",
    );
    $file->add_command(
	-label => "Save Profile As...",
	-underline => 13,
	-command => \&save_as,
    );
    $file->add_separator;
    $file->add_command(
	-label => "Run PerlCritic",
	-underline => 0,
	-state => "disabled",
	accel_bind($mw, "<Control-r>",
		   sub {$win{notebook}->select(1); $win{runbutton}->invoke();}),
    );
    $file->add_command(
	-label => "Run Selected Policy Only",
	-state => "disabled",
    );
    $file->add_separator;
    $win{mru} = $file->new_menu(
        -tearoff => 0,
	-postcommand => \&update_mru_menu,
    );
    $file->add_cascade(
        -label => "Recent Files",
	-underline => 7,
	-state => "disabled",
        -menu => $win{mru},
    );

    if ($IS_AQUA) {
	# Exit binding comes in standard Apple menu on OS X
	$mw->g_bind("<Command-q>", \&exit_program);
	Tkx::interp_alias("", "::tk::mac::Quit", "", [\&exit_program]);
    } else {
        $file->add_separator;
	$file->add_command(
	    -label => "Exit",
	    -underline => 1,
	    accel_bind($mw, "<Control-q>", \&exit_program),
	    );
    }

    # EDIT menu
    my $edit = $menu->new_menu(
        -tearoff => 0,
    );
    $menu->add_cascade(
	-label => "Edit",
        -underline => 0,
        -menu => $edit,
    );
    $edit->add_command(
        -label => "Cut",
	-command => [\&selection_event, "<<Cut>>"],
        accelerator("<<Cut>>"),
    );
    $edit->add_command(
        -label => "Copy",
	-command => [\&selection_event, "<<Copy>>"],
        accelerator("<<Copy>>"),
    );
    $edit->add_command(
        -label => "Paste",
	-command => [\&focus_event, "<<Paste>>"],
        accelerator("<<Paste>>"),
    );
    $edit->add_separator;
    $edit->add_command (
        -label => "Clear Profile",
        -command => sub {
            $profile->clear && dirty();
            sync_profile();
        },
    );
    if (0) {
	$edit->add_separator;
	$edit->add_command(
	    -label => "Global Options...",
	    -command => [],
	    );
    }

    if ($DEBUG) {
	$edit->add_separator;
	$edit->add_checkbutton(
	    -label => "Debug",
	    -variable => \$DEBUG,
        );
    }

    # VIEW menu
    my $view = $menu->new_menu(
        -tearoff => 0,
    );
    $menu->add_cascade(
	-label => "View",
        -underline => 0,
        -menu => $view,
    );
    $view->add_checkbutton(
        -label => "Toolbar",
	-variable => \$globalPrefs{view_toolbar},
	-command => [\&view_change, 'view_toolbar'],
    );
    $view->add_checkbutton(
        -label => "Statusbar",
	-variable => \$globalPrefs{view_statusbar},
	-command => [\&view_change, 'view_statusbar'],
    );
    $view->add_separator;
    $view->add_checkbutton(
        -label => "Line Numbers",
	-variable => \$globalPrefs{view_line},
	-command => [\&view_change, 'view_line'],
    );
    $view->add_checkbutton(
        -label => "Detailed PerlCritic Output",
	-variable => \$globalPrefs{view_stats},
	-command => [\&view_change, 'view_stats'],
    );
    $view->add_separator;
    my $cview = $menu->new_menu(-tearoff => 0);
    $view->add_cascade(
        -label => "Criticism Summary",
	-underline => 0,
	-menu => $cview,
    );
    for (qw(policy severity line col)) {
	my $lbl = ucfirst($_);
	$lbl = "Column" if $_ eq 'col';
	$cview->add_checkbutton(
	    -label => $lbl,
	    -variable => \$globalPrefs{"view_tree_$_"},
	    -command => [\&runtree_cols],
	    );
    }
    if ($DEBUG) {			    # not enabled for 8.0.1
    $view->add_separator;
    $view->add_command(
	-label => "Show Profile State ...",
	-command => sub {
	    require Text::Diff;
	    my $w = $win{state};
	    if ($w && $w->g_winfo_exists) {
		$w->g_wm_deiconify;
		$w->g_raise;
		return;
	    }
	    $w = $win{state} = $mw->new_toplevel(-borderwidth => 5);
	    $w->g_wm_title("Profile State - PerlCritic");

	    my $display = "diff";
	    my $m = $w->new_menu(-tearoff => 0);
	    for (qw(diff current file dump)) {
		next if !$DEBUG and /dump/;
		$m->add_radiobutton(
		    -label => $_, -value => $_,
		    -variable => \$display,
		);
	    }
	    my $mb = $w->new_ttk__menubutton(
		-textvariable => \$display,
		-direction => "below",
		-menu => $m,
	    );
	    $mb->g_pack;

	    my $t = $w->new_text(
		-font => "ASfontFixed",
		-state => "disabled",
		-highlightthickness => 0,
		-borderwidth => 0,
		-padx => 10, -pady => 5,
	    );
	    $t->tag_configure("add", -foreground => "blue");
	    $t->tag_configure("rem", -foreground => "red");
	    $t->tag_configure("header", -font => "ASfontFixedBold");
	    $t->tag_raise('sel');
	    $t->g_pack(-fill => "both", -expand => 1, -padx => 0, -pady => 5);

	    my $update;
	    $update = sub {
		if ($t->g_winfo_exists) {
		    $t->configure(-state => "normal");
		    $t->delete('1.0', "end");
		    if ($display eq "diff") {
			my $name = $profile->filename;
			my $orig = $name;
			if (defined $orig) {
			    $orig = file_content($orig);
			}
			else {
			    $name = $UNTITLED;
			    $orig = "";
			}
			my $diff = Text::Diff::diff(
			    \$orig,
			    \$profile->content,
			    {
				STYLE => "Unified",
				FILENAME_A => $name,
				FILENAME_B => $name . "~",
			    }
			);
			$diff ||= "No changes made to " . $name;
			my $chunk;
			for (split(/^/, $diff)) {
			    my @tag;
			    if (/^\@\@/) {
				$chunk++;
				@tag = 'header';
			    }
			    if ($chunk) {
				@tag = 'add' if /^\+/;
				@tag = 'rem' if /^-/;
			    }
			    $t->insert("end", $_, @tag);
			}
		    }
		    elsif ($display eq "dump") {
			$t->insert("end", $profile->dump);
		    }
		    elsif ($display eq "current") {
			$t->insert("end", $profile->content);
		    }
		    elsif ($display eq "file") {
			if (my $f = $profile->filename) {
			    $t->insert("end", file_content($f));
			}
			else {
			    $t->insert("end", "*** Not saved yet ***\n");			}
		    }
		    else {
			$t->insert("end", "Don't know what $display is about");
		    }
		    $t->configure(-state => "disabled");
		    # XXX Would be better to update in sub dirty
		    # XXX  - but that would not pick up changes done to the
		    # XXX    file outside of this application
		    Tkx::after(1000, $update);
		}
	    };
	    &$update;
	},
    );
    }

    add_help($menu,
        program_title => "PerlCritic",
        contents => "bin/perlcritic-gui.html",
        copyright => 'Copyright (C) 2010 ActiveState Software Inc.  All rights reserved.',
        product_url => 'http://www.ActiveState.com/ActivePerl',
        version => $VERSION,
	version_extra => "perlcritic $perlcritic_version",
    );

    $menu;
}

sub warn_unsaved_profile {
    my $msg;
    if ($profile->filename) {
	$msg = "Last profile changes are not saved for\n    \"" .
	    $profile->filename . "\"\n";
    } else {
	$msg = "New profile changes are not saved.\n"
    }
    $msg .= shift if @_;
    my $answer = Tkx::tk___messageBox(
	-title => "Unsaved Profile",
	-icon => 'warning',
	-parent => $mw,
	-type => 'yesnocancel',
	-message => $msg,
	);
    return $answer;
}

sub load_profile {
    my $f = shift;
    return if (defined($profile) && defined($f)
	       && $profile->filename && $f eq $profile->filename);
    if ($DIRTY) {
	my $answer = warn_unsaved_profile("Save changes before loading other profile?");
	if ($answer eq "cancel") {
	    # May have been from combobox - make sure it is still set right
	    $profile_name = $profile->filename || "";
	    return 0;
	}
	save() if $answer eq "yes";
    }
    # Errors can occur with unreadable profiles and mismatched perl arch
    # (e.g. finding 32-perl with 64-bit pdk)
    eval {
	if (defined($f)) {
	    if ($f eq $UNTITLED) {
		$profile = ActiveState::PerlCritic::UserProfile->new();
	    } else {
		$profile = ActiveState::PerlCritic::UserProfile->new($f);
	    }
	} else {
	    $profile = ActiveState::PerlCritic::UserProfile->new_default;
	}
    };
    if ($@) {
	Tkx::tk___messageBox(
	    -title => "Profile Load Error",
	    -icon => 'error',
	    -parent => $mw,
	    -type => 'ok',
	    -message => "Error loading profile" .($f ? " \"$f\"" : ""). ":\n$!",
	    );
	return 0;
    }
    dirty(0);
    sync_profile();
    return 1;
}

sub sync_profile {
    strictness_num($profile->param("severity") || 4);
    populate_tree();
    my $name = $profile->filename;
    if ($name) {
	# Ensure $name is at the front of @values.
	my @values = grep(!/^\Q$name\E$/, @{$globalPrefs{mrufiles}});
	unshift @values, $name;
	my $max = 10; # max num of mru items
	$#values = $max if @values >= $max;
	$globalPrefs{mrufiles} = \@values;
	$globalPrefs{lastProfileFile} = $name;
    }

    $profile_name = $name || "";
    $win{themes}->set('All'); # XXX or Configured?
    filter_tree();

    my $msg = "Loaded ";
    if ($name) {
	my $shortname = $name;
	$shortname =~ s,[\\/]?\.perlcriticrc\z,,;
	$shortname =~ s,.+[\\/],,;
	$mw->g_wm_title("$shortname - PerlCritic");
	$msg .= $name;
	if (! -f $name) {
	    dirty();
	    $msg .= " [new file]";
	}
    } else {
	$mw->g_wm_title("$UNTITLED - PerlCritic");
	$msg .= "new profile";
    }
    status_message($msg, 0);
}

sub dirty {
    my $dirty = shift;
    $DIRTY = defined($dirty) ? $dirty : 1;

    my $state = $DIRTY ? "normal" : "disabled";
    $win{save}->configure(-state => $state);
    $state = "disabled" unless defined($profile->filename);
    for ("Revert Profile", "Save Profile") {
        $win{filemenu}->entryconfigure($_, -state => $state);
    }
}

sub new_profile {
    return load_profile($UNTITLED);
}

sub open_profile {
    my @args = (
	-title => "Open Profile",
	-initialdir => $profile->dirname,
	-filetypes => [
	    ["Configuration files", ".perlcriticrc"],
	    ["All files", "*"],
	],
    );
    if (my $f = $mw->getOpenFile(@args)) {
	return 0 unless load_profile($f);
	return 1;
    }

    return 0;
}

sub open_profile_dir {
    my @args = (
        -title => "Open Profile Directory",
	-mustexist => 1,
	-initialdir => $profile->dirname,
    );
    if (my $d = $mw->chooseDirectory(@args)) {
	my $f = catfile($d, '.perlcriticrc');
	return 0 unless load_profile($f);
	open_run_dirfile(set => $d) if looks_like_source_directory($d);
	return 1;
    }

    return 0;
}

sub open_default {
    return load_profile(undef);
}

sub save_as {
    my @args = (
	-title => "Save Critic Profile",
	-defaultextension => ".perlcriticrc",
	-initialdir => $profile->dirname,
	-filetypes => [
	    ["Configuration files", ".perlcriticrc"],
	    ["All files", "*"],
	],
    );
    push(@args, (-initialfile => basename($profile->filename)))
	if $profile->filename;
    if (my $f = $mw->getSaveFile(@args)) {
	eval { $profile->save($f); };
	if ($@) {
	    Tkx::tk___messageBox(
		-title => "Profile Save Error",
		-icon => 'error',
		-parent => $mw,
		-type => 'ok',
		-message => "Error saving profile \"$f\":\n$!",
		);
	    return 0;
	}
        status_message("Wrote $f");
	dirty(0);
	# Cheap way to reset the various bits for "new" load based on
	# the updated filename
	$profile = undef;
	load_profile($f);
	return 1;
    }
    return 0;
}


sub save {
    return save_as() unless $profile->filename;
    eval { $profile->save; };
    if ($@) {
	Tkx::tk___messageBox(
	    -title => "Profile Save Error",
	    -icon => 'error',
	    -parent => $mw,
	    -type => 'ok',
	    -message => "Error saving profile \"".$profile->filename."\":\n$!",
	    );
	return 0;
    }
    status_message("Wrote " . $profile->filename);
    dirty(0);
    return 1;
}


sub exit_program {
    if ($DIRTY) {
	my $answer = warn_unsaved_profile("Save changes before exiting?");
	return if ($answer eq "cancel");
	save() if ($answer eq "yes");
    };

    my $geo = $mw->g_wm_geometry();
    my $wm_state = $globalPrefs{wm_state} = $mw->g_wm_state();
    if ($wm_state eq 'zoomed' || $wm_state eq 'icon') {
	# Don't store this setting if they leave the window maximized.
	# Get the size in normal state.
	$mw->g_wm_state('normal');
	$geo = $mw->g_wm_geometry();
	$mw->g_wm_state($wm_state);
    }
    if ($geo) {
	$globalPrefs{geometry} = $geo;
    } else {
	delete $globalPrefs{geometry};
    }

    unless (-d $APPDATA_LOCATION) {
        require File::Path;
	File::Path::mkpath($APPDATA_LOCATION);
    }
    my %h = %globalPrefs; # copy to avoid storing tied Tcl vars
    Storable::nstore(\%h, $PREFS_FILE);

    $mw->g_destroy();
}


sub update_mru_menu {
    $win{mru}->delete(0, 'end');
    my @fnames = @{$globalPrefs{mrufiles}};
    if (@fnames) {
	foreach my $fname (@fnames) {
	    $win{mru}->add_command(
                -label => $fname,
		-command => [\&load_profile, $fname],
	    );
	}
    }
}


sub accelerator {
    my $event = shift;
    my @binding = Tkx::SplitList(Tkx::event_info($event));

    # Don't use binding that have more specific bindings for the
    # text widget.  They are probably used for something else.
    @binding = grep !Tkx::bind("Text", $_), @binding;

    # Bindings for F-keys not found on the standard PC keyboard
    # are of no use to us.
    @binding = grep !/Key-F(\d+)/ || $1 <= 12, @binding;

    #print "$event: @binding\n";
    return () unless @binding;

    # clean it up
    my $acc = $binding[0];
    $acc =~ s/^<// && $acc =~ s/>$//;
    $acc =~ s/Shift-Key-/Shift+/;
    if ($IS_AQUA) {
	$acc =~ s/Mod1-Key-/Command-/;
    } else {
	$acc =~ s/Control-Key-/Ctrl+/;
    }
    $acc =~ s/Meta-Key-/Alt+/;
    $acc =~ s/^Key-//;
    $acc =~ s/([-\+][a-z])$/\U$1/;
    return -accelerator => $acc;
}

sub open_run_dirfile {
    my $type = shift;
    my $f;
    if ($type eq "set") {
	# Used by prefs
	$f = shift;
	return unless (defined($f) && -e $f);
    } elsif ($type eq "Directory") {
	my @args = (
	    -title => "Open Perl Source Directory",
	    -parent => $mw,
	    -initialdir => $profile->dirname,
	    -mustexist => 1,
	    );
	return unless ($f = $mw->chooseDirectory(@args));
    } else {
	my @args = (
	    -title => "Open Perl Source File",
	    -parent => $mw,
	    -initialdir => $profile->dirname,
	    -filetypes => [
		 ["Perl files", [".pl", ".pm"]],
		 ["All files", "*"],
	    ],
	    );
	return unless ($f = $mw->getOpenFile(@args));
    }
    # Ensure native style pathname of $f
    $f = Tkx::file_nativename(Tkx::file_normalize($f));
    $globalPrefs{lastSourceFile} = $f;
    if ($win{runbutton}->state() =~ /disabled/) {
	$win{runbutton}->configure(-state => 'normal');
	$win{tbrunbutton}->configure(-state => 'normal');
	$win{filemenu}->entryconfigure("Run*", -state => 'normal');
    }
}

sub runtree_cols {
    my @cols = ();
    for (qw(policy severity line col)) {
	push(@cols, $_) if $globalPrefs{"view_tree_$_"};
    }
    $win{runtree}->configure(-displaycolumns => \@cols);
}

sub runtree_action {
    my $action = $_[-1];
    my $x = shift;
    my $y = shift;
    my $t = $win{runtree};
    my @id = Tkx::SplitList($t->identify($x, $y));

    return unless shift(@id) =~ /cell|item/;
    my $item = shift(@id);

    if ($action eq 'double') {
	# raise "Details" if leaf node was selected
	$win{notebook}->select(0) unless $t->m_children($item);
    }
    elsif ($action eq 'popup') {
	# We are over the file column of item $item
	# Here would could pop up "Open in Editor"
    }
}

sub runtree_text_highlight {
    my $id = shift;
    my $file = shift;
    my $fname = shift;
    my $tree = $win{runtree};
    my $txt = $win{source};

    if ($last_viewed ne $fname) {
	$txt->delete('1.0', 'end');
	$last_viewed = $fname;

	my $data = file_content($fname);
	$data =~ s/\r//g;  # displayed as small squares
	$txt->insert('end', $data);

	update_syntax_tags($txt);

	# Add line numbers first so we can insert flags as "non-lines".
	# This is slightly lazy, in that line can be controlled by a tag, but
	# don't bother to do any if the user currently doesn't want them.
	my ($last) = split(/\./, $txt->index("end-1c"));
	my $len = length($last);
	#return unless ($len < 4) || $globalPrefs{view_line};
	for (1..$last-1) {
	    $txt->insert("$_.0", sprintf("% ${len}u:\t", $_), 'line');
	}

	# Adjust tabs so linenum area covers first tab stop, and the rest
	# are +8 chars after in wordprocessor style.
	my $cw = Tkx::font_measure($txt->cget('-font'), "0");
	$txt->configure(
	    -tabs => [($len+2)*$cw, ($len+10)*$cw],
	    -tabstyle => 'wordprocessor',
	    );

	# Reverse the list to allow for stable insert of items
        # use Data::Dump; dd \%output;
	for my $item (reverse @{$output{$fname}}) {
	    my ($line, $col, $mng, $pol, $sev) = @{$item};
	    my $idx = "$line." . ($len+2);
	    my $prefix = $txt->get($idx, "$idx + ".($col-1)." chars");
	    $prefix =~ s/\S/ /g;
	    $txt->insert(($line+1) . ".0", "\t", 'line',
			 "$prefix^$mng\n", "sev$sev");
	}
    }

    # Find the right line based on offset of inserted items
    my $line = $tree->set($id, 'line');
    $txt->tag_remove('curline', "1.0", "end");
    if ($line) {
	$line += $tree->index($id) + 1;
	$txt->see("$line.0");
	$txt->tag_add('curline', "$line.0", "$line.end");
    }
}

sub runtree_select {
    my $tree = $win{runtree};
    my $txt = $win{source};

    $txt->configure(-state => 'normal');

    my $id = $tree->selection();
    if ($id) {
        my $d = $last_dir;
        my $f = $tree->set($id, 'file');
	my $fname = (-d $last_dir) ? catfile($d, $f) : $d;

	my $policy = $tree->set($id, 'policy');
	if ($policy && defined($profile->policy($policy))) {
	    tree_select($policy);
	}

	runtree_text_highlight($id, $f, $fname);
    } else {
	$txt->delete('1.0', 'end');
    }

    $txt->configure(-state => 'disabled');
}

sub run_perlcritic {
    my @opts = @_;
    my $trun = $win{run};
    my $dir = $globalPrefs{lastSourceFile};

    if (! -e $dir) {
	$win{notebook}->select(1); # select "Run" tab
	Tkx::tk___messageBox(
	    -title => "Invalid Source Area",
	    -icon => 'error',
	    -parent => $trun->g_winfo_toplevel(),
	    -type => 'ok',
	    -message => "Invalid source area \"$dir\"",
	    );
	return;
    }

    if ($profile->filename && !$DIRTY) {
	push(@opts, ('--profile', $profile->filename));
    }
    else {
        my $tmp = File::Temp->new(UNLINK => 0);
        $tmp_profile_file = $tmp->filename;
        file_content($tmp->filename, $profile->content);
        push(@opts, '--profile' => $tmp_profile_file);
    }

    # Turn this into a background process with progressbar similar
    # to perlapp wrap
    # Format is %policy %filename %line %col %meaning %severity
    my $format = "==%p\\0%f\\0%l\\0%c\\0%m\\0%s\\n";
    run_cmd(tcl_list(perlcritic_cmd('--verbose', $format, '--statistics', @opts, $dir)));

    # Cache dir for processing use later
    $last_dir = $dir;
}

BEGIN {
# Declare $buf outside the function so that we pass the same
# reference to Tcl each time.  With a lexical we would create
# new references and new Tcl bindings each time.
my $buf;

sub fileevent_cmd_handler {
    my($fh, $output_cmd, $eof_cmd, $abnormal_cmd) = @_;
    my $n;
    eval { $n = Tkx::gets($fh, \$buf); };
    if ($@) {
	# call eof_cmd if abnormal_cmd hasn't been specified,
	# otherwise just call the abnormal_cmd.
	&$eof_cmd($fh) if $eof_cmd && !$abnormal_cmd;
	&$abnormal_cmd("$!", $fh) if $abnormal_cmd;
	eval { Tkx::close($fh); };
	warn $@ if $@ && $DEBUG;
	return;
    }
    if ($n == -1) {
	if (Tkx::eof($fh)) {
	    &$eof_cmd($fh) if $eof_cmd;
	    eval {Tkx::close($fh);};
	    &$abnormal_cmd("$!", $fh) if $@ && $abnormal_cmd;
	}
	return;
    }
    &$output_cmd($buf, $fh) if $output_cmd;
}

} # BEGIN


sub run_with_fileevent {
    my($run_cmd, $output_cmd, $eof_cmd, $abnormal_cmd) = @_;
    my $fh;

    $abnormal_cmd = $eof_cmd unless defined($abnormal_cmd);
    print STDERR "popen: $run_cmd\n" if $DEBUG;
    $win{run}->configure(-state => 'normal');
    $win{run}->insert('end', "$run_cmd\n\n");
    $win{run}->configure(-state => 'disabled');
    eval { $fh = Tkx::open("| $run_cmd"); } || die;
    Tkx::fconfigure($fh, -blocking => 0);
    Tkx::fileevent($fh, 'readable',
	[\&fileevent_cmd_handler, $fh,
	$output_cmd, $eof_cmd, $abnormal_cmd],
    );
    return $fh;
}


sub run_cmd {
    # Note that the $cmd should be a well formed Tcl list
    my($cmd, %opt) = @_;

    %output = ();
    $win{notebook}->select(1); # Run tab
    $win{runtree}->delete($win{runtree}->m_children(""));
    dtext_clear($win{source});
    if ($win{tree}->item_id('state p_currentrun')) {
	$win{tree}->item_element_configure(
	    'state p_currentrun', 'policies', 'elemNum', -data => 0,
	    );
	$win{tree}->item_state_set('state p_currentrun', '!p_currentrun');
	$win{tree}->item_tag_remove('all', 'currentrun');
    }

    my $txt = $win{run};
    dtext_clear($txt);

    my $fh;
    my $pid;
    my $already_killed;
    my $kill_after;
    {
	my $short_cmd = Tkx::lindex($cmd, 0);
	$short_cmd = basename($short_cmd);
	#$short_cmd =~ s/\Qperlcritic\E\z//;
	busy("Running '$short_cmd'...", sub {
	    if ($pid) {
		if ($already_killed) {
		    # try to use more force the second time.
		    #
		    # XXX There is a race here on systems that reuse pids quickly.
		    # XXX The first kill might have already killed our process and
		    # XXX another unrelated have taken its place.  Hopefully the
		    # XXX stop button is not put long enough for that to happen.
		    kill(-9, $pid);
		    $pid = undef;
		}
		else {
		    dtext_append($txt, "Attempting to stop subprocess $pid ...\n", "cmd");

		    kill('TERM', $pid);
		    $already_killed++;
		    $kill_after = Tkx::after(1000, sub {
			dtext_append($txt, "Press stop once more to kill subprocess $pid by force...\n", "cmd");
			$kill_after = undef;
	            });
		}
	    }
	    else {
		$mw->bell;
	    }
	});
    }

    my $DEV_NULL = devnull();
    $cmd .= " <$DEV_NULL 2>\@1";
    eval {
	$fh = run_with_fileevent(
            $cmd,
            \&cmd_output,
	    sub {
		Tkx::after_cancel($kill_after) if $kill_after;
		cmd_output(undef);
	    },
        );
	$pid = Tkx::pid($fh);
	print STDERR "Running pid $pid (fh $fh)\n" if $DEBUG;
    };
    if ($@) {
	dtext_append("Can't run '$cmd': $!", "error");
	busy(undef);
    }
    $txt->configure(-state => "disabled");
}

sub cmd_output {
    # Data comes in from fileevent line by line.
    # We may be called with undef - that means end of input
    my $chunk = shift;
    my $txt = $win{run};
    my $t = $win{runtree};
    $txt->configure(-state => "normal");
    if (defined $chunk) {
	# Process $chunk
	if ($chunk =~ /^==(.*)$/) {
	    # Format is %policy %filename %line %col %meaning %severity
	    my ($pol, $fname, $line, $col, $mng, $sev) = split(/\0/, $1);
	    if (0) {
		# Don't need these in the text widget
		$txt->insert("end", "File: $fname\n") unless $last_file eq $fname;
		$txt->insert("end", "    \@$line,$col: $mng\n"
			     . "    [$pol] (Severity $sev)\n"
		    );
	    }
	    # This can happen when we stop output mid-run
	    return unless defined($sev);
	    # Take off the first part of the path for viewing
	    my $dname = $fname;
	    if (-d $last_dir) {
		$dname =~ s!^\Q$last_dir\E[\\/]?!!;
	    } else {
		$dname = basename($fname);
	    }
	    if (!defined($output{$dname})) {
		my $id = $t->insert(
		    "", "end",
		    -text => $dname,
		    -values => [$dname],
		    );
		$output{$dname} = $id;
		if ($t->index($id) == 0) {
		    # Focus on the runtree when first item comes in
		    Tkx::focus(-force => $t);
		    $t->focus($id);
		}
	    }
	    $t->insert(
		$output{$dname}, "end",
		-text => $mng,
		-values => [$dname, $line, $col, $mng, (split(/::/, $pol))[-1], $sev],
		-tags => "sev$sev",
		);
	    push(@{$output{$fname}}, [$line, $col, $mng, $pol, $sev]);
	    if (defined($profile->policy($pol))) {
		# Tag this policy with currentrun
		my @names = split(/::/, $pol);
		my $id = $win{tree}->item_id("tag policy_" . $names[-1]);
		if ($id) {
		    # Make sure that item is visible
		    $win{tree}->item_configure($id, -visible => 1);
		    $win{tree}->item_tag_add("list {$id {$id ancestors}}",
					     'currentrun');
		    $win{tree}->item_state_set($id, 'p_currentrun');
		    my $cur = $win{tree}->item_element_cget(
			$id, 'policies', 'elemNum', '-data',
			);
		    $win{tree}->item_element_configure(
			$id, 'policies', 'elemNum', -data => ++$cur,
			);
		}
	    }
	    $last_file = $fname;
	} elsif ($chunk =~ /^(.*) source OK$/) {
	    my $fname = $1;
	    $txt->insert("end", "File \"$fname\" OK");
	    $txt->insert("end", "\n");
	    $last_file = "";
	} else {
	    $txt->insert("end", $chunk);
	    $txt->insert("end", "\n");
	    $last_file = "";
	}
    }
    else {
	$txt->insert("end", "\n[DONE]");
	busy(undef);
	$last_file = "";
	$last_viewed = "";
        my @tkids = Tkx::SplitList($t->m_children("")); 
	if (!@tkids) {
	    # No children - no errors.  Make sure to reset filter
	    status_message("No Criticisms", 2000);
	    if ($win{themes}->get() eq "Current Run") {
		$last_theme = "";
		filter_tree();
	    }
	} else {
	    my @phits = Tkx::SplitList($win{tree}->item_id("tag {currentrun && policy}"));
	    if (@phits > 1 || $win{themes}->get() eq "Current Run") {
		# Only change filter if more than one policy hit
		# or "Current Run" is the current filter
		$win{themes}->set("Current Run");
		$last_theme = "";
		filter_tree();
	    } else {
		$win{tree}->selection_modify($phits[0],
					     $win{tree}->selection_get());
	    }
            if (@tkids == 1) {
                $t->selection_set($tkids[0]);
                runtree_select();
                $t->item($tkids[0], -open => 1);
            }
	}
        if ($tmp_profile_file) {
            unlink($tmp_profile_file);
            undef($tmp_profile_file);
        }
    }
    $txt->configure(-state => "disabled");
    $txt->see("end");
}

BEGIN {
    my $busy_cb;

    sub busy {
	my $msg = shift;
	if ($msg) {
	    $busy_cb = shift;
	    # Make any "Run" controls based on busy_cb existence
	    if ($busy_cb) {
		# Passed a callback to stop being busy
		$win{runbutton}->state('alternate'); # move to "running"
		$win{runbutton}->configure(-command => $busy_cb);
		$win{tbrunbutton}->state('alternate'); # move to "running"
		$win{tbrunbutton}->configure(-command => $busy_cb);
	    } else {
		$win{runbutton}->state('disabled'); # move to "disabled"
		$win{tbrunbutton}->state('disabled'); # move to "disabled"
	    }
	    $win{filemenu}->entryconfigure("Run*", -state => 'disabled');

	    $stat->add($win{progress}, -pad => 0, -separator => 1, -sticky => "ew");
	    $win{progress}->start;
	}
	else {
            if ($busy_cb) {
		# Disable any "Stop" button
		$busy_cb = undef;
	    }
	    # Reenable "Run" controls
	    $win{runbutton}->state('!alternate !disabled');
	    $win{runbutton}->configure(-command => [\&run_perlcritic]);
	    $win{tbrunbutton}->state('!alternate !disabled');
	    $win{tbrunbutton}->configure(-command => [\&run_perlcritic]);
	    $win{filemenu}->entryconfigure("Run*", -state => 'normal');
	    $win{progress}->stop;
	    eval { $stat->remove($win{progress}); }; # ignore warnings
	    warn $@ if $@ && $DEBUG;
	}
	$msg = "" unless defined $msg;
	status_message($msg, 0);
    }
}

sub status_message {
    my $text = shift;
    my $delay = shift;

    $delay = $status_delay unless defined $delay;
    Tkx::after_cancel($status_afterid);
    $STATUS = $text;
    $status_afterid = Tkx::after($delay, sub { $STATUS = ""; }) if $delay;
}

sub style_init {
    Tkx::package_require("style");
    Tkx::style__use("as", -priority => 70);
}

sub dtext_append {
    my $t = shift;
    $t->configure(-state => "normal");
    $t->insert("end", @_);
    $t->configure(-state => "disabled");
}

sub dtext_clear {
    my $t = shift;
    $t->configure(-state => "normal");
    $t->delete("1.0", "end");
    $t->configure(-state => "disabled");
}

sub tcl_list {  ## no critic
    return scalar(Tkx::list(@_));
}

sub looks_like_source_directory {
    my $d = shift;
    return 0 unless -d $d;
    return 1 if -f "$d/Makefile.PL" || -f "$d/Build.PL";
    return 1 if -d "lib" && -d "t";
    return 0;
}

sub usage {
    print <<EOT; exit;
Usage: $progname [options...] [profile] [source]

The supported options are:

    --version         Print version number and exit
    --run             Run perlcritic on sources initially
    --perl <path>     Override what perl to use
    

EOT
}

sub version {
    print "$progname $VERSION (perlcritic $perlcritic_version)\n";
}

__END__

=head1 PerlCritic - Graphical Interface

=head1 Overview of the Interface

B<PerlCritic> is a graphical interface to the
L<Perl::Critic CPAN module|http://search.cpan.org/perldoc?Perl::Critic>,
an "extensible framework for creating and applying coding standards to
Perl source code". The interface lets you choose and configure the
Perl::Critic::Policy modules, and run your sources through those
policies and view any violations.

=begin html

<img src="../images/PerlCritic_run.png">

=end html

=begin text

Portions of this page (screenshots and toolbar descriptions) are not
available via 'perldoc'.

Please see the HTML documentation in the ActivePerl User Guide.

=end text

=begin man

Portions of this page (screenshots and toolbar descriptions) are not
available via 'perldoc'.

Please see the HTML documentation in the ActivePerl User Guide.

=end man

=head1 Profiles and Policies

B<PerlCritic> comes with the standard Perl::Critic::Policy modules from
CPAN, which are based mainly on the guidelines from Damian Conway's I<Perl
Best Practices> book.

=head2 Choosing a Profile

If PerlCritic is started without specifying a profile, it will attempt
to load the default profile - a 'I<.perlcriticrc>' file in your HOME
directory.

To create a new profile, select B<File|New Profile> (Ctrl+N).

To load a named profile (e.g. 'I<MyProject.perlcriticrc>') click the
B<Open profile> button or select B<File|Open Profile> (Ctrl+O).

To load a hidden profile (e.g. 'I<.perlcriticrc>') click the B<Open
profile directory> button or select B<File|Open Profile Directory>
(Ctrl+D>.

The full path to the current profile is displayed in the toolbar.

=head2 Enabling and Disabling Policies

The left pane contains the Policy Tree. Use this to browse and select
the policies you want to enable in the currently loaded profile. Items
in the tree have three possible states:

=over

=item *

B<Unconfigured in profile> (checked, gray): The policy is enabled, but
not explicitly saved in the current profile.

=item *

B<Enabled in profile> (checked, black): The policy is enabled explicitly
in the current profile.

=item *

B<Disabled in profile> (unchecked, black): The policy is explicitly
disabled in the current profile.

=back

The main policy groups have a right-click context menu with options to
Unconfigure, Enable, or Disable all policies in the group.

=head2 Policy Tree Filter

Immediately above the Policy Tree is a drop list with filter parameters
which allows you to limit the sometimes lengthy list of policies to
those of a particular conceptual group. There are two additional filter
parameters that have special functions:

=over

=item *

B<Configured>: Shows policies in the tree that are configured (either
enabled or disabled) in the current profile.

=item *

B<Current Run>: Shows policies which were flagged in the most recent
policy test run.

=back

=head2 Policy Details

Double clicking on any policy item opens the configuration details and
documentation for that particular policy in the B<Details> tab.

Each module will have unique configuration options which are described
in its documentation. When the policy is in an unconfigured state
(gray), the policy options cannot be modified. When the policy is
configured either as enabled or disabled in the profile, the options
available for that policy can be modified.

The B<Severity> slider controls which severity level level the policy is
triggered at during a run (see L<Running PerlCritic on Sources>).

=head2 Severity

Each policy has a Severity setting which can be set to a value from 1
(least important) to 5 (most important) with a slider. This setting
controls which level of L<Strictness> the policy will be included in
when the policy tests are run.

=head2 Creating Custom Policies

Documentation on creating policies for PerlCritic can be found here:

L<http://search.cpan.org/dist/Perl-Critic/lib/Perl/Critic/DEVELOPER.pod>

=head1 Running PerlCritic on Sources

PerlCritic can be run on a single Perl file or all Perl sources in a
specified directory.

=over

=item *

Use B<Select single file...> to chose a single file to run policy tests
on.

=item *

Use B<Select sources directory...> to specify a directory to run policy
tests on. PerlCritic will run the policy tests on all Perl files in the
directory, traversing all subdirectories recursively. B<Note>:
Specifying directories with many files and/or subdirectories will
increase the amount of time a policy test run will take.

=back

=head2 Strictness

Each policy has a L<Severity> level. Policy tests can be run with the
following levels of Strictness:

=over

=item *

Brutal: Reports violations of any enabled policy.

=item *

Cruel: Reports violations of enabled policies with a severity setting of
2 through 5.

=item *

Harsh: Severity 3 through 5.

=item *

Stern: Severity 4 and 5.

=item *

Gentle: Severity 5 violations only.

=back

=head2 Running a single Policy

To run a single policy only, right click on the policy in the Policy
Tree and select B<Run this policy only> or use B<File|Run Selected
Policy Only>.

=head1 Reviewing Criticisms

The B<Run> tab has two main panes for displaying criticisms. When Perl
Critic is run on the specified sources, the top pane is populated with a
list of the files which have been flagged with criticisms. Each item in
the list can be expanded to show:

=over

=item *

The criticism summary (i.e. the reason it is a policy violation).

=item *

Policy name.

=item *

The severity level of the relevant policy.

=item *

The line number of the violation.

=item *

The column number of the violation.

=back

Criticism, severity, and line number are shown by default.

Clicking on a criticism opens the relevant code in the bottom pane. All
criticisms are inserted as highlighted text below the line containing
the violation, with a caret "^" indicating the column where the
violation occurred. Highlighting indicates the severity setting of the
policy, from light yellow (severity 1) to red (severity 5).

Double clicking on a criticism in the top pane opens the B<Details> tab
showing the policy details for that criticism.

=head2 View options

The B<View> menu contains options for displaying line numbers in the
bottom pane (useful for locating the violation), and for opening an
additional pane for displaying detailed output from last policy test run
(useful for debugging problems with policy tests).

The B<View|Criticism Summary> sub-menu allows you to show or hide
columns in the criticism summary pane.

=head1 COPYRIGHT

Copyright (C) 2010 ActiveState Software Inc.  All rights reserved.

=cut

__END__
:endofperl
