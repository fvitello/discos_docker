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
# IDL Files and flags
# 
IDL_FILES = HelloDemo XmlComponent LampAccess LampCallback ErrorSystemComponent EventComponent
IDL_TAO_FLAGS =
USER_IDL =

HelloDemoStubs_LIBS = acscomponentStubs

XmlComponentStubs_LIBS = acscomponentStubs JContExmplErrTypeTestStubs xmlentityStubs 

LampAccessStubs_LIBS = acscomponentStubs acserrStubs

LampCallbackStubs_LIBS = acscomponentStubs acserrStubs LampAccessStubs

ErrorSystemComponentStubs_LIBS = ErrorSystemExampleStubs acscomponentStubs

EventComponentStubs_LIBS = acsexmplFridgeStubs ACSErrTypeCommonStubs acscomponentStubs

ACSERRDEF = JContExmplErrTypeTest ErrorSystemExample

#
# Jarfiles: We must distinguish between component implementation classes and other classes.
#
COMPONENTS_JARFILES = jcontexmplComp

jcontexmplComp_DIRS = \
	alma/demo/ErrorSystemComp \
	alma/demo/EventConsumerImpl \
	alma/demo/EventSupplierImpl \
	alma/demo/ComponentWithXmlOffshootImpl \
	alma/demo/HelloDemoImpl \
	alma/demo/LampAccessImpl \
	alma/demo/LampCallbackImpl \
	alma/demo/XmlComponentImpl \
	alma/demo/XmlOffShootImpl \
	alma/acsexmplErrorComponent
jcontexmplComp_JLIBS = LampAccess EventComponent ErrorSystemComponent LampCallback XmlComponent


JARFILES = jcontexmplClient

jcontexmplClient_DIRS = \
	alma/demo/client \
	alma/demo/dyncomp \
	alma/demo/EventConsumerImpl \
	alma/acsexmpl/clients
jcontexmplClient_JLIBS= XmlComponent LampCallback EventComponent

DEBUG = on
#COMPONENT_HELPERS=on

XML_IDL="ObsProposal=alma.xmljbind.test.obsproposal.ObsProposal; \
         SchedBlock=alma.xmljbind.test.schedblock.SchedBlock"

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         =
SCRIPTS_L       =
