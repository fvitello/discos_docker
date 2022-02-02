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
# 3rd party Install files
#
INSTALL_FILES:=../lib/jhall-2.0_05.jar

#
# Jarfiles and their directories
#
DEBUG:=on

JARFILES:=jACSUtil
jACSUtil_DIRS:=com alma
jACSUtil_EXTRAS:=alma/acs/jhelpgen/content/*.gif alma/acs/classloading/*.txt
jACSUtil_JARS:=junit-dep-4.10 commons-lang-2.5 hibernate-core-5.3.7.Final hibernate-jpa-2.1-api-1.0.2.Final

#
# Scripts (public and local)
# ----------------------------
SCRIPTS:=acsExtractJavaSources acsJarPackageInfo acsJarsearch acsJarSignInfo acsJarUnsign acsJavaHelp
