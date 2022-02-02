#*****************************************************************************
# -*- perl-mode  -*-
# $Id: Common.pm,v 1.1 2006/05/06 19:57:14 sturolla Exp $
#   NAME
#   Common.pm
#****************************************************************************
package Common;

use POSIX ":sys_wait_h";                # Used by waitpid
use Time::Local;                        # Used by time()
#
# host, user, command
#
sub remoteCall { 
    local($host) = $_[0];
    local($user) = $_[1];
    local($command) = $_[2];
    if ($main::REMOTE eq "rsh") { 
	if ( `uname` =~ /^SunOS/ || `uname` =~ /^Linux/ ) {  #
	    return "rsh $host -l $user $command";
	} 
	if ( `uname` =~ /^HP-UX/) {  #
	    return "remsh $host -l $user $command";
	} 
    } else { 
	return "ssh -x $host -l $user $command";
    }
}

#
# host, user, arguments
#
sub remoteCopy { 
    local($origin) = $_[0];
    local($destination) = $_[1];
    if ( `uname` =~ /^SunOS/ || $main::REMOTE eq "rsh") { #
	return "rcp -r $origin $destination";
    } else { 
	return "scp -rpq $origin $destination";
    }
}


###################################################
# Filename enhancement functions
###################################################
#
# if the file starts with slash, only the basename is taken
#
sub fileToBase { 
    local($file) = $_[0];
    if ( $file =~ /^\//) { 
	$file =~ s/.*\///;
    }
    return "$main::base_dest/$file";
}

sub fileToDest { 
    local($file) = $_[0];
    return "$Common::username\@$webhost:".fileToBase($file);
}
#
sub publishReport { 
    local($file) = $_[0];
    if ($file eq "" || $file eq "/") { 
	warn "Common::publishReport given null argument - ignoring\n";
	return;
    }
    system remoteCall($webhost,$username," rm -fr ".fileToBase($file));
    system("chmod -R a+r $file ");
    system(remoteCopy($file, fileToDest($file)));
}

sub executeOrDie { 
 local($command) = @_;
 #print $command."\n" if $main::debug;
 system "$command ";
 if ( $? !=0) { 
     close($main::logf);
     die "failed to execute $command\n" ;
 }
}

###################################################
###################################################

#
# this procedure takes as an input a directory and counts the lines
# of code in it, using sloccount
# The total number of SLOC is reported.
#
sub countlines { 
  local($dir) = $_[0];
  local($slocOutput, $sloc) ;
  
  $slocOutput = `sloccount $dir 2>/dev/null`;
  if (   $slocOutput =~ /\(SLOC\)\s*=\s*([\d,]*)/  ) {
      $sloc = $1;
      $sloc =~ s/,//;
      return $sloc;
  } else { 
      warn "Could not determine SLOC from sloccount output for $dir\n" if $debug;
      return 0;
  }
}
# given an argument which can be either the name
# of a pkginBuild module, or the name of a directory,
# this routine determines which is appropriate
sub isPkgin { 
  local($integ) = $_[0];
  if ( -f "$integ/config/$integ"."INSTALL.cfg") { 
      return 1;
  }
  return 0;
}
#
# this procedure extracts keys from a Makefile
# It should probably be modified to use a call 
# to 'make' with the options displaying the variables database
# TBD

