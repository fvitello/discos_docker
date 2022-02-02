#*******************************************************************************
# E.S.O. - ACS project
#
# "@(#) $Id: Makefile,v 1.11 2006/07/14 09:18:19 gchiozzi Exp $"
#
#*******************************************************************************

### STEPS to build a distribution:
#
#   See README file in this directory
#
#   Use the build_distribution target if you want also to build ACS from CVS at the same time

#
# This is the directory parent of the directories of the distributions.
# i.e. the directory where this same Makefile resides.
# The value is used in the Makefile.conf to set other variables
# and allows to lauch Makefile targets from here or directly
# from inside the directories of the distributions.
# In that case the value is not set and ../ is used for the evaluation
#
# Notice that setting here an absolute path will generate absolute paths
# in the links in the installation sub-directories.
#
# Originally, this variable was exported, but this was causing problems
# in the recursive Makefiles.
# We now set the variable locally and let the recursive Makefiles to
# calcualte their own as ../
#
PARENT_DEST=.


#
# Include basic definitions and variables to be configured.
#
include Makefile.conf

# Put an if statement: according to the OS and type (LGPL or not)
# include the right Makefile

All = all
$(All): docs distribution 

link_makefile:
	@mkdir -p $(DEST)
	@if [ ! -h $(DEST)/Makefile ]; then \
		ln -s ../Makefile.$(PLATFORM)$(TYPE) $(DEST)/Makefile; \
        fi

distribution: link_makefile
	@echo "Preparing ACS distributions"
	@$(MAKE) -C $(DEST) all
	@echo "... done!"

#
# Notice that on SUN we do not build ARCHIVE
#
build_distribution: link_makefile
	@echo "Building and preparing ACS distributions"
	@$(MAKE) -C $(DEST) build all
	@echo "... done!"

docs:
	@echo "... Creating Documentation image"
	cd ..; gtar -chzf Distribution/ACS_Docs.tar.gz Docs


delete_old:
	@echo "Deleting all installation. Use when limited space available"
	@rm -rf $(ACSBUILDDIR)/ACS $(ACSBUILDDIR)/ARCHIVE
	@rm -rf $(SOURCE)$(PREF)/ACSSW $(SOURCE)$(PREF)/acsdata
	@echo "... done!"

Clean = clean
$(Clean): link_makefile
	@echo "Cleaning distributions"
	@$(MAKE) -C $(DEST)  $@
	@echo "... done!"

# __oOo__
