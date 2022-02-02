#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) European Southern Observatory, 2002
# Copyright by ESO (in the framework of the ALMA collaboration)
# and Cosylab 2002, All rights reserved
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
# "@(#) $Id: Makefile,v 1.90 2012/01/20 22:07:43 tstaig Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# gchiozzi 2002-12-12 Removed reference to fnd and CCD libraries
# msekoran 2002-02-26 Added ACSErrTypeCORBA IDLs.
# gchiozzi 2002-02-26 Changed order in IDL_FILES
# msekoran 2002-02-12 Added ACSErrTypeMonitor, ACSErrTypeAlarm IDLs.
# gchiozzi 2002-01-18 Added clean_dist target
# almamgr  20/06/01  created
#

#*******************************************************************************
# This Makefile follows ACS Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#-------------------------------------------------------------------------------

-include rules.mk

INCLUDES+=acserrHandlersErr.h ErrorSystemErrType.h
acserr_OBJECTS += acserrHandlersErr ErrorSystemErrType
IDL_FILES+=acserrHandlersErr ErrorSystemErrType
ErrorSystemErrTypeStubs_LIBS = acserrStubs
acserrHandlersErrStubs_LIBS = acserrStubs


#ACSERRDEF = ErrorSystemErrType
#
#>>>>> END OF standard rules

# Here we have to generate the code for errors by hand because the acserr library is not available yet
# and because we want to make err system more compact (w/o too much err type libraries)
ERRCODE_GEN = acserrHandlersErr ErrorSystemErrType

#
# CHOSE PLATFORM
# --------------
# Default is UNIX, for VxVorks application next line MUST BE UNCOMMENTED
#MAKE_VXWORKS = on   

#
# INCLUDE STANDARDS
# -----------------

ERRCODE_GEN_XML = $(foreach xml, $(ERRCODE_GEN), ../idl/$(xml).xml)
ERRCODE_GEN_IDL = $(foreach xml, $(ERRCODE_GEN), ../idl/$(xml).idl)
ERRCODE_GEN_IDL_FULL_PATH = $(foreach xml, $(ERRCODE_GEN), $(CURDIR)/../idl/$(xml).idl)
ERRCODE_GEN_H = $(foreach xml, $(ERRCODE_GEN), ../include/$(xml).h)
ERRCODE_GEN_CPP = $(foreach xml, $(ERRCODE_GEN), ../src/$(xml).cpp) $(ERRCODE_GEN_H)

MAKEDIRTMP := $(shell searchFile include/acsMakefile)
ifneq ($(MAKEDIRTMP),\#error\#)
   MAKEDIR := $(MAKEDIRTMP)/include
   include $(MAKEDIR)/acsMakefile
endif

#
# TARGETS
# -------
all:	do_errcode_gen do_all
	@echo " . . . 'all' done" 

clean : clean_all clean_errcode_gen
	$(RM) *~ ../doc/*.pcode ../include/*~ ../dbl/*~ ../*~ ../../*~ core
	@echo " . . . clean done"

clean_dist : clean clean_dist_all 
	@echo " . . . clean done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"


$(ERRCODE_GEN_IDL) : $(ERRCODE_GEN_XML)
	$(AT) for member in $(ERRCODE_GEN); do \
	echo "== Generating:" $${member}".idl out of:" $${member}".xml"; \
		sh ./acserrGenIDL  ../idl/$${member}.xml ../idl/$${member}.idl ; \
		done

$(ERRCODE_GEN_IDL_FULL_PATH) : $(ERRCODE_GEN_XML)
	$(AT) for member in $(ERRCODE_GEN); do \
	echo "== Generating:" $${member}".idl out of:" $${member}".xml"; \
		sh ./acserrGenIDL  ../idl/$${member}.xml ../idl/$${member}.idl ; \
		done

$(ERRCODE_GEN_CPP) : $(ERRCODE_GEN_XML)
	$(AT) for member in $(ERRCODE_GEN); do \
		echo "== Generating:" $${member}".cpp and:" $${member}".h out of:" $${member}".xml"; \
		sh ./acserrGenCpp ../idl/$${member}.xml ../src/$${member}.cpp ../include/$${member}.h ; \
		done

do_errcode_gen :  $(ERRCODE_GEN_IDL) $(ERRCODE_GEN_CPP) 

clean_errcode_gen:
	$(AT) $(RM) $(ERRCODE_GEN_IDL) $(ERRCODE_GEN_CPP)