sub getFromMakefile {
    local($tOken) = $_[0];
    local($file) = $_[1];
    local($tmpRes) = "";
    local($finRes);
    local(@tmpResA, @finRes);
    warn ">>> getFromMakefile: could not find file $file\n" unless -f $file;
    warn ">>> getFromMakefile: could not read file $file\n" unless -r $file;
    open(tmpFD," cat $file |");
    @lines = <tmpFD>;
    close(tmpFD);
    for($i = 0; $i <= $#lines; $i++) { 
	next if $lines[$i] =~ /^#/; #
	if ($lines[$i] =~ /^$tOken\s*=/ ) {
	    if ( $lines[$i] =~ /\\/ ) { 
		$tmpRes = $lines[$i];
		while( $lines[$i] =~ /\\/ ) { 
		    $tmpRes .= $lines[++$i];
		} 
	    } else { 
		$tmpRes = $lines[$i];
	    }
	    #print ">>$tmpRes<<\n";
	    $tmpRes =~ s/\n//g;
	    $tmpRes =~ s/\\//g;
	    $tmpRes =~ s/^$tOken\s*=//;
	    $tmpRes =~ s/^\s+//g;
	    #print ">>$tmpRes<<\n";
	    @tmpResA = split(/\s+/,$tmpRes) ;
	    if ( $tmpRes ne "") { 
		foreach $content (@tmpResA) { 
		    local($postFix);
		    if($content =~ /\$\(([^\)]*)\)/ || 
		       $content =~ /\$\{([^\}]*)\}/   )  { 
			local(@tmpArray);
			
			$token2 = $1;
			$content =~ /$token2[\)|}](.*)/; #$
		    $postFix = $1;
		    #print "RECURSE ($token2) - $postFix !\n";
		    @tmpArray = getFromMakefile($token2,$file);
		    #foreach (@tmpArray) { 
		#	$_ .= $postFix;
		#    }
		    push( @finRes, @tmpArray);
		} else { 
		    push(@finRes, $content);
		}

		}
		return @finRes ;
	    }
	    return;
	}
    }
    return ();
}


# for a directory based structure, the idea
# is that a "module" is something which has a Makefile
# in it.
# The success of this routine also depends upon the
# directory structure whereby either 'ws' or 'lcu' is given.
#
sub determineModulesDirBased { 
    local($topdir) = $_[0];
    local($command) = "find $topdir -depth -name Makefile";
    local(@Makefiles, $file) ;
    local(@modules, @excludees);
    @Makefiles = `$command`;
    foreach $file (@Makefiles) { 
	$file =~ s/\n//;
	local($excluded, $already);
	if ( 
	     ( ($file =~ /(\w*)\/(ws|lcu)\/src\/Makefile$/ || 
		$file =~ /(\w*)\/src\/Makefile$/        ||
		# the following is the interface for DFS pipelines
		( $file =~ /(\w*)\/Makefile$/  && ( -f "$file.am") && ( -f "$1/bootstrap" ) ) ) )
	      ) { 
	    $name = $1;
	    # this is to avoid third party packages
	    next if ($file =~ /\/(test|src).*$name/ );
	    $file =~ /(^.*$name)/;
	    $tmpString = $1;
	    $already = 0;
	    $excluded = 0;
	    foreach $mod (@modules,@excludees) { 
                if ($tmpString eq $mod  ) { 
		    $already = 1;
		    break;
		}
	    }
	    foreach $paria (@Common::EXCLUDEES) { 
		if ($paria eq $tmpString) { 
		    $excluded = 1;
		    break;
		}
	    }
	    if ( ! $already) { 
		if ( ! $excluded) { 
		    push(@modules,$tmpString);
		} else { 
		    push(@excludees,$tmpString);
		}
	    }
	} 
    }
    if ($#modules == -1) { 
	$modules[0] = $topdir;
    }
    return @modules;
}


sub determineModulesFull { 
  local($integ,$dynamic) = @_;
  if($dynamic eq "") { 
      # dynamic defaults to zero
      $dynamic = 0;
  }
  if(isPkgin($integ)) { 
      local($names, $versions, $directories);
      local(@names, @directories);
      local(@modules);

      local($excluded, $tmpString);

      ($names, $versions, $directories) 
		= extractAllInfoStraight("$integ/config/$integ"."INSTALL*.cfg");
      @names = @$names;
      @directories = @$directories;
	
      for($i = 0 ; $i <= $#names; $i++) { 
      
      # Check for excludeed list      
         $excluded = 0;
	 if ( $directories[$i] ne "") { 
	    $tmpString = "$directories[$i]/$names[$i]";   
	 } else { 
	    $tmpString = $names[$i];   
	 }
	 
	 foreach $paria (@Common::EXCLUDEES) { 
	    if ($paria eq $tmpString) { 
	       $excluded = 1;
	       break;
	    }
	 }
         if ( ! $excluded) { 

	    if ( $directories[$i] ne "") { 
	        if ($dynamic && $directories[$i] eq "DOCS") { 
		  
	        } else { 
		    push(@modules, "$directories[$i]/$names[$i]");
	        }
	    } else { 
	        push(@modules, "$names[$i]");
	    }

	 }

      }	
      return @modules;
  } else { 
      return determineModulesDirBased($integ);
  }
}


