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

USER_CFLAGS =
USER_LIB =

#
# Jarfiles: We must distinguish between component implementation classes and other classes.
#
COMPONENTS_JARFILES = contLogTestComp

contLogTestComp_DIRS = \
	alma/contLogTest/TestLogLevelsCompImpl
contLogTestComp_JLIBS = contLogTest_IF

JARFILES = contLogTestClient

contLogTestClient_DIRS = \
	alma/contLogTest/client
contLogTestClient_JLIBS = contLogTest_IF

#
# This will trigger debugging information for Java
# and generation of documentation for generated code.
#
DEBUG = on
COMPONENT_HELPERS=on

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# Type-safe log code generation
ACSLOGTSDEF = typeSafeLogs

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     =
EXECUTABLES_L   = 

#
# Includes (.h) files (public and local)
# ---------------------------------
INCLUDES =	contLogTestImpl.h
INCLUDES_L      = 

#
# Libraries (public and local)
# ----------------------------
LIBRARIES =	contLogTestImpl
LIBRARIES_L     =

contLogTestImpl_OBJECTS = contLogTestImpl
contLogTestImpl_LIBS    = contLogTest_IFStubs ACSErrTypeCommon acscomponent archiveevents

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

# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         = 
PY_SCRIPTS_L       =

PY_MODULES         = 
PY_MODULES_L       =

PY_PACKAGES        = pyContLogTest
PY_PACKAGES_L      =
pppppp_MODULES	   =
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
# IDL Files and flags
# 
IDL_FILES = contLogTest_IF
IDL_TAO_FLAGS =
USER_IDL =

contLogTest_IFStubs_LIBS = acscomponentStubs ACSErrTypeCommonStubs
