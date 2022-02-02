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

#
# additional include and library search paths
#USER_INC = 
USER_LIB =

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     = acsservicesdaemon acsservicesdaemonStop \
                  acsdaemonNamingServiceImp acsdaemonNotificationServiceImp \
                  acsdaemonInterfaceRepositoryImp acsdaemonLoggingServiceImp \
                  acsdaemonACSLogServiceImp acsdaemonConfigurationDatabaseImp \
                  acsdaemonManagerImp acsdaemonAlarmServiceImp \
                  acscontainerdaemon acsdaemonStartContainer \
                  acsdaemonStopContainer acsdaemonStartAcs \
                  acsdaemonStopAcs acsdaemonStatusAcs \
                  acscontainerdaemonSmartStart acscontainerdaemonStop \
                  acsdaemonImpStop
EXECUTABLES_L   = 

#
# <brief description of xxxxx program>
acscontainerdaemon_OBJECTS   =  acscontainerdaemon acsContainerHandlerImpl acsdaemonORBTask acsRequest acsServiceController acsDaemonUtils
acscontainerdaemon_LIBS      =  acscomponentStubs acsdaemonErrType acsdaemonStubs ACSErrTypeCommon acsThread acsQoS ACSErrTypeCommon ACSErrTypeCORBA maciErrType maciStubs AcsAlarmSystemStubs

acsdaemonStartContainer_OBJECTS   =	acsdaemonStartContainer
acsdaemonStartContainer_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonStopContainer_OBJECTS   =	acsdaemonStopContainer
acsdaemonStopContainer_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsservicesdaemon_OBJECTS   =	acsservicesdaemon acsServicesHandlerImpl acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsservicesdaemon_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread expat acsQoS maciErrType maciStubs AcsAlarmSystemStubs ACSErrTypeCORBA

acsdaemonStartAcs_OBJECTS   =	acsdaemonStartAcs
acsdaemonStartAcs_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs


acsdaemonStopAcs_OBJECTS   =	acsdaemonStopAcs
acsdaemonStopAcs_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonStatusAcs_OBJECTS   =	acsdaemonStatusAcs
acsdaemonStatusAcs_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acscontainerdaemonStop_OBJECTS   =	acscontainerdaemonStop
acscontainerdaemonStop_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acscontainerdaemonSmartStart_OBJECTS   =	acscontainerdaemonSmartStart
acscontainerdaemonSmartStart_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsservicesdaemonStop_OBJECTS   =	acsservicesdaemonStop
acsservicesdaemonStop_LIBS      =	acscomponentStubs logging acsdaemonErrType acsdaemonStubs maciErrType acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonNamingServiceImp_OBJECTS   =	acsdaemonNamingServiceImp acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsdaemonNamingServiceImp_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonNotificationServiceImp_OBJECTS   =	acsdaemonNotificationServiceImp acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsdaemonNotificationServiceImp_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs TAO_CosNotification_Serv

acsdaemonInterfaceRepositoryImp_OBJECTS   =	acsdaemonInterfaceRepositoryImp acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsdaemonInterfaceRepositoryImp_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonLoggingServiceImp_OBJECTS   =	acsdaemonLoggingServiceImp acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsdaemonLoggingServiceImp_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonACSLogServiceImp_OBJECTS   =	acsdaemonACSLogServiceImp acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsdaemonACSLogServiceImp_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonConfigurationDatabaseImp_OBJECTS   =	acsdaemonConfigurationDatabaseImp acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsdaemonConfigurationDatabaseImp_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonManagerImp_OBJECTS   =	acsdaemonManagerImp acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsdaemonManagerImp_LIBS      =	acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonAlarmServiceImp_OBJECTS   = acsdaemonAlarmServiceImp acsRequest acsServiceController acsdaemonORBTask acsDaemonUtils
acsdaemonAlarmServiceImp_LIBS      = acscomponentStubs acsdaemonErrType acsdaemonStubs acsThread acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

acsdaemonImpStop_OBJECTS   =       acsdaemonImpStop
acsdaemonImpStop_LIBS      =       acscomponentStubs acsdaemonErrType acsdaemonStubs acsutil ACSErrTypeCommon ACSErrTypeCORBA acsQoS acsAlSysSource AcsAlarmSystemStubs

#
# Includes (.h) files (public only)
# ---------------------------------
INCLUDES        = acsDaemonImpl.h acsdaemonORBTask.h

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       =
LIBRARIES_L     =

#
# <brief description of lllll library>
lllll_OBJECTS   =

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         = acsdaemonImpStopAll acsStartRemoteTmcdb
SCRIPTS_L       =

#
# TCL scripts (public and local)
# ------------------------------
TCL_SCRIPTS     =
TCL_SCRIPTS_L   =

#
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         = ResourcesDaemon acsdaemonContainerUtil
PY_SCRIPTS_L       =

PY_MODULES         = ACSContainerDaemonUtils
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
# Configuration Database Files
# ----------------------------
CDB_SCHEMAS = 

# 
# IDL Files and flags
# 

IDL_FILES = 
IDL_TAO_FLAGS =
USER_IDL =


ACSERRDEF = 

#
# Jarfiles and their directories
#
JARFILES := acsdaemonUtils
acsdaemonUtils_DIRS := alma

#
# java sources in Jarfile on/off
DEBUG=on

#
# ACS XmlIdl generation on/off
#
XML_IDL= 
#
# Java Component Helper Classes generation on/off
#
COMPONENT_HELPERS=
#
# Java Entity Classes generation on/off
#
XSDBIND=
#
# Schema Config files for the above
#
XSDBIND_INCLUDE=
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
# other files to be installed
#----------------------------
INSTALL_FILES =