# 
# this routine returns three arrays:
# directory, module and version
# for a given file 
sub extractAllInfoStraight { 
    local($integ) = $_[0];
    local(@tmpArray);
    local(@names, @versions, @directories);
    local($name, $number, $directory, $version);
    local($processed) = 0;
    foreach $confFile (glob($integ)) { 
	if ( ! -f $confFile ) { 
	    die " could not determine configuration file from integration module $confFile \n";
	}
	open(tmpFD, "$confFile");
	@lines =<tmpFD>;
	close(tmpFD);
	foreach (@lines) { 
	    next if /^#/; #'
	    if ( /^INSTALL\.FILES/ ) {
		s/^INSTALL\.FILES.*$// ;  
		$processed = 1;
	    }
	    
	    if ( /^INSTALL\.LOGINNAME/ ) {
		s/^INSTALL\.LOGINNAME.*$// ;  
		$processed = 1;
	    }
	    
	    if (/MODULE.*NAME/) {
		$name = $number = $version = $directory = "";
		if ( ! /INSTALL\.MODULE(\d+)\.NAME\s+\"([\w|\-|\d]+)\"/ ) { #"
		    next;
		}
		$number = $1;
		$name = $2;	    
		
		@tmpArray = grep(/INSTALL\.MODULE$number\.VERSION/, @lines);
		if ( $tmpArray[0] =~ /\.MODULE$number\.VERSION\s+\"([^\"]*)\"/ ) {
		    #" this for emacs
		    $version = $1;
		}
		
		@tmpArray = grep(/INSTALL\.MODULE$number\.SUBPKG/, @lines);
		if ( $tmpArray[0] =~ /\.MODULE$number\.SUBPKG\s+\"([^\"]*)\"/ ) {   
		    #" this for emacs
		    $directory = $1;
		    if ($directory eq "") { 
			$directory = "MISSING-PKG";
			$addenda .= "INSTALL.MODULE$number.SUBPKG\t\"MISSING-PKG\";\n"; 
		    }
		}
		push(@names, $name);
		push(@versions, $version);
		push(@directories, $directory);
	    }
	}
	
	if ( $addenda ne "" || $processed ) {
	    print "Adding missing directories to $confFile\n" 
		if $main::debug && ! $processed ;
	    
	    print ">>>> Fixing the config file \n" 
		if $processed && $main::debug ;
	    if ( -o $confFile) { 
		system "chmod u+w $confFile";
	    }
	    
	    open(tmpFD, "> $confFile") || die ">>> Failed to open $confFile for writing";
	    print tmpFD @lines;
	    print  tmpFD $addenda ;
	    close(tmpFD);
	    system "chmod u-w $confFile";
	}
    } # loop over files returned by glob
    return (\@names, \@versions, \@directories);
}


#      foreach $tmpConf ( split(/\n/, `find $integ  -type d -maxdepth 1`)) { 
	  

#      foreach $tmpConf ( split(/\n/, `find $integ  -type d -maxdepth 1`)) { 
	  

# 
# this routine determines which subdirectories are specified in 
# a configuration file for an integration module
sub determineDirs { 
  local($integ) = @_;
  local(%Dirs, $tmpConf,$names, $directories, $versions );
  undef %Dirs;
  $tmpConf = $integ."INSTALL*.cfg";
  if ( -f "$integ/config/$integ"."INSTALL.cfg" ) { 
      ($names, $versions, $directories) = extractAllInfoStraight("$integ/config/$tmpConf");
  } else { 
      warn "there's no config dir, I will go ahead directory-based\n" if $debug;
      local(@tmpArray);
      push(@tmpArray, $integ);
      return @tmpArray;
#      die "Could not determine configuration file from integration module $integ \n";
  }
  foreach (@$directories) {
      $Dirs{$_} = 0;
  }
  return keys %Dirs;
}

sub Timestamp { 
  local($date);
  if (`uname` =~ /Linux/) { 
    chop($date = `date -u`);
    return $date;
  } else { 
    chop($date = `date `);
    return $date;
  }
}

