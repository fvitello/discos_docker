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
#USER_INC =

#
# additional include and library search paths
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
# Includes (.h) files (public and local)
# ---------------------------------
INCLUDES        = cdbField.h cdb.h cdb.i cdbData_Types.h cdbINIReader.h cdbExport.h cdbDALaccess.h cdbDAOImpl.h cdbDAONode.h cdbDAOProxy.h
INCLUDES_L      =

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = cdb
LIBRARIES_L     =

cdb_OBJECTS   = cdbField cdb cdbIMDB cdbINIReader cdbDALaccess cdbDAOImpl cdbDAONode cdbDAOProxy
cdb_LIBS      = cdbDALStubs ACSErrTypeCommon ACSErrTypeCORBA cdbErrType acscomponentStubs logging expat
cdb_CFLAGS    = -DCDB_BUILD_DLL

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         = cdbjDAL cdbjDALShutdown cdbjDALClearCache cdbRead cdbWrite cdbTATPrologue cdbTATEpilogue cdbSetDefaultComponent cdbChangeComponentDeployment cdbAddImplLang.py 
SCRIPTS_L       =

INSTALL_FILES := ../lib/openjdk-orb-jdk9-supplement-1.0.1.Final.jar

DEBUG=on

JARFILES:=CDB
CDB_DIRS:=com
CDB_JARS:=jACSUtil acsjlog cdbjDAL repeatGuard

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

#
# Configuration Database Schema Files
# ----------------------
CDB_SCHEMAS =

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
