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
# Scripts (public and local)
# ----------------------------
SCRIPTS         = acscommandcenter accStarter accStopper accEnableVars accStarter2
SCRIPTS_L       =

#
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         =
PY_SCRIPTS_L       =

PY_MODULES         =
PY_MODULES_L       =

PY_PACKAGES        =
PY_PACKAGES_L      =
pppppp_MODULES	   =


# 
# IDL Files and flags
# 
IDL_FILES =
IDL_TAO_FLAGS =
USER_IDL =

#
# XML schema files and includes
#
XSDBIND = AcsCommandCenterEntities
XSDBIND_INCLUDE = systementities

#
# ACS XmlIdl generation on/off
#
XML_IDL = "AcsCommandCenterProject=alma.entity.xmlbinding.acscommandcenterproject.AcsCommandCenterProject;AcsCommandCenterTools=alma.entity.xmlbinding.acscommandcentertools.AcsCommandCenterTools"

#
# Jarfiles and their directories
#
JARFILES= acscommandcenter
acscommandcenter_DIRS= alma
acscommandcenter_EXTRAS= alma/acs/commandcenter/resources/*.gif \
                         alma/acs/commandcenter/resources/*.jpg \
                         AcsCommandCenterTools.xml \
                         AcsCommandCenterBuiltinTools.xml \
                         VERSION
acscommandcenter_JLIBS=AcsCommandCenterEntities
                         
#
# java sources in Jarfile on/off
DEBUG= on


#
# other files to be installed
#----------------------------
INSTALL_FILES = ../lib/JavaOnlyAcsConfig.jar \
					 ../lib/trilead-ssh2-build213.jar \
					 ../lib/AcsCommandCenterHelp.jar \
					 ../lib/xtmodcoll-3.0.jar

#
# Java Component Helper Classes generation on/off
#
#COMPONENT_HELPERS=
