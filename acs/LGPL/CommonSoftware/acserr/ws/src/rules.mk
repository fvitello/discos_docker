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

PY_PACKAGES = ACSErrorChecker
DEBUG=on

#
# user definable C-compilation flags

#
# additional include and library search paths
USER_LIB =

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
INCLUDES        = acserr.h acserrExceptionManager.h acserrLegacy.h acserrACSbaseExImpl.h \
		 acserrHandlers.h acserrGenExport.h

 
#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = acserr
LIBRARIES_L     =

#
# <brief description of lllll library>
acserr_OBJECTS = acserr acserrExceptionManager \
		 acserrLegacy acserrACSbaseExImpl \
		 acserrHandlers
acserr_LIBS = logging loki acsutil acserrStubs acserrHandlersErrStubs
acserr_CFLAGS = -DACSERRGEN_BUILD_DLL

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         = acserrGenCpp acserrGenJava acserrGenIDL acserrGenPython updateErrDefs.sh acserrGenCheckXML
SCRIPTS_L       =

acserrGenCheckXML_DEPS:=$(mod_acserr_name)_xmlvalidator_jar

#
# other files to be installed
#----------------------------
INSTALL_FILES = ../config/AES2H.xslt ../config/AES2CPP.xslt ../config/AES2IDL.xslt ../config/AES2Java.xslt ../config/AES2Py.xslt ../idl/ACSError.xsd

#
# IDL FILES
#
IDL_FILES =

XSDBIND = ACSError
XSDBIND_INCLUDE = commontypes

#
# Jarfiles and their directories
#
JARFILES = acserrj xmlvalidator
acserrj_DIRS = alma/acs/exceptions
acserrj_JARS:=jACSUtil acserr
xmlvalidator_DIRS = alma/acs/xml/validator