$ENV{CMM_HOST}="te13.hq.eso.org";
$ENV{CMM_PORT}="3000";
#
# CMM SPECIFIC INFO
#
$archive_prefix = "/diskc/archives/cmm/";
$archive_host = "te13";
$mod_info = "/home/vltsccm/vlt/SCCM/tables/modNameList";
#
# HTML HEADERS 
#
$virtualHeader = "<!--#include virtual=\"../frame/top.html\" -->\n";
$virtualFooter = "<!--#include virtual=\"../frame/bottom.html\" -->\n";
#
# WEB PAGE SPECIFIC INFORMATION
# SQA-MACHINE SPECIFICATION
# Notice: the assumption is that the same account is used both
# for CVS Server, webhost and sqahost
#
$username = "sqa-ops";
$webhost = "websqa.hq.eso.org";
$webpath = "/home/web/sqa/docs/";
$webcp = "/usr/server/bin/webcp ";
$sqahost = "sqa0";

$ALMASnapshotRep = "$webpath/alma/snapshot/";
$SQAMSnapshotRep = "$webpath/sqam/snapshot/";
$VLTSnapshotRep = "/home/web/eso/docs/projects/vlt/sw-dev/admin/spr-survey/";
$Table{"VLT"}{"table"}        = "VLT_SPR";
$Table{"VLT"}{"username"}     = "vltmgr";
$Table{"VLT"}{"destination"} = $VLTSnapshotRep;
$Table{"ALMA"}{"table"}       = "ALMA_SPR";
$Table{"ALMA"}{"username"}    = "mzampare";
$Table{"ALMA"}{"destination"} = $ALMASnapshotRep;
$sqaBase = "/diska/sqa0";


# warning: changes to these keys should be reflected in the
# archiving process running on websqa
%{$Summ{modules}}   = ( label => "Modules",res => 0,ord=>1);
%{$Summ{buildfailed}} = ( label => "Build FAILED",res => 0,color=>red,ord=>2);
%{$Summ{buildexcluded}} = ( label => "Build excluded",res => 0,color=>gray,ord=>3);
%{$Summ{testfailed}}  = ( label => "Test FAILED",res => 0,color=>red,ord=>4);
%{$Summ{testpassed}}  = ( label => "Test PASSED",res => 0,color=>green,ord=>5);
%{$Summ{testundetermined}} = ( label => "Test UNDETERMINED", color=>red, res => 0,ord=>6);
%{$Summ{testexcluded}} = ( label => "Test excluded", color=>gray, res =>0,ord=>7);
%{$Summ{nomakefile}}  = ( label => "No Makefile ",res => 0,ord=>8);
%{$Summ{notestdir}}   = ( label => "Missing Test Directory",res => 0,ord=>9);
%{$Summ{testtimeout}} = ( label => "Test TIMED OUT",res => 0,color=>red,ord=>10);
%{$Summ{coredumped}}  = ( label => "Test CORE DUMPED",res => 0,color => red,ord=>11);
%{$Summ{purifyfailed}}  = ( label => "Instrumentation Failed", res => 0,ord=>12);



############################################################
# this part for Codewizard 
############################################################
############################################################
sub convertFileName { 
    local($myFile)  = @_;
    $myFile =~ s/^.*\///;
    $myFile = "$myFile-rep.html";
    return $myFile;
}

#
# temporary routine for the handling of Purify
#
sub PurifyPrepare { 
    local($type) = $_[0];
    local($RationalDir) = "/diska/alma/Rational";
    local($PurifyRelease) = "2002.05.21";
    local($Purify) = "$RationalDir/releases/PurifyPlusForLinux.$PurifyRelease/targets/clinuxgnu/lib"; 

    local($CXX)         = "attolcc DYNTYPE -- gcc -I$Purify ";
    local($LD)          = "attolcc DYNTYPE -- gcc " ;
    local($JAVIC)       = "javic DYNTYPE -- javac ";
    local($makeCommand) = " CXX=\"$CXX\" LD=\"$LD\" ";
    local(%dynType) = ("memory" => " -mempro ", "coverage" => " -trace -pass -noternary ", "profiling" => " -perfpro ");
    
    $makeCommand =~ s/DYNTYPE/$dynType{$type}/g;
    return( $makeCommand);
}

