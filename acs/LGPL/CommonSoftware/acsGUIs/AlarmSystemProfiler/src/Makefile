#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) European Southern Observatory, 2009 
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
#
# "@(#) $Id: Makefile,v 1.1 2010/12/09 09:26:54 rtobar Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# acaproni  2009-10-11  created
#

#*******************************************************************************
# This Makefile follows ACS Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#-------------------------------------------------------------------------------
-include rules.mk

#
#>>>>> END OF standard rules

#
# INCLUDE STANDARDS
# -----------------

MAKEDIRTMP := $(shell searchFile include/acsMakefile)
ifneq ($(MAKEDIRTMP),\#error\#)
   MAKEDIR := $(MAKEDIRTMP)/include
   include $(MAKEDIR)/acsMakefile
endif

# Determine the host architecture and use it to decide which version of the
# product should we install
ARCH=$(shell getconf LONG_BIT)

# We only support 32 and 64 bits builds
ifneq ($(ARCH),32)
ifneq ($(ARCH),64)
	$(error Unsupported architecture: "$(ARCH)")
endif
endif

# Deduce the zipfile suffix
ifeq ($(ARCH), 32)
   ZIP_SUFFIX=x86
else
   ZIP_SUFFIX=x86_64
endif

#
# TARGETS
# -------
all:	do_all
	ant init createJarsPlugin pde-build
	@echo " . . . 'all' done" 

clean : clean_all 
	ant clean
	@echo " . . . clean done"

clean_dist : clean_all clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@INSTALL_DIR=$$(searchFile bin/alarmSystemProfiler); \
	 cd $$INSTALL_DIR/lib; \
	 rm -rf AlarmsProfiler; \
	 unzip -o $(PWD)/../object/headlessTemp/I.AlarmSystemProfiler/AlarmSystemProfiler-linux.gtk.$(ZIP_SUFFIX).zip;
	@echo " . . . installation done"
