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

# This will trigger debugging information for Java
# and generation of documentation for generated code.
DEBUG=on

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES     = acsexmplClient \
                  acsexmplAsyncCalls \
		  acsexmplClientDynamicComponent \
                  acsexmplClientWave \
                  acsexmplClientAlarmThread \
                  acsexmplClientListComponents \
                  acsexmplClientComponentIOR \
                  acsexmplClientFridgeCmd acsexmplClientFridge \
		  acsexmplClientAmsSeq \
		  acsexmplClientHelloWorld \
		  acsexmplClientHelloWorldSP \
		  acsexmplClientFridgeNC \
                  acsexmplClientErrorComponent
EXECUTABLES_L   = 

#
# <brief description of xxxxx program>
acsexmplClient_OBJECTS	   = acsexmplClient
acsexmplClient_LIBS	   = acsexmplMountStubs acsexmplCallbacksImpl maciClient

acsexmplAsyncCalls_OBJECTS	= acsexmplAsyncCalls acsexmplAsyncCallbacks acsexmplAsyncMethodCB
acsexmplAsyncCalls_LIBS	   	= acsexmplMountStubs maciClient

acsexmplClientWave_OBJECTS	   = acsexmplClientWave
acsexmplClientWave_LIBS	           = acsexmplMountStubs acsexmplCallbacksImpl maciClient

acsexmplClientAlarmThread_OBJECTS  = acsexmplClientAlarmThread
acsexmplClientAlarmThread_LIBS	   = acsexmplPowerSupplyStubs acsexmplCallbacksImpl maciClient

acsexmplClientFridgeCmd_OBJECTS	   = acsexmplClientFridgeCmd
acsexmplClientFridgeCmd_LIBS	   = acsexmplFridgeStubs acsnc maciClient

acsexmplClientFridge_OBJECTS	   = acsexmplClientFridge
acsexmplClientFridge_LIBS	   = acsexmplFridgeStubs acsnc acsexmplCallbacksImpl maciClient

acsexmplClientComponentIOR_OBJECTS 	   = acsexmplClientComponentIOR
acsexmplClientComponentIOR_LIBS       = maciClient

acsexmplClientListComponents_OBJECTS           = acsexmplClientListComponents
acsexmplClientListComponents_LIBS           = maciClient

acsexmplClientAmsSeq_OBJECTS	   = acsexmplClientAmsSeq
acsexmplClientAmsSeq_LIBS	   = acsexmplAmsSeqStubs maciClient

acsexmplClientHelloWorld_OBJECTS   = acsexmplClientHelloWorld
acsexmplClientHelloWorld_LIBS      = acsexmplHelloWorldStubs ACSErrTypeCommon maciClient

acsexmplClientHelloWorldSP_OBJECTS   = acsexmplClientHelloWorldSP
acsexmplClientHelloWorldSP_LIBS      = acsexmplHelloWorldStubs ACSErrTypeCommon maciClient

acsexmplClientFridgeNC_OBJECTS     = acsexmplClientFridgeNC
acsexmplClientFridgeNC_LIBS        = acsnc acsexmplFridgeStubs ACSErrTypeCommon maciClient

acsexmplClientDynamicComponent_OBJECTS = acsexmplClientDynamicComponent
acsexmplClientDynamicComponent_LIBS    = acsexmplHelloWorldStubs ACSErrTypeCommon maciClient

acsexmplClientErrorComponent_OBJECTS   = acsexmplClientErrorComponent
acsexmplClientErrorComponent_LIBS      = acsexmplErrorComponentStubs ACSErrTypeCommon maciClient

acsexmplCDB_OBJECTS = acsexmplCDB
acsexmplCDB_LIBS = 

#
# Includes (.h) files (public and local)
# ---------------------------------
INCLUDES =	acsexmplMountImpl.h \
		acsexmplPowerSupplyImpl.h \
		acsexmplExport.h \
		acsexmplRampedPowerSupplyImpl.h \
		acsexmplLampImpl.h \
		acsexmplFridgeImpl.h \
		acsexmplDoorImpl.h \
		acsexmplBuildingImpl.h \
		acsexmplAmsSeqImpl.h \
		acsexmplCalendarImpl.h \
		acsexmplHelloWorldImpl.h \
		acsexmplLongDevIO.h \
		acsexmplCallbacks.h \
		acsexmplAsyncCallbacks.h \
		acsexmplSlowMountImpl.h \
		acsexmplFilterWheelImpl.h \
		acsexmplErrorComponentImpl.h \
		acsexmplInitErrorHelloWorld.h \
		acsexmplConstrErrorHelloWorld.h
