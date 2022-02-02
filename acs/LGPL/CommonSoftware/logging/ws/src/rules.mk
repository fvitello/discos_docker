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
#USER_INC =

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     = loggingService loggingClient
EXECUTABLES_L   = 

loggingService_OBJECTS = loggingService loggingACSLogFactory_i \
                         loggingACSStructuredPushSupplier \
                         loggingACSStructuredPushSupplierBin \
                         loggingACSStructuredPushSupplierXml loggingACSLog_i \
                         loggingHelper  loggingAcsLogServiceImpl

loggingService_LIBS = logging baselogging logging_idlStubs acsutil acscommonStubs loki log4cpp TAO_CosNotification_Serv TAO_CosNaming_Serv

loggingClient_OBJECTS = loggingClient loggingHelper 
loggingClient_LIBS = logging baselogging logging_idlStubs acsutil acscommonStubs loki log4cpp TAO_CosNotification_Serv TAO_CosNaming_Serv

#
# Includes (.h) files (public and local)
# ---------------------------------
INCLUDES        = 	logging.h loggingExport.h loggingBaseExport.h loggingXMLElement.h loggingXMLParser.h \
		  	loggingClient.h loggingLocalSyslog.h loggingRemoteSyslog.h loggingLocalFile.h \
			loggingCacheLogger.h \
			loggingBaseLog.h loggingStatistics.h loggingHandler.h loggingLogger.h loggingLoggable.h \
                        loggingLogTrace.h loggingACSLogger.h \
			loggingLogSvcHandler.h  loggingLogLevelDefinition.h \
			loggingLoggingTSSStorage.h loggingLoggingProxy.h loggingMACROS.h \
			loggingACEMACROS.h loggingGetLogger.h loggingGenericLogger.h loggingStdoutHandler.h \
			loggingAcsLogServiceImpl.h loggingStdoutlayout.h loggingXmlLayout.h\
            loggingACSCategory.h loggingACSLoggingEvent.h loggingLogThrottle.h \
            loggingACSRemoteAppender.h loggingLog4cpp.h loggingLog4cppMACROS.h \
			loggingLog4cppACEMACROS.h loggingACSHierarchyMaintainer.h loggingThrottleAlarmInterface.h \
			loggingStopWatch.h loggingExecuteWithLogger.h loggingExecuteWithLogger.i
INCLUDES_L      = loggingService.h \
		  loggingACSStructuredPushSupplier.h \
		  loggingACSStructuredPushSupplierXml.h \
          loggingACSStructuredPushSupplierBin.h \
          loggingACSLogFactory_i.h loggingACSLog_i.h \
		  loggingHelper.h logging ACSHierarchyMaintainer.h 

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = baselogging logging 
LIBRARIES_L     =

baselogging_OBJECTS =	loggingBaseLog loggingStatistics loggingHandler loggingLogger \
                        loggingLogTrace loggingGetLogger loggingGenericLogger \
                        loggingStdoutHandler \
                        loggingLoggingProxy loggingLocalFile loggingXMLElement \
                        loggingRemoteSyslog loggingLocalSyslog loggingACSLogger loggingLogSvcHandler  \
						loggingLogThrottle \
						loggingACSLoggingEvent loggingXmlLayout \
						loggingStdoutlayout loggingACSCategory \
						loggingACSRemoteAppender loggingACSHierarchyMaintainer \
						loggingLog4cpp loggingStopWatch loggingLogLevelDefinition loggingXMLParser

baselogging_LIBS =    acsutil loki log4cpp logging_idlStubs
baselogging_LDFLAGS = -ggdb
baselogging_CFLAGS  = -DLOGGINGBASE_BUILD_DLL
logging_OBJECTS 	=	logging loggingLoggable loggingXMLElement loggingXMLParser \
						loggingLocalSyslog loggingRemoteSyslog loggingLocalFile \
						loggingLoggingTSSStorage loggingLogLevelDefinition \
						loggingLoggingProxy loggingACSLogger loggingLogSvcHandler  \
						loggingLogThrottle \
						loggingACSLoggingEvent loggingXmlLayout \
						loggingStdoutlayout loggingACSCategory \
						loggingACSRemoteAppender loggingACSHierarchyMaintainer \
						loggingLog4cpp loggingStopWatch
						
logging_LIBS    =	logging_idlStubs acsutil acscommonStubs baselogging log4cpp pthread rt
logging_LDFLAGS =	-ggdb
logging_CFLAGS  = -DLOGGING_BUILD_DLL

#log4cpplogging_OBJECTS 	:= 	loggingACSLoggingEvent loggingXmlLayout \
#							loggingStdoutlayout loggingACSCategory \
#							loggingACSRemoteAppender loggingACSHierarchyMaintainer \
#							loggingLog4cpp loggingLogThrottle
#log4cpplogging_LIBS		:=	log4cpp logging_idlStubs

#
# other files to be installed
#----------------------------
INSTALL_FILES = ../idl/loggingMI.xsd

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         = 
SCRIPTS_L       =

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
