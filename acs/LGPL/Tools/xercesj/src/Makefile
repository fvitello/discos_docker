#*******************************************************************************
#
# "@(#) $Id: Makefile,v 1.9 2010/06/04 14:02:31 mzampare Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# hsommer  18/02/05  created
#

#*******************************************************************************
# REMARKS
#    None
#------------------------------------------------------------------------

#
# The built jar file needs to be installed
# As well as a couple of DTDs uses by the cdbChecker.
#----------------------------
INSTALL_FILES = ../lib/endorsed/xercesImpl.jar \
		../lib/xercesImpl.jar \
		../lib/xml-apis-1.4.01.jar \
                ../config/CDB/schemas/datatypes.dtd \
                ../config/CDB/schemas/XMLSchema.dtd 

#
# Configuration Database Schema Files
# ----------------------
#
# We keep here a local copy of the original xml.xsd
# from the W3 org to be used by the CDB.
# In this way we do not need to access the internat when we need to
# access it.
# xerces is the most basic module that handles schema files and
# therefore we have decided to put it here.
#
CDB_SCHEMAS = xml XMLSchema

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
	mvn dependency:get -Dartifact=xerces:xercesImpl:2.12.0:jar:sources -Ddest=main/java/xercesImpl-sources.jar -Dtransitive=false
	cd main/java; jar xf xercesImpl-sources.jar; rm xercesImpl-sources.jar; find . -name '*.java' |xargs dos2unix &> /dev/null
	mvn -f .. package; rm -rf main
	mkdir -p ../lib/endorsed; mv ../lib/xercesImpl-acs.jar ../lib/xercesImpl.jar; cp ../lib/xercesImpl.jar ../lib/endorsed/xercesImpl.jar
	@echo " . . . 'all' done"

clean : clean_all
	mvn -f .. clean
	rm -rf ../lib
	@echo " . . . clean done"

clean_dist : clean clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"


#___oOo___
