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

# Name of the jarfile which contains the generators' files
TMCDB_GRAMMAR=tmcdbGrammar
SM_GENERATOR=smGenerator

# Helper variables for extra content in jarfiles
SMGENERATOR_EXTRAFILES:=$(wildcard $(MODPATH)/src/alma/acs/sm/profiles/*.profile.uml)
SMGENERATOR_EXTRAFILES+=$(wildcard $(MODPATH)/src/alma/acs/sm/templates/*.xpt)
SMGENERATOR_EXTRAFILES+=$(wildcard $(MODPATH)/src/alma/acs/sm/templates/*.ext)
SMGENERATOR_EXTRAFILES+=$(wildcard $(MODPATH)/src/alma/acs/sm/templates/Java/*.xpt)
SMGENERATOR_EXTRAFILES+=$(wildcard $(MODPATH)/src/alma/acs/sm/workflows/*.mwe)
SMGENERATOR_EXTRAFILES+=$(wildcard $(MODPATH)/src/alma/acs/sm/*.chk)

#
# other files to be installed
#----------------------------
INSTALL_JARS=$(SM_GENERATOR).jar

$(MODRULE)all: $(MODPATH) $(MODDEP) $(MODPATH)/object/.done_generating $(MODPATH)/object/.done_src-merged $(MODPATH)/lib/$(SM_GENERATOR).jar

$(MODPATH)/object/.done_src-merged: $(MODPATH)/object/.done_generating | $(MODPATH)/object
	$(AT)$(MAKE) -C $(MODPATH)/src-merged
	$(AT)touch $(MODPATH)/object/.done_src-merged

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)$(MAKE) -C $(MODPATH)/src-merged install
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo "== Deleting generated sources"
	$(AT)rm -f $(MODPATH)/lib/$(SM_GENERATOR).jar
	$(AT)rm -rf $(MODPATH)/src-gen
	$(AT)$(MAKE) -C $(MODPATH)/src-merged clean
	$(AT)rm -f $(MODPATH)/object/.done_generating
	$(AT)rm -f $(MODPATH)/object/.done_src-merged
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"

# Target for generating code from the grammar definition of the TMCDB and compile it.
$(MODPATH)/object/.done_generating : $(MODPATH)/src/alma/acs/tmcdb/grammardef/GenerateTmcdbTables.mwe $(MODPATH)/src/alma/acs/tmcdb/grammardef/TmcdbTables.xtext | $(MODPATH)/object
	$(AT)echo "== Generating classes from TMCDB grammar..."
	$(AT)cd $(MODPATH); CLASSPATH="$$CLASSPATH$(PATH_SEP)$(MODPATH)/src$(PATH_SEP)$(MODPATH)/src-gen" acsStartJava org.eclipse.emf.mwe.core.WorkflowRunner $(MODPATH)/src/alma/acs/tmcdb/grammardef/GenerateTmcdbTables.mwe; cd -
	$(AT)echo "== Merging generated and existing sources"
	$(AT)mkdir -p $(MODPATH)/src-merged/alma/acs/
	$(AT)cp -r $(MODPATH)/src-gen/alma/acs/tmcdb $(MODPATH)/src-merged/alma/acs/
	$(AT)cp -r $(MODPATH)/src/alma/acs/tmcdb/grammardef $(MODPATH)/src-merged/alma/acs/tmcdb/
	$(AT)touch $(MODPATH)/object/.done_generating

# the javaMakefile of ACS doesn't support the creation of jarfiles without classes.
# Therefore, we need to create them by hand.
$(MODPATH)/lib/$(TMCDB_GRAMMAR).jar:
	$(AT)echo "== Making $(TMCDB_GRAMMAR).jar"
	$(AT)$(MAKE) -C $(MODPATH)/src-merged

$(MODPATH)/lib/$(SM_GENERATOR).jar: $(SMGENERATOR_EXTRAFILES) $(MODPATH)/object/.done_src-merged
	$(AT)echo "== Making $(SM_GENERATOR).jar"
	$(AT)jar cf $@ $(addprefix -C $(MODPATH)/src ,$(subst $(MODPATH)/src/,,$(SMGENERATOR_EXTRAFILES)))