INCLUDES_L      = 

#
# Libraries (public and local)
# ----------------------------
LIBRARIES =	acsexmplCallbacksImpl \
		acsexmplHelloWorldImpl \
		acsexmplPowerSupplyImpl \
		acsexmplRampedPowerSupplyImpl \
		acsexmplMountImpl \
		acsexmplLampImpl \
		acsexmplFridgeImpl \
		acsexmplDoorImpl \
		acsexmplBuildingImpl \
		acsexmplAmsSeqImpl \
		acsexmplCalendarImpl \
		acsexmplSlowMountImpl \
		acsexmplLampWheelImpl \
		acsexmplFilterWheelImpl \
		acsexmplErrorComponentImpl \
		acsexmplInitErrorHelloWorld \
		acsexmplConstrErrorHelloWorld
LIBRARIES_L     =

acsexmplCallbacksImpl_OBJECTS   = acsexmplCallbacksImpl
acsexmplCallbacksImpl_LIBS	= baci
acsexmplCallbacksImpl_CFLAGS = -DACSEXMPL_BUILD_DLL

acsexmplPowerSupplyImpl_OBJECTS = acsexmplPowerSupplyImpl acsexmplPowerSupplyCurrentImpl acsexmplPowerSupplyDLL 
acsexmplPowerSupplyImpl_LIBS	= acsexmplPowerSupplyStubs baci
acsexmplPowerSupplyImpl_CFLAGS = -DACSEXMPL_BUILD_DLL

acsexmplRampedPowerSupplyImpl_OBJECTS	= acsexmplRampedPowerSupplyImpl \
	  	              acsexmplPowerSupplyImpl acsexmplPowerSupplyCurrentImpl 
acsexmplRampedPowerSupplyImpl_LIBS	= acsexmplRampedPowerSupplyStubs acsexmplPowerSupplyImpl
acsexmplRampedPowerSupplyImpl_CFLAGS = -DACSEXMPL_BUILD_DLL

acsexmplMountImpl_OBJECTS	= acsexmplMountImpl
acsexmplMountImpl_LIBS		= acsexmplMountStubs baci
acsexmplMountImpl_CFLAGS		= -DACSEXMPL_BUILD_DLL

acsexmplSlowMountImpl_OBJECTS	= acsexmplSlowMountImpl
acsexmplSlowMountImpl_LIBS	= acsexmplMountStubs baci
acsexmplSlowMountImpl_CFLAGS = -DACSEXMPL_BUILD_DLL

acsexmplLampImpl_OBJECTS 	= acsexmplLampImpl
acsexmplLampImpl_LIBS 		= acsexmplLampStubs baci
acsexmplLampImpl_CFLAGS 	= -DACSEXMPL_BUILD_DLL

acsexmplFridgeImpl_OBJECTS	= acsexmplFridgeImpl
acsexmplFridgeImpl_LIBS	   	= acsnc acsexmplFridgeStubs baci
acsexmplFridgeImpl_CFLAGS   	= -DACSEXMPL_BUILD_DLL

acsexmplDoorImpl_OBJECTS       	= acsexmplDoorImpl
acsexmplDoorImpl_LIBS     	= acsexmplBuildingStubs baci
acsexmplDoorImpl_CFLAGS     = -DACSEXMPL_BUILD_DLL

acsexmplBuildingImpl_OBJECTS   	= acsexmplBuildingImpl
acsexmplBuildingImpl_LIBS   	= acsexmplBuildingStubs baci
acsexmplBuildingImpl_CFLAGS   	= -DACSEXMPL_BUILD_DLL

