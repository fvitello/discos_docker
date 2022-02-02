#*******************************************************************************
# E.S.O. - ALMA project
#
# "@(#) $Id: Makefile,v 1.33 2009/06/26 13:47:09 eallaert Exp $" 
#
# Makefile of the document utilites
#
# who       when      what
# --------  --------  ---------------------------------------------------
# pforstma  09/09/94  Created from doc, docArchive and docReview Makfiles
# gfilippi  04/11/94  upgrade according to vltMake 1.3
# gfilippi  12/07/95  docPcode added
# gfilippi  13/07/95  use wishx for docPcode 
# gfilippi  21/03/97  use tcl for docPcode 
# fcarbogn  25/07/97  porting FM4 to FM5 
# mverola   21/07/98  added docManExtract and docModManBuild
# mverola   24/08/98  docUnix2Doc changed from script to tcl-script
# psivera   02/02/01  added docDeroff (for MANPAGES to TXT conversion)
# eallaert 2009-06-26 added docPrepareComments
#---------------------------------------------------------------------------

#*******************************************************************************
# ALMA - Atacama Large Millimeter Array
# Copyright (c) ESO - European Southern Observatory, 2014
# (in the framework of the ALMA collaboration).
# All rights reserved.
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
#*******************************************************************************

#*******************************************************************************
# This Makefile follows ALMA/ACS Standards (see Makefile manpage documentation).
#*******************************************************************************
# REMARKS
#    none
#------------------------------------------------------------------------

#
# MODULE CODE DESCRIPTION:
# ------------------------
-include rules.mk

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
all:    do_all
	@echo " . . . 'all' done"

clean : clean_all
	@echo " . . . clean done"

clean_dist : clean_all clean_dist_all
	@echo " . . . clean_dist done"

man   : do_man
	@echo " . . . man page(s) done"

install : install_all
	-if [ -f $(PRJTOP)/paraCatalog.mif ];\
             then $(RM) $(PRJTOP)/paraCatalog.mif;\
         fi;\
         cp paraCatalog.mif $(PRJTOP)
	@echo " . . . installation done"
