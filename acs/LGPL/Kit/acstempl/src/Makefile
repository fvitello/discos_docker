#*******************************************************************************
# E.S.O. - VLT project
#
# "@(#) $Id: Makefile,v 1.21 2004/09/16 13:17:42 mpasquat Exp $" 
#
# Makefile of VLT templates
#
# who        when      what
# --------  ----------  ----------------------------------------------
# psivera   2000-10-16  created

#*******************************************************************************
# This Makefile follows ACS Standards (see Makefile manpage documentation).
#*******************************************************************************
# REMARKS
#     None  
#------------------------------------------------------------------------

#
# MODULE CODE DESCRIPTION:
# ------------------------
-include rules.mk

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
	$(AT) $(RM) ../*~ ../*/*~
	@echo " . . . clean done"

man   : do_man 
	-cd ../forCoding;    make man
	@echo " . . . man page(s) done"

install : install_all templates doxygen_tmpl

templates:
	-cd ../forCoding;    make install
	@echo " . . . installation done"

doxygen_tmpl:
	-cd ../forDoxygen;    make install
	@echo " . . . installation done"

#___oOo___
