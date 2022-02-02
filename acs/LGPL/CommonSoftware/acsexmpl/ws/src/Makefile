#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
#
# "@(#) $Id: Makefile,v 1.141 2012/01/21 22:48:11 tstaig Exp $" 
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# almamgr 2003-09-02 Added TAO_CosProperty library needed on Sun to link
# dfugate  2003-08-26 Removed old dbl classes.
# dfugate  2003-08-26 Removed acsexmplDoor.idl (it's in building now)
# gchiozzi 2003-05-25 Renamed in acsabeans.sh removing .sh
# rgeorgie 2002-12-05 Commented out CCS libraries
# gchiozzi 2002-10-21 Added CDB_SCHEMAS
# david 2002-08-08 removed acsexmplDoorDefines.h from INCLUDES
# david 2002-08-06 removed "-rf" from the clean abeans entry as it is not necessary
# david 2002-07-30 made doxygenize a local script so it doesn't install into ACSROOT
# gchiozzi 2002-07-18 Added first simple target to generate abeans.
# msekoran 2002-07-12 added Calendar example
# david 2002-06-20 changed to remove doxygen documentation
# david 2002-06-14 Changed gman target to man
# blopez   2002-04-04 Files for example device "Building" added
# blopez   2002-04-04 Library for ACSDO usage added
# blopez   2002-03-25 Files for example device "Door" added
# gchiozzi 2002-03-08 Added GetAsync test client
# gchiozzi 2002-02-13 Refactored example clients
# almamgr 2002-01-22 Cleaned up USER_IDL. Takes default from acsMakefile.
# almamgr 2002-01-22 Replaced old libraries with new axsexmpl... files
# gchiozzi 2002-01-21 Replaced PowerSupplyESO with PowerSupplyACS
# gchiozzi 2002-01-17 Removed all -I directives for ACE/TAO include files. Are handled by acsMakefile
# almamgr 2000-07-20 created
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
	$(RM) ../doc/html
	$(RM) tmp.txt acsexmplbeans.jar
	@echo " . . . clean done"

clean_dist : clean clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	cp ../doc/html/group__ACSEXMPLDOC.html ../doc/html/main.html
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"

checkCdbSchemas:
	cdbChecker -v