acsexmplAmsSeqImpl_OBJECTS     	= acsexmplAmsSeqImpl
acsexmplAmsSeqImpl_LIBS     	= acsexmplAmsSeqStubs baci
acsexmplAmsSeqImpl_CFLAGS     	= -DACSEXMPL_BUILD_DLL

acsexmplCalendarImpl_OBJECTS   	= acsexmplCalendarImpl
acsexmplCalendarImpl_LIBS   	= acsexmplCalendarStubs baci
acsexmplCalendarImpl_CFLAGS   = -DACSEXMPL_BUILD_DLL

acsexmplHelloWorldImpl_OBJECTS 	= acsexmplHelloWorldImpl
acsexmplHelloWorldImpl_LIBS	= acsexmplHelloWorldStubs ACSErrTypeCommon acscomponent archiveevents
acsexmplHelloWorldImpl_CFLAGS	= -DACSEXMPL_BUILD_DLL

acsexmplLampWheelImpl_OBJECTS 	= acsexmplLampWheelImpl
acsexmplLampWheelImpl_LIBS	= acsexmplLampWheelStubs expat baci
acsexmplLampWheelImpl_CFLAGS	= -DACSEXMPL_BUILD_DLL

acsexmplFilterWheelImpl_OBJECTS = acsexmplFilterWheelImpl
acsexmplFilterWheelImpl_LIBS = acsexmplFilterWheelStubs baci
acsexmplFilterWheelImpl_CFLAGS = -DACSEXMPL_BUILD_DLL

acsexmplErrorComponentImpl_OBJECTS 	= acsexmplErrorComponentImpl
acsexmplErrorComponentImpl_LIBS	= acsexmplErrorComponentStubs ACSErrTypeCommon acscomponent archiveevents
acsexmplErrorComponentImpl_CFLAGS	= -DACSEXMPL_BUILD_DLL

acsexmplInitErrorHelloWorld_OBJECTS 	= acsexmplInitErrorHelloWorld
acsexmplInitErrorHelloWorld_LIBS	= acsexmplHelloWorldStubs ACSErrTypeCommon acsexmplErrTest acscomponent archiveevents
acsexmplInitErrorHelloWorld_CFLAGS 	= -DACSEXMPL_BUILD_DLL

acsexmplConstrErrorHelloWorld_OBJECTS 	= acsexmplConstrErrorHelloWorld
acsexmplConstrErrorHelloWorld_LIBS	= acsexmplHelloWorldStubs ACSErrTypeCommon acsexmplErrTest acscomponent archiveevents
acsexmplConstrErrorHelloWorld_CFLAGS 	= -DACSEXMPL_BUILD_DLL

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         = acsexmplPSPanel
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
CDB_SCHEMAS = AmsSeq  Calendar Fridge \
              LAMP LAMPWHEEL MOUNT \
              PowerSupply RampedPowerSupply \
              Building Door Tower \
              FILTERWHEEL 
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
IDL_FILES = 	acsexmplPowerSupply \
		acsexmplMount \
		acsexmplRampedPowerSupply \
		acsexmplLamp \
		acsexmplFridge \
		acsexmplBuilding \
		acsexmplAmsSeq \
		acsexmplCalendar \
		acsexmplHelloWorld \
		acsexmplLampWheel \
		acsexmplFilterWheel \
                acsexmplErrorComponent
USER_IDL = 
acsexmplPowerSupplyStubs_LIBS = baciStubs
acsexmplMountStubs_LIBS = baciStubs
acsexmplRampedPowerSupplyStubs_LIBS = acsexmplPowerSupplyStubs
acsexmplLampStubs_LIBS = baciStubs
acsexmplFridgeStubs_LIBS = baciStubs
acsexmplBuildingStubs_LIBS = baciStubs acserrStubs
acsexmplAmsSeqStubs_LIBS = baciStubs
acsexmplCalendarStubs_LIBS = baciStubs
acsexmplHelloWorldStubs_LIBS = acscomponentStubs ACSErrTypeCommon
acsexmplLampWheelStubs_LIBS = baciStubs
acsexmplFilterWheelStubs_LIBS = baciStubs
acsexmplErrorComponentStubs_LIBS = acscomponentStubs ACSErrTypeCommon

ACSERRDEF = 	acsexmplErrTest
