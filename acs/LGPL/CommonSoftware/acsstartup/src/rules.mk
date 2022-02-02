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

USER_LIB =

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         = \
		acsStartContainer \
		acsStopContainer \
		acsStartORBSRVC acsStopORBSRVC \
		acsStartManager acsStopManager \
		acsStart acsStop \
		acsstartupAcsPorts acsstartupPids \
		acsStartJava \
		acsList \
		acsKillProc \
		irbrowser nsbrowser \
		acsStartLight \
		acsstartupConstants \
		acsstartupAcsInstance \
		acsstartupLogging.sh \
		acsstartupReport \
		acsStatus acsstartupLoadIFR \
		acsstartupProcesses \
		acsACSLogService \
		acsConfigurationDatabase \
		acsRDBConfigurationDatabase \
		acsInterfaceRepository \
		acsLoggingService \
		acsManager \
		acsNamingService \
		acsNotifyService acsstartupNotifyServiceStart \
		acsAlarmService \
		acsStartContainerWithFortran \
		acsstartupNotifyPortViaErrorCode \
		acsLogProcessMem \
		ifrCacheInvalidate irquery \
		acsstartupNSRef acsQueryNS \
		acsstartupFreeInstanceDir

SCRIPTS_L       =

#
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         = killACS acsstartupContainerPort acsstartupNotifyPort \
		     acsstartupRemovePID \
		     acsContainersStatus acsNotifysStatus \
		     acsstartupCreateChannel \
                     acsdataClean acsConfigureNotificationChannels
PY_SCRIPTS_L       =

PY_MODULES         =
PY_MODULES_L       =

PY_PACKAGES        =
PY_PACKAGES_L      =
pppppp_MODULES	   =

EXECUTABLES = acsstartupIrFeed
MAKE_NOIFR_CHECK = on # jagonzal: there is a cyclic dependency between acsstartupIrFeed and acsstartupLoadIFR (checker)

acsstartupIrFeed_OBJECTS = acsstartupIrFeed

# 
# IDL Files and flags
# 
IDL_FILES = ACSIRSentinel
TAO_IDLFLAGS =
USER_IDL =

#
# Jarfiles and their directories
#
JARFILES=
acsstartup_DIRS=
acsstartup_EXTRAS= 

#
# java sources in Jarfile on/off
DEBUG= on

#
# other files to be installed
#----------------------------
INSTALL_FILES =

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

#
# man pages to be done
# --------------------
MANSECTIONS = 1
MAN1 = 
