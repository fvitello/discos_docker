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
#------------------------------------------------------------------------

USER_CFLAGS =
USER_LIB =
USER_INC = 

# Program to build
# Can be overwritten in the call

PROG = 

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     = $(PROG)
EXECUTABLES_L   = 

#
# <brief description of xxxxx program>

$(PROG)_OBJECTS            = $(PROG)
$(PROG)_LDFLAGS            =
$(PROG)_LIBS               = C++ CCS


#
# Includes (.h) files (public and local)
# ---------------------------------
INCLUDES        = acsutil.h acsutilWildcard.h acsutilExport.h acsutilThreadInit.h acsutilFindFile.h acsutilLL.h acsutilTempFile.h acsutilPorts.h \
		  acsutilAnyAide.h acsutilTimeStamp.h acsutilORBHelper.h
INCLUDES_L      = 

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = acsutil
LIBRARIES_L     =

acsutil_OBJECTS	= acsutilWildcard acsutilFindFile acsutilLL acsutilTempFile acsutilPortsImpl acsutilAnyAide acsutilTimeStamp acsutilORBHelper
acsutil_LIBS    = acscommonStubs

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         = acsutilTATPrologue acsutilTATEpilogue acsutilTATTestRunner acsutilProfiler acsutilDiffTrap acsutilRedo \
		  acsutilBlock acsutilAwaitContainerStart 
SCRIPTS_L       = 

#
# TCL scripts (public and local)
# ------------------------------
TCL_SCRIPTS     =
TCL_SCRIPTS_L   =

#
# Python scripts (public and local)
# ------------------------------
PY_SCRIPTS     = 
PY_SCRIPTS_L   =

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
# IDL FILES
#
IDL_FILES =
