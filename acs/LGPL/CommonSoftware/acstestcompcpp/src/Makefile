#*******************************************************************************
# ACS project Makefile
#
# "@(#) $Id: Makefile,v 1.6 2012/01/21 22:48:11 tstaig Exp $"
#
# $Log: Makefile,v $
# Revision 1.6  2012/01/21 22:48:11  tstaig
# Backport from branches ACS-9_0_0-windows-B and ACS-9_1_0-windows-B to support
# ACS on Windows under Cygwin. This commit corresponds to several CommonSoftware
# modules.
#
# Revision 1.5  2007/07/16 15:51:25  hsommer
# DEBUG=on to include .java in IDL-generated jar
#
# Revision 1.4  2006/10/18 15:52:30  gchiozzi
# Added schema file for ErrorExplorer.
#
# Revision 1.3  2006/10/09 11:00:40  bjeram
# repalce maciErrTypeComponent with maciErrType
#
# Revision 1.2  2006/09/28 16:01:45  gchiozzi
# Added new components and cleaned up.
#
# Revision 1.1  2006/09/14 14:54:34  gchiozzi
# First checkin of new module with CPP test components.
#
#
# Makefile of acstestcomp
#
# who       when      what
# --------  --------  ----------------------------------------------
# root      25/09/02  created
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

#
# TARGETS
# -------
all:	do_all doxygen
	@echo " . . . 'all' done"

clean:	clean_all
	@echo " . . . clean done"

clean_dist:	clean_all clean_dist_all
	@echo " . . . clean_dist done"

man:	do_man
	@echo " . . . man page(s) done"

install:	install_all
	@echo " . . . installation done"


#___oOo___