# this procedure should take as input a file name
# filter if for FAILED or FAILURE and 'color' the corresponding
# HTML
#
sub bunteBilder { 
    local($file) = $_[0];
    local(@tmpArray);
    if ( ! -f $file ) { 
	warn "Common::bunteBilder: file $file does not exist\n";
	return "";
    }
    open(fd, $file);
    @tmpArray = <fd>;
    close(fd);
    
    foreach (@tmpArray) { 
	s/FAILED/<font color=\"red\">FAILED<\/font>/g;  
	s/FAILURE/<font color=\"red\">FAILURE<\/font>/g;  #"emacs
    }
    return join('', @tmpArray);
}

#########################################################################
###### DATABASE AREA ####################################################
#########################################################################
sub errorArisen { 
    warn "NRI:SQL error\n";
}

sub attPro { 
    local($sql) = $_[0];
    local($dbh) = $_[1];
    local($sth);
    if ( ! defined $dbh) { 
	warn ">>> attPro: \$dbh is undefined!\n";
	return "";
    }
    $sth = $dbh->prepare($sql);
    eval {
	print "SQL: $sql\n" if $debug;
	$sth->execute;
    };
    if ( $@ ) {
	# Error[s] raised
	$error="$@";
	errorArisen($error);
	$dbh->disconnect();
    }
    return $sth;
}



sub openMetricsDatabase { 
    local($dbh);
    local($string) = "dbi:Sybase:server=ESOECF";

    DBI->trace(0);
    $dbh = $string;
    # Set RaiseError=0 to catch the error later on
    $dbh = DBI->connect($string, "sqa", "sqa_user",{  PrintError =>0, RaiseError =>0, AutoCommit =>0});
    return $dbh;
}

sub openRemedyDatabase { 
    local($dbh);
    local($string) = "dbi:Sybase:server=ARSERV12";
    DBI->trace(0);
    $dbh = $string; 
    # Set RaiseError=0 to catch the error later on
    $dbh = DBI->connect($dbh, "sqispr", "sqispr_ps",{  PrintError =>0, RaiseError =>0, AutoCommit =>0});
    return $dbh;
}


# This procedure stored a metric point in the database
# recordMetrics("alma","corr","sloc_python",234);
#
sub recordMetrics { 
    local($project, $subsystem, $metric, $value, $date) = @_;
    local($projId, $subId, $metId) = ("","","");
    local($dbh, $sth);
    local($dataTable) = "data";

    if ($main::dataTable ne "") { 
	$dataTable = $main::dataTable;
    }

    if ( ! defined $date) { 
	chop($date = `date -I`);
    } 

    $dbh = openMetricsDatabase();
    if ($dbh) {
       # DB Connect OK
       $sth = attPro("select proj_id from project where proj_name = \"".$project."\"", $dbh);
       while(@row = $sth->fetchrow_array) { 
	   $projId = $row[0];
       }
       $sth->finish;
       if ( $projId eq "") { 
	   warn "NRI: DBI: no such project $project exists\n";
	   $dbh->disconnect();
	   return;
       }

       if ( $subsystem eq "ZERO") { 
	   $subId = 0;
       } else { 
	   $sth = attPro("select sub_id from subsystem where sub_name = \"".$subsystem."\" and proj_id = $projId", $dbh);
	   while(@row = $sth->fetchrow_array) { 
	       $subId = $row[0];
	   }
	   $sth->finish;
	   if ( $subId eq "") { 
	       warn "NRI: DBI: no such subsystem $subsystem exists\n";
	       $dbh->disconnect();
	       return;
	   }
       }

       $sth = attPro("select met_id from metric where met_name = \"".$metric." \"", $dbh);
       while(@row = $sth->fetchrow_array) { 
	   $metId = $row[0];
       }
       $sth->finish;
       if ( $metId eq "") { 
	   warn "NRI: DBI: no such metric $metric exists\n";
	   $dbh->disconnect();
	   return;
       }


       $statement = "insert into $dataTable values ( $projId,$subId,$metId, \"$date\", $value)";   
       #print "$statement\n" if $debug;
       $sth = attPro($statement, $dbh);
       $dbh->commit();
       $dbh->disconnect();

    } else {
       # DB Connect failed
       print ">>> recordMetrics:  Connection failed: $DBI::errstr" if $debug
    }
}

