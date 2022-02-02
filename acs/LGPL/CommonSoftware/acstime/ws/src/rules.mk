#*******************************************************************************
# ALMA - Atacama Large Millimeter Array
# Copyright (c) Associated Universities Inc., 2020
# (in the framework of the ALMA collaboration).
# All rights reserved.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#*******************************************************************************

#*******************************************************************************
# This Makefile follows ACS Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#-------------------------------------------------------------------------------

#
# user definable C-compilation flags
#USER_CFLAGS = 

DEBUG=on

#
# additional include and library search paths
USER_INC = -I$(PYTHONINC)
#USER_LIB = 

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     =
EXECUTABLES_L   = 

#
# <brief description of xxxxx program>
xxxxx_OBJECTS   =	
xxxxx_LDFLAGS   =
xxxxx_LIBS      =

#
# special compilation flags for single c sources
#yyyyy_CFLAGS   = 

#
# Includes (.h) files (public only)
# ---------------------------------
INCLUDES        = 	acstimeClockImpl.h \
			acstimeTimerImpl.h \
			acstimeDevIOTime.h acstimeTimeUtil.h \
			acstimeDurationHelper.h acstimeEpochHelper.h \
			acstime.h \
			acstimeProfiler.h

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = acstime acsclock acstimer _acstimeSWIG 
LIBRARIES_L     =

acstime_OBJECTS   =	acstimeTimeUtil \
			acstimeDurationHelper acstimeEpochHelper \
			acstimeProfiler \
			acstimeC acstimeS
acstime_LIBS      =     baci ACSTimeError maciErrType

acsclock_OBJECTS   =	acstimeClockImpl
acsclock_LIBS      =    baci ACSTimeError acstime


acstimer_OBJECTS   =	acstimeTimerImpl
acstimer_LIBS      =    baci ACSTimeError acstime

#Quick fix a Sun-SWIG problem is to NOT compile acstimeSWIG_wrap for Sun
_acstimeSWIG_OBJECTS:=$(shell if [ $(shell uname) = Linux -o $(shell uname) = $(CYGWIN_VER) ]; then echo 'acstimeSWIG_wrap '; fi;) \
			 acstimeTimeUtil \
			 acstimeDurationHelper acstimeEpochHelper \
			 acstimeC acstimeS
_acstimeSWIG_LDFLAGS=-L$(PYTHON_ROOT)/lib -Wl,-rpath=$(PYTHON_ROOT)/lib -l$(PYTHONLIB)
_acstimeSWIG_LIBS:=baci ACSTimeError maciErrType

#
# <brief description of lllll library>
lllll_OBJECTS   =

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         =
SCRIPTS_L       =

#
# TCL scripts (public and local)
# ------------------------------
TCL_SCRIPTS     =
TCL_SCRIPTS_L   =

#
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         =
PY_SCRIPTS_L       =

PY_MODULES         = acstimeSWIG
PY_MODULES_L       =

PY_PACKAGES        =
PY_PACKAGES_L      =
pppppp_MODULES	   =

#
# <brief description of tttttt tcl-script>
tttttt_OBJECTS  =
tttttt_TCLSH    = 
tttttt_LIBS     = 

#
# TCL libraries (public and local)
# ------------------------------
TCL_LIBRARIES   =
TCL_LIBRARIES_L =

#
# <brief description of tttlll library>
tttlll_OBJECTS  = 

#
# UIF panels (public and local)
# ----------------------------
PANELS   =
PANELS_L = 

# Self-explanatory
CDB_SCHEMAS = CLOCK

# 
# IDL Files and flags
# 
IDL_FILES = acstime
IDL_TAO_FLAGS =
USER_IDL =

acstimeStubs_LIBS = baciStubs ACSTimeErrorStubs

#ACS Error System target
ACSERRDEF = ACSTimeError

#
# Jarfiles and their directories
#
JARFILES = jacstime
jacstime_JLIBS=acstime
jacstime_DIRS = alma

#
# man pages to be done
# --------------------
MANSECTIONS =
MAN1 =
MAN3 =
MAN5 =
MAN7 =
MAN8 =

#
# local man pages
# ---------------
MANl =

#
# ASCII file to be converted into Framemaker-MIF
# --------------------
ASCII_TO_MIF = 

#
# INS_ROOT files to be installed
#-------------------------------
INS_ROOT_FILES =
INS_ROOT_DIR   =

#
# other files to be installed
#----------------------------
INSTALL_FILES =
