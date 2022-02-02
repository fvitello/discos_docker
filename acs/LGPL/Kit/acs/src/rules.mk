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
# This Makefile follows ACS Standards (see Makefile manpage documentation).
#
# REMARK: this should be the first module to be installed.
#         For such a reason, in few cases, it needs to work from /src.
#*******************************************************************************

#
# MODULE CODE DESCRIPTION:
# ------------------------
#
# C programs (public and local)
# -----------------------------
EXECUTABLES:=
EXECUTABLES_L:=

#
# Includes (.h) files (public only)
# ---------------------------------
INCLUDES:= acsPort.h

#
# Scripts (public and local)
# --------------------------
SCRIPTS:= acsMakeTclScript acsMakeTclLib acsMan doxygenize acsChangeEnv \
          acsSwitchEnv instAlmaTarball JacPrep acsCheckGroupPermissions \
          cvs2cl acsUserConfig acsMakeCheckUnresolvedSymbols acsMakeJavaClasspath \
          acsMakeCopySources acsMakeLogInstallation acsMakeInstallFiles searchFile
SCRIPTS_L:=

#
# TCL scripts (public and local)
# ------------------------------
TCL_SCRIPTS:= acsReplace
acsReplace_OBJECTS:= acsReplace
acsReplace_TCLSH:= tcl
TCL_SCRIPTS_L:=

#
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS:= acsConfigReport acsGetAllJars acsGetSpecificJars acsSearchPath convertTree
PY_SCRIPTS_L:=

PY_MODULES:= acsSearchPath
PY_MODULES_L:=

PY_PACKAGES:= acsConfigReportModule acsKit
PY_PACKAGES_L:=
pppppp_MODULES:=

#
# man pages to be done
# --------------------
MANSECTIONS:= 1 5 n
MAN1:= acsMan acsConfigReport.py
MAN5:= ../include/acsMakefile Makefile.doc
MANn:= acsReplace.tcl

#
# local man pages
# ---------------
MANl:= $(SCRIPTS)

#
# other files to be installed
# ---------------------------
INSTALL_FILES:= ../include/acsMakefile ../include/InclusiveMakefile.mk \
      ../include/acsMakefileDefinitions.mk ../include/acsMakefileCore.mk \
      ../include/acsMakefileVxWorks.mk ../include/acsMakefileConfig.mk \
      ../include/acsMakefileProfile.Default.mk  ../include/acsMakefileProfile.Unix.mk \
      ../include/acsMakefileProfile.Linux.mk ../include/acsMakefileProfile.Cygwin.mk \
      $(addsuffix .mk,$(addprefix ../include/acsMakefileProfile.Fedora,29 30)) \
      $(addsuffix .mk,$(addprefix ../include/acsMakefileProfile.RedHatEnterpriseServer,6 7 8)) \
      $(addsuffix .mk,$(addprefix ../include/acsMakefileProfile.CentOS,6 7 8)) \
      ../include/Makefile_LCU.template ../include/Makefile_WS.template \
      ../include/Makefile_PACKAGE.template $(wildcard ../config/acsPackageInfo*.rpmref) \
      ../config/XSDIncludeDependencies.xml
