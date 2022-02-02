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
INCLUDES        =
INCLUDES_L      = CounterSupplierImpl.h

#
# Libraries (public and local)
# ----------------------------
LIBRARIES       = CounterConsumerImpl CounterSupplierImpl 
LIBRARIES_L     =

#
# <brief description of lllll library>
CounterSupplierImpl_OBJECTS   = CounterSupplierImpl
CounterSupplierImpl_LIBS      = contNcTest_IFStubs ACSErrTypeCommon acsnc acscomponent

CounterConsumerImpl_OBJECTS   = CounterConsumerImpl
CounterConsumerImpl_LIBS      = contNcTest_IFStubs ACSErrTypeCommon acsnc acscomponent

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
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         = contNcTestCounterSupplier.py contNcTestCounterConsumer.py
PY_SCRIPTS_L       =

PY_MODULES         =
PY_MODULES_L       =

PY_PACKAGES        = pyCounterTest
PY_PACKAGES_L      =
pppppp_MODULES	   =

#
# TCL libraries (public and local)
# ------------------------------
TCL_LIBRARIES   =
TCL_LIBRARIES_L =

#
# <brief description of tttlll library>
tttlll_OBJECTS  = 

#
# UIF panels (public and local)
# ----------------------------
PANELS   =
PANELS_L = 

# 
# IDL Files and flags
# 
IDL_FILES = contNcTest_IF
IDL_TAO_FLAGS =
USER_IDL =

contNcTest_IFStubs_LIBS = acscomponentStubs ACSErrTypeCommonStubs

#
# Jarfiles: We must distinguish between component implementation classes and other classes.
#
COMPONENTS_JARFILES = counterSupplierComp counterConsumerComp

counterSupplierComp_DIRS = alma/COUNTER/CounterSupplierImpl
counterSupplierComp_JLIBS = contNcTest_IF

counterConsumerComp_DIRS = alma/COUNTER/CounterConsumerImpl
counterConsumerComp_JLIBS = contNcTest_IF


#
# java sources in Jarfile on/off
#
# 'DEBUG=on' will trigger debugging information for Java
# and generation of documentation for generated code.
#
DEBUG=on

#
# ACS XmlIdl generation on/off
#
XML_IDL= 
#
# Java Component Helper Classes generation on/off
#
COMPONENT_HELPERS=on

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
# INS_ROOT files to be installed
#-------------------------------
INS_ROOT_FILES =
INS_ROOT_DIR   =

#
# other files to be installed
#----------------------------
INSTALL_FILES =
