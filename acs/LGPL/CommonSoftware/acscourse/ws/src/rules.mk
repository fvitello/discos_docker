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
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# Flag to switch on debugging information
# ----------------------------
#
DEBUG=on

#
# Error definition files for exceptions
# and completions
# ----------------------------
#
ACSERRDEF = ACSErrTypeACSCourse

#
# Java sources
# ----------------------------
#
JARFILES =              acscourseImpl
acscourseImpl_DIRS   =  alma
acscourseImpl_EXTRAS =
acscourseImpl_JLIBS = acscourseMount

XSDBIND = acscourseEntities
XSDBIND_INCLUDE = systementities   

COMPONENT_HELPERS=on

# defines the Java binding class to be used for the corresponding IDL typedefs
XML_IDL="MyXmlConfigData=alma.acscourse.xmlbinding.myxmlconfigdata.MyXmlConfigData"

#
# CPP sources
# ----------------------------
#
USER_CFLAGS =
USER_LIB =

#
# Includes (.h) files (public and local)
# 
INCLUDES =	acscourseMount1Impl.h \
	        acscourseMount2Impl.h acscourseMount2LoopImpl.h \
		acscourseMount3Impl.h acscourseMount4Impl.h acscourseMount5Impl.h

#
# Libraries (public and local)
# 
LIBRARIES =	acscourseMount1Impl acscourseMount2Impl acscourseMount2LoopImpl acscourseMount3Impl acscourseMount4Impl acscourseMount5Impl

acscourseMount1Impl_OBJECTS	= acscourseMount1Impl
acscourseMount1Impl_LIBS	= acscourseMountStubs xmlentityStubs ACSErrTypeACSCourse acscomponent archiveevents

acscourseMount2Impl_OBJECTS	= acscourseMount2Impl acscourseMount2ImplDLL
acscourseMount2Impl_LIBS	= acscourseMountStubs xmlentityStubs ACSErrTypeACSCourse acscomponent baci archiveevents

acscourseMount2LoopImpl_OBJECTS	= acscourseMount2LoopImpl acscourseMount2Impl
acscourseMount2LoopImpl_LIBS	= acscourseMountStubs xmlentityStubs ACSErrTypeACSCourse acscomponent baci archiveevents

acscourseMount3Impl_OBJECTS	= acscourseMount3Impl 
acscourseMount3Impl_LIBS	= acscourseMountStubs xmlentityStubs ACSErrTypeACSCourse acscomponent baci archiveevents

acscourseMount4Impl_OBJECTS	= acscourseMount4Impl
acscourseMount4Impl_LIBS	= acscourseMountStubs xmlentityStubs ACSErrTypeACSCourse acscomponent baci archiveevents

acscourseMount5Impl_OBJECTS	= acscourseMount5Impl
acscourseMount5Impl_LIBS	= acscourseMountStubs xmlentityStubs acsnc ACSErrTypeACSCourse acscomponent baci archiveevents

#
# Configuration database schema Files
# ------------------------------------
CDB_SCHEMAS = ACSCourseMount ACSCourseMountRW

#
# IDL FILES
#----------------------------
IDL_FILES = 	acscourseMount
USER_IDL = 
acscourseMountStubs_LIBS = baciStubs ACSErrTypeACSCourse ACSErrTypeCommon xmlentityStubs

#
# Python sources
#----------------------------
PY_SCRIPTS = mountSimple mountCallback acscourseMountSupplier acscourseMountConsumer

PY_PACKAGES = ACSCOURSE_MOUNTImpl
