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
SCRIPTS         = hibernateCdbJDal MonitoringSyncTool xjc
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
# Configuration Database Files
# ----------------------------
CDB_SCHEMAS = ControlDevice

#
# Jarfiles and their directories
#
JARFILES=cdb_rdb AcsTmcdbUtils

cdb_rdb_DIRS=com alma/TMCDB alma/acs/tmcdb/logic
cdb_rdb_EXTRAS=alma/TMCDB/maci/hibernate-mappings-maci.hbm.xml \
               alma/TMCDB/baci/hibernate-mappings-baci.hbm.xml \
               acsOnly-cdb_rdb-hibernate.cfg.xml
cdb_rdb_JLIBS := TMCDBswconfigStrategy cdbrdb-pojos 

AcsTmcdbUtils_DIRS := alma/acs/tmcdb/generated/lrutype alma/acs/tmcdb/utils
AcsTmcdbUtils_JLIBS := cdb_rdb
AcsTmcdbUtils_JARS:=tmcdbGenerator

#
# java sources in Jarfile on/off
DEBUG=on

#
# other files to be installed
#----------------------------
POJOS_JAR = cdbrdb-pojos.jar
INSTALL_FILES = ../lib/$(POJOS_JAR) ../lib/TMCDBswconfigStrategy.jar ../lib/commons-cli-1.2.jar \
		../lib/jaxb-api-2.3.1.jar ../lib/jaxb-core-2.3.0.1.jar ../lib/jaxb-impl-2.3.2.jar \
		../lib/jaxb-xjc-2.3.2.jar ../lib/pfl-basic-4.0.1.jar

DDLDATA=$(ACSDATA)/config/DDL
