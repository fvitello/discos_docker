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
USER_CFLAGS = 

#
# This will trigger debugging information for Java
# and generation of documentation for generated code.
#
DEBUG=on

#
# additional include and library search paths
#USER_INC =
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
xxxxx_OBJECTS     =
xxxxx_LIBS        = 


#
# special compilation flags for single c sources
#yyyyy_CFLAGS   = 

#
# Includes (.h) files (public only)
# ---------------------------------
INCLUDES        = acssampImpl.h acssampObjImpl.h acssampObjImpl.i

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = acssamp
LIBRARIES_L     =

#
# <brief description of lllll library>
acssamp_OBJECTS   = acssampImpl acssampC acssampS
acssamp_LIBS = acsutil cdb logging acscomponent baci maci maciClient acsnc acssampStubs TAO_DynamicInterface

#
# Configuration Database Files
# ----------------------
CDB_SCHEMAS = SAMP


# 
# IDL Files and flags
# 
IDL_FILES = acssamp
IDL_TAO_FLAGS =
USER_IDL =
acssampStubs_LIBS = baciStubs ACSErrTypeCommon