#
# this routine returns the PIDs of all 
# descendants of a certain PID, including itself.
#
sub descendants { 
    local($pid) = $_[0];
    # a space separated list of PIDs
    local($descendants) = "";
    if ( `uname` =~ /^Linux/) { 
	local($cmnd) = "pstree -pl $pid";
	$rawList = `$cmnd`;
	if ($?) { 
	    warn ">>> descendants: pstree returned non zero code\n";
	}
	$pattern = "\(\d+\)";
	foreach $pod ($rawList =~ /\(\d+\)/g) { 
	    $pod =~ s/^\(|\)$//g;
	    #print $pod." ";
	    $descendants .=  "$pod ";
	}
    } else { 
	print ">> No Claro2::descendants support for this OS\n";
	$descendants = "$pid ";
    }
    return $descendants;
}

 
# This subroutine spawns a child and checks
# fot its natural end. In case of a child
# requiring to much time to end (resources) the sub forces
# the child to terminate.
#
# SYNOPSIS
# spwanWithTimer(TimeLimit, Command)
#
# TimeLimit: maximum time[seconds] given to the child to end. 
# Command:   command that the child must execute.
#
# Return codes:
# ---------------------------------------------------------
#     -1        Unseccessfull fork
#     -2        Illegal parameter: time limit <= 0
#     -3        Illegal Command
#   -666        Child killed due to timelimit exceeded
# others        Child return code
#

sub spawnWithTimer{

    local($time_range)=$_[0];               # Time limit in seconds
    local($command)=$_[1];                  # Command
    local($outFile)= $_[2];
    local($child_out);                      # Child Output
    local($today)=time();                   # Starting time
    local($time_limit)=$today+$time_range;  # Termination time
    local($kid);                            # Variable used by waitpid
    local($now);                            # Actual Time
    local($pid);                            # Process PID
    local($child_killed)="NO";              # Initialization
    $child_rc=-1;                           # Initialization
    
    local($debug) = 0;
    if ( $time_range <= 0 ) {
       # Illegal parameter time limit
	return -2;
    }
   
    if ( $command eq "" ) {
       # Illegal Command
	return -3;
    }

    defined (my $pid = fork) or die "Cannot fork: $!";
    
   if ( $pid == 0 ) {       # *** Child process ***
      sleep 1;
      if ($outFile ne "") { 
	  system "$command > $outFile 2>&1";
      } else { 
	  system "$command ";
      }
      $child_rc=$?;
     
      if ($child_rc < 0) {
	  
          # Child process ended due to a command error
	  exit $child_rc;
      } else {
	  # Child ended correctly
	  # N.B.: return code is multiplied by 256
	  exit $child_rc/256;
      }
      
  } else {       # *** Father process ***
      do {
            
         # Check the child status (alive or not)
         # waitpid returns:
         #   1) the PID when the process is dead
         #   2) -1 if there are no child processes
         #   3) 0 if the FLAGS specify nonblocking and the process ins't  dead yet
	  print "Father ($$) checking for $pid status\n" if ($debug);
	  $kid = waitpid(-1,&WNOHANG);
	  
	  # The Father stores the R.C. of the child in case it finishes due
	  # due to some mistakes
	  $child_out=$?;
	  
	  sleep 10;
	  
	  # Get the time
	  $now=time();
	  
	  # Check if Time Limit is exceeded
	  if ($now > $time_limit) {
	      # Child is killed by the Father
	      print "TIMEOUT ($pid, descendants:".descendants($pid).")\n";
	      if (descendants($pid) ne "") { 
		  local($signalled) = 0;
		  foreach(split(/ /,descendants($pid))) { 
		      print "\nsignalling $_\n";
		      $signalled += kill 9,$_;
		  }
		  $child_killed="YES";
		  sleep 10;
		  print "TIMEOUT (signalled $signalled) Remaining: ".descendants($pid)."\n";
	      }
	  }
	  # The loop continues until the Child  is Killed or it
	  # naturally ends.
	  
      } until (($kid ==  $pid) ||  ($child_killed eq "YES"));
      
      # Cheking for which reason the child is ended/terminated
      if ($child_killed eq "YES" ) {
          # Child has been killed
          return -666;
      } else {
          if ($child_out < 0) {
	      # Child process ended due to a command error
	      print "Father finished waiting ($pid)\n" if ($debug);
	      return $child_out;
          } else {
	      # Child ended correctly
	      # N.B.: return code is multiplied by 256
	      print "Father finished waiting ($pid)\n" if ($debug);
	      return $child_out/256;
          }
      }
  }
}



1;

