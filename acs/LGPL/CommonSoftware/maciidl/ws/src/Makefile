#*******************************************************************************
# ALMA - Atacama Large Millimiter Array
# (c) European Southern Observatory, 2005 
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#
# "@(#) $Id: Makefile,v 1.11 2012/01/20 23:18:16 tstaig Exp $"
#
# Makefile of ........
#
# who       when      what
# --------  --------  ----------------------------------------------
# bjeram  2005-01-21  created
#

#*******************************************************************************
# This Makefile follows VLT Standards (see Makefile(5) for more).
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
all:	prepare_xsdbind do_all 
#all:	do_all
	@echo " . . . 'all' done" 

prepare_xsdbind:
	@echo " copying xml.xsd from ACSROOT, because we can't create binding classes up in ACS/LGPL/Tools/xercesj where this file comes from"
	@cp -a $(ACSROOT)/config/CDB/schemas/xml.xsd ../config/CDB/schemas/

#clean : clean_xsdbind clean_all
clean : clean_all clean_my_xsdbind
	@echo " . . . clean done"

clean_my_xsdbind:
	@echo " will remove schema files copied for XSDBIND workaround . . ."
	@rm -f ../config/CDB/schemas/xml.xsd

clean_dist : clean_all clean_dist_all 
	@echo " . . . clean_dist done"

man   : do_man 
	@echo " . . . man page(s) done"

install: install_all
	@echo " . . . installation done"


#___oOo___
