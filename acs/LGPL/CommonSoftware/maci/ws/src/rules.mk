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
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     = maciContainer maciManagerShutdown maciContainerShutdown maciReleaseComponent maciContainerLogLevel maciContainerLogStatsConfiguration maciComponentRefDump
EXECUTABLES_L   = 

maciContainer_OBJECTS   = maciContainer 
maciContainer_LIBS = maci acsContainerServices acsComponentListener logging TAO_CosNotification_MC_Ext TAO_DynamicInterface

maciManagerShutdown_OBJECTS = maciManagerShutdown
maciManagerShutdown_LIBS = maci

maciContainerShutdown_OBJECTS = maciContainerShutdown
maciContainerShutdown_LIBS = maci

maciReleaseComponent_OBJECTS = maciReleaseComponent
maciReleaseComponent_LIBS = maci

maciContainerLogLevel_OBJECTS = maciContainerLogLevel
maciContainerLogLevel_LIBS = maci

maciContainerLogStatsConfiguration_OBJECTS = maciContainerLogStatsConfiguration
maciContainerLogStatsConfiguration_LIBS = maci

maciComponentRefDump_OBJECTS = maciComponentRefDump
maciComponentRefDump_LIBS= maciClient

#
# Includes (.h) files (public and local)
# ---------------------------------
INCLUDES	= maciHelper.h maciSimpleClient.h \
              maciSimpleClientThreadHook.h maciExport.h maciContainerServices.h \
              maciContainerImpl.h maciContainerThreadHook.h \
		      maciACSComponentDefines.h maciPropertyDefines.h \
              maciLibraryManager.h maciRegistrar.h maciComponentStateManager.h\
		      maciRegistrar.i maciClientExport.h maciLogThrottleAlarmImpl.h
INCLUDES_L	= maciContainer.h maciServantManager.h \
		      maciORBTask.h 

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = maci maciClient
LIBRARIES_L     =

maci_OBJECTS   = maciLibraryManager maciORBTask  maciContainerThreadHook \
		 maciServantManager maciContainerServices maciContainerImpl \
		 maciHelper maciComponentStateManager maciLogThrottleAlarmImpl
maci_LIBS = acsContainerServices maciErrType acsErrTypeContainerServices maciStubs acsAlSysSource acsErrTypeAlarmSourceFactory AcsContainerLogLTS alarmSource acscomponent acsQoS archiveevents
maci_LDFLAGS = -ggdb
maci_CFLAGS = -DMACI_BUILD_DLL

maciClient_OBJECTS =  maciSimpleClient maciSimpleClientThreadHook
maciClient_LIBS    =  baci maci maciErrType
maciClient_CFLAGS = -DMACICLIENT_BUILD_DLL

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

#
# On-Line Database Files
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
# Java: add debug info and sources
#
DEBUG=on

#
# IDL FILES
#
IDL_FILES =
