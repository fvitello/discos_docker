#*******************************************************************************
# E.S.O. - ACS project
#
# "@(#) $Id: Makefile,v 1.47 2013/02/14 17:12:20 acaproni Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# almamgr 2003-08-27 Removed test and dbl targets
# almamgr 2003-08-27 Replaced tar with gtar for sun
# dave 2003-08-25 GENERATEDS is no longer part of the "all" target
# dave 2003-08-14 added PyXML and generateDS for Python XML entity classes
# david  07/07/03  created
#

#
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         = pythfilter
PY_SCRIPTS_L       =

PY_MODULES         = acs_python
PY_MODULES_L       =

PY_PACKAGES        =
PY_PACKAGES_L      =
pppppp_MODULES	   =

PY_EXT_DOC_L	   = pexpect-doc

#
# list all python tarballs
# ------------------------------
PY_DOC :
	$(foreach file, $(PY_EXT_DOC_L), - $(AT) cd ../doc; if [ -e $(file).tgz ]; then echo "== Extracting external documentation: $(file).tgz"; gtar -zxvf $(file).tgz; fi )

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
all: do_all
	@echo " . . . 'all' done"

clean: clean_all
	$(RM) ../doc/doc ../doc/api ../doc/idl
	@echo " . . . clean done"

clean_dist : clean clean_dist_all
	@echo " . . . clean_dist done"

man   : do_man
	@echo " . . . man page(s) done"

install : install_all
	@echo " . . . installation done"

#___oOo___
