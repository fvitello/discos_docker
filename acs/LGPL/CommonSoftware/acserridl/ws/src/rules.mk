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

MAKE_NOIFR_CHECK:=on # todo check this

#
# user definable C-compilation flags
#USER_CFLAGS:=

#
# additional include and library search paths
#USER_INC:=
#USER_LIB:=

#
# MODULE CODE DESCRIPTION:
# ------------------------
# As a general rule:  public file are "cleaned" and "installed"  
#                     local (_L) are not "installed".

#
# C programs (public and local)
# -----------------------------
EXECUTABLES:=
EXECUTABLES_L:=

#
# <brief description of xxxxx program>
xxxxx_OBJECTS:=
xxxxx_LDFLAGS:=
xxxxx_LIBS:=

#
# special compilation flags for single c sources
#yyyyy_CFLAGS:=

#
# Includes (.h) files (public only)
# ---------------------------------
INCLUDES:=

#
# Libraries (public and local)
# ----------------------------
LIBRARIES:=
LIBRARIES_L:=

#
# <brief description of lllll library>
lllll_OBJECTS:=

#
# Scripts (public and local)
# ----------------------------
SCRIPTS:=
SCRIPTS_L:=

# 
# IDL Files and flags
# 
IDL_FILES:=acserr
TAO_IDLFLAGS:=
USER_IDL:=

#
# java sources in Jarfile on/off
DEBUG:=on

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
