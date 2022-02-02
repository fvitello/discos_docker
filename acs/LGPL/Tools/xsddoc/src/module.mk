SHELL=/bin/bash
PLATFORM := $(shell uname)
XSDDOC_VER = 1.0

JARFILES = xsddoc
xsddoc_DIRS = net
xsddoc_EXTRAS = \
                net/sf/xframe/xsddoc/xslt/component.xsl\
                net/sf/xframe/xsddoc/xslt/help-doc.xsl\
                net/sf/xframe/xsddoc/xslt/index-all.xsl\
                net/sf/xframe/xsddoc/xslt/index.xsl\
                net/sf/xframe/xsddoc/xslt/model.xsl\
                net/sf/xframe/xsddoc/xslt/overview-all.xsl\
                net/sf/xframe/xsddoc/xslt/overview-namespaces.xsl\
                net/sf/xframe/xsddoc/xslt/overview-namespace.xsl\
                net/sf/xframe/xsddoc/xslt/schema-index.xsl\
                net/sf/xframe/xsddoc/xslt/schema-summary.xsl\
                net/sf/xframe/xsddoc/xslt/util.xsl\
                net/sf/xframe/xsddoc/xslt/xml2html.xsl\
                net/sf/xframe/xsddoc/xslt/xmldoc.xsl\
                net/sf/xframe/xsddoc/css/stylesheet.css
xsddoc_JARS:=xalan

SCRIPTS = xsddoc

$(MODDEP)_PREQS:=$(MODPATH)/src/net/sf/xframe/xsddoc/Task.java

$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)$(if $(wildcard $(MODPATH)/src/net),rm -rf $(MODPATH)/src/net,)
	$(AT)$(if $(wildcard $(MODPATH)/src/LICENSE.txt),rm $(MODPATH)/src/LICENSE.txt,)
	$(AT)echo " . . . $@ done"

#
# GCH
# This extracts the needed sources from the original
# tar file and moves them in the right place in the tree.
# We will then use a standard ACS Makefile to build.
# I prefer this to extract everything and use the internal
# ant spect because the original tar file contains also 
# the pre-build jar and many other files that are not needed.
# 

# the net outcome of this is a net directory in the CWD
# and an up to date last_unpacked file
#
$(MODPATH)/src/net $(MODPATH)/src/LICENSE.txt: $(MODRULE)unpack

.INTERMEDIATE: $(MODRULE)unpack
$(MODRULE)unpack: $(MODPATH)/src/xsddoc-$(XSDDOC_VER).tar.gz
	$(AT)echo "== Extracting sources from the tar file"
	$(AT)gtar -C $(MODPATH)/src -xzf $(MODPATH)/src/xsddoc-$(XSDDOC_VER).tar.gz xsddoc-$(XSDDOC_VER)/src/net xsddoc-$(XSDDOC_VER)/LICENSE.txt
	$(AT)mv $(MODPATH)/src/xsddoc-$(XSDDOC_VER)/src/net $(MODPATH)/src/xsddoc-$(XSDDOC_VER)/LICENSE.txt $(MODPATH)/src/
	$(AT)rm -rf $(MODPATH)/src/xsddoc-$(XSDDOC_VER)
	$(AT)touch $(MODPATH)/src/net $(MODPATH)/src/LICENSE.txt

#
# This target prepares the patch file
# after new patches have been applied/coded.
# It assumes that the new/patched files are in
# in 
#     xsddoc
# and unpacks the unpatched code to makethe diff
#     tmp_unpack/xsddoc.orig
#
# Does not use xsddoc as directory name but adds .orig
# to make clearer reading the patch file.
# Before preparing the patch also cleans up the code with the patches
# Makes a copy of the previous patch file for comparison
# and deleted the unpatched code afterwards.
# 
# I had to put a 'true' because patch returns -1. No idea why.  
#
$(MODRULE)preparePatch:
	@if [ -e xsddoc.patch ]; then \
           mv xsddoc.patch xsddoc.patch.old ;\
         fi
	@rm -rf tmp_unpack; mkdir -p tmp_unpack
	@cd tmp_unpack; \
           gtar -xzf ../xsddoc-$(XSDDOC_VER).tar.gz xsddoc-$(XSDDOC_VER)/src/net; \
           mv xsddoc-$(XSDDOC_VER)/src/net . ;\
           rm -rf xsddoc-$(XSDDOC_VER)
	@LC_ALL=C TZ=UTC0 diff -Naur tmp_unpack/net net  >xsddoc.patch; true
	@rm -rf tmp_unpack
	@echo " . . . patch file prepared"

# Apply the patch
$(MODPATH)/src/net/sf/xframe/xsddoc/Task.java: $(MODPATH)/src/xsddoc.patch | $(MODPATH)/src/net
	$(AT)patch -d $(MODPATH)/src -p0 < $(MODPATH)/src/xsddoc.patch
	$(AT)echo " . . . patch applied";\
