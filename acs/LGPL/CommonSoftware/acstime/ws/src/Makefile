#*******************************************************************************
# NRAO - ACS project
#
# "@(#) $Id: Makefile,v 1.30 2012/01/21 22:48:11 tstaig Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# gchiozzi 2003-09-01 Removed test target
# david  2003-07-04  created
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
all:	acstime_swig do_all
	@echo " . . . 'all' done" 

clean : clean_all 
	$(RM) *~ ../idl/*~ ../include/*~ ../test/*~
	$(RM) ../config/CDB/schemas/*~
	$(RM) .purify
	@echo " . . . clean done"

clean_dist : clean_all clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@$(RM) $(INSTALL_ROOT)/lib/python/site-packages/_acstimeSWIG.$(SHLIB_EXT)
	@cp ../lib/lib_acstimeSWIG.$(SHLIB_EXT) $(INSTALL_ROOT)/lib/python/site-packages/_acstimeSWIG.$(SHLIB_EXT)
	@echo " . . . installation done"

acstime_swig:
	@swig -c++ -python ../config/acstimeSWIG.i
	@mv ../config/acstimeSWIG_wrap.cxx acstimeSWIG_wrap.cpp
	@mv ../config/acstimeSWIG.py .

#___oOo___
