#*******************************************************************************
# E.S.O. - ACS project
#
# "@(#) $Id: Makefile,v 1.13 2003/10/28 10:39:44 mschilli Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# mschilli 2004-05-05 changed jManagerConfig.jar to JavaOnlyAcsConfig.jar
# mschilli 2004-04-08 added new tool configuration files
# mschilli 2003-10-28 moved java portion of acsstartup module to this module
# mschilli 2003-10-27 added 5 install_files
# mschilli 2003-10-23 added acsSetBaseAndRun script
# dave 2003-09-04 added startACS script
# dave  2003-09-04  created
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
	$(RM) *~
	@echo " . . . clean done"

clean_dist : clean_all clean_dist_all
	@echo " . . . clean_dist done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"




#___oOo___
