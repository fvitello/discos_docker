#*******************************************************************************
# ALMA - Atacama Large Millimeter Array
# Copyright (c) Associated Universities Inc., 2020
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
# This Makefile follows ACS Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#-------------------------------------------------------------------------------
-include $(MODPATH)/src/rules.mk

ifdef NDDSHOME
$(MODRULE)all: $(MODPATH) $(MODDEP) do_dds_gen
	$(AT)echo " . . . $@ done"
	
$(MODRULE)clean: $(MODPATH) clean_$(MODDEP) dds_clean
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)cp -f $(MODPATH)/config/bulkDataNTDefaultQosProfiles.*.xml $(INSTDIR)/config
	$(AT)chmod $(P644) $(INSTDIR)/config/bulkDataNTDefaultQosProfiles.*.xml
	$(AT)echo " . . . $@ done"
else
$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)echo -e "\e[0;31mWARNING: NDDSHOME is not defined, and thus only code common to old and new BD will be build (ACSERR and IDL) for target all!!!!\e[0m" 
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo -e "\e[0;31mWARNING: NDDSHOME is not defined, and thus only code common to old and new BD will be installed (ACSERR and IDL) for target install!!!!\e[0m"
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo " . . . $@ done"
endif

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"

do_dds_gen: $(addprefix $(MODPATH)/src/,bulkDataNT.cpp bulkDataNTSupport.cpp bulkDataNTPlugin.cpp) $(addprefix $(MODPATH)/include/,bulkDataNT.h  bulkDataNTSupport.h bulkDataNTPlugin.h)
	
$(addprefix $(MODPATH)/src/,bulkDataNT.cpp bulkDataNTSupport.cpp bulkDataNTPlugin.cpp) $(addprefix $(MODPATH)/include/,bulkDataNT.h  bulkDataNTSupport.h bulkDataNTPlugin.h : ../idl/bulkDataNT.idl)
	$(NDDSHOME)/scripts/rtiddsgen -namespace -replace -d $(MODPATH)/src $(MODPATH)/idl/bulkDataNT.idl; \
	rename .cxx .cpp $(MODPATH)/src/*.cxx; \
	mv $(MODPATH)/src/*.h $(MODPATH)/include

dds_clean:
	$(AT)rm -f $(addprefix $(MODPATH)/src/,bulkDataNT.cpp bulkDataNTSupport.cpp bulkDataNTPlugin.cpp); \
	rm -f $(addprefix $(MODPATH)/include/,bulkDataNT.h bulkDataNTSupport.h bulkDataNTPlugin.h)
