#*******************************************************************************
# E.S.O. - ACS project
#
# "@(#) $Id: Makefile,v 1.8 2012/01/21 22:48:11 tstaig Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# dave  07/01/04  created
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
all:	do_all
	@echo " . . . 'all' done" 

clean : clean_all
	$(RM) *~ ../*~ Acssim/*~ Acssim/Corba/*~ Acssim/Servants/*~
	$(RM) *.pyc Acssim/*.pyc Acssim/Corba/*.pyc Acssim/Servants/*.pyc
	$(RM) ../lib/*
	$(RM) ../doc/*.html ../doc/pychecker
	@echo " . . . clean done"

clean_dist : clean clean_dist_all 
	$(RM) ../bin ../doc ../include ../lib ../object
	@echo " . . . clean_dist done"

man   : do_man
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"

#___oOo___
