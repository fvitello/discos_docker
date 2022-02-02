#*******************************************************************************
# E.S.O. - VLT project
#
# "@(#) $Id: Makefile,v 1.133 2012/03/20 14:20:41 bjeram Exp $" 
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# almamgr 2003-08-29 Removed dbl handling
# rcirami  2003-08-28 Added maciACSComponentDefines.h and maciPropertyDefines.h
# msekoran 2003-05-22 Added maciORBTask
# gchiozzi 2003-04-02 Removed nslist and nsadd. Use standard TAO ones.
# jibsen   2002-03-14 maciActivatorImpl.h made public
# gchiozzi 2001-09-20 Temoprarily added nsadd and nslist. To be removed later
# gchiozzi 2001-07-17 Removed IDL generated files from INCLUDE. Redundant.
# msekoran 2000-08-19 created
# msekoran 2001-02-19 file naming changed (adding module name prefix)
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
# CHOSE PLATFORM
# --------------
# Default is UNIX, for VxVorks application next line MUST BE UNCOMMENTED
#MAKE_VXWORKS = on   

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
all:	do_all
	@echo " . . . 'all' done" 

clean : clean_all 
	$(RM) *~ ../include/*~ ../idl/*~ ../*~ ../../*~ core
	@echo " . . . clean done"

clean_dist : clean clean_dist_all 
	@echo " . . . clean done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"
