XERCESJ_VER = 2.9.1
XERCESJ_DIR = xerces-2_9_1

INSTALL_FILES = ../config/CDB/schemas/datatypes.dtd ../config/CDB/schemas/XMLSchema.dtd 
INSTALL_JARS:= endorsed/xercesImpl.jar

CDB_SCHEMAS = xml XMLSchema

$(MODRULE)all: $(MODPATH) $(MODDEP) $(MODPATH)/lib/endorsed/xercesImpl.jar
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(if $(wildcard $(MODPATH)/lib/endorsed/xercesImpl.jar),rm -f $(MODPATH)/lib/endorsed/xercesImpl.jar,)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(if $(wildcard $(MODPATH)/src/$(XERCESJ_DIR)),rm -rf $(MODPATH)/src/$(XERCESJ_DIR),)
	$(AT)echo " . . . $@ done"

#
# This target prepares the patch file
# after new patches have been applied/coded.
# It assumes that the new/patched files are in
# in 
#     $(XERCESJ_DIR)
# and unpacks the unpatched code to makethe diff
#     tmp_unpack/$(XERCESJ_DIR).orig
#
# Does not use $(XERCESJ_DIR) as directory name but adds .orig
# to make clearer reading the patch file.
# Before preparing the patch also cleans up the code with the patches
# Makes a copy of the previous patch file for comparison
# and deleted the unpatched code afterwards.
# 
# I had to put a 'true' because patch returns -1. No idea why.  
#
.NOTPARALLEL: $(MODRULE)preparePatch
$(MODRULE)preparePatch:
	$(AT)mv xml-xerces.patch xml-xerces.patch.old
	$(AT)rm -rf tmp_unpack; mkdir -p tmp_unpack
	$(AT)cd tmp_unpack; gtar -xzf ../Xerces-J-src.$(XERCESJ_VER).tar.gz; mv $(XERCESJ_DIR) $(XERCESJ_DIR).orig
	$(AT)cd $(XERCESJ_DIR)/java; ./build.sh clean
	$(AT)LC_ALL=C TZ=UTC0 diff -Naur tmp_unpack/$(XERCESJ_DIR).orig $(XERCESJ_DIR)  >xml-xerces.patch; true
	$(AT)rm -rf tmp_unpack
	$(AT)echo " . . . patch file prepared"


#
# Unpack the tar file with the original distribution
#
$(MODPATH)/src/$(XERCESJ_DIR): $(MODPATH)/src/Xerces-J-src.$(XERCESJ_VER).tar.gz
	$(AT)echo " . . . unpacking original distribution Xerces-J-src.$(XERCESJ_VER).tar.gz"
	$(AT)gtar -C $(MODPATH)/src -xzf $(MODPATH)/src/Xerces-J-src.$(XERCESJ_VER).tar.gz
	$(AT)touch $(MODPATH)/src/$(XERCESJ_DIR)

#
# Apply the xpointer patch.
# Delete Xerces-J java source files that should be excluded from the build (see ICT-1178).
# Copy selected external libraries from ACSROOT and the xerces tools to the location expected by the Xerces-J build.
#
# TODOs: 
# (a) See http://ictjira.alma.cl/browse/ICT-1174 about still hardcoding $ACSROOT instead of using acsGetSpecificJars.py
# (b) Find a way of calling xercesj's build.xml so that we skip the extraction of tools/xml-commons-external-src.zip.
#     Currently we inject the empty xml-commons-external-src-dummy.zip file because it seems easier than patching build.xml.
#
$(MODPATH)/src/$(XERCESJ_DIR)/src/org/apache/xerces/xpointer/XPointerHandler.java $(MODPATH)/src/$(XERCESJ_DIR)/tools/resolver.jar $(MODPATH)/src/$(XERCESJ_DIR)/tools/bin $(MODPATH)/src/$(XERCESJ_DIR)/tools/xml-commons-external-src.zip: $(MODRULE)patch

.INTERMEDIATE: $(MODRULE)patch
$(MODRULE)patch: $(MODPATH)/src/xml-xerces.patch $(call findDep,commons-xml-resolver-1.2.jar,jar,lib,1) | $(MODPATH)/src/$(XERCESJ_DIR)
	$(AT)patch -d $(MODPATH)/src/$(XERCESJ_DIR) -p1 < $(MODPATH)/src/xml-xerces.patch
	$(AT)echo " . . . patch applied";\
	$(AT)rm -rf $(MODPATH)/src/$(XERCESJ_DIR)/src/org/apache/html $(MODPATH)/src/$(XERCESJ_DIR)/src/org/w3c/dom/html
	$(AT)echo " . . . removed HTML DOM support";\
	$(AT)mkdir -p $(MODPATH)/src/$(XERCESJ_DIR)/tools/bin
	$(AT)cp $(call findDep,commons-xml-resolver-1.2.jar,jar,lib,1) $(MODPATH)/src/$(XERCESJ_DIR)/tools/resolver.jar
	$(AT)cp $(MODPATH)/src/xjavac.jar $(MODPATH)/src/$(XERCESJ_DIR)/tools/bin
	$(AT)cp $(MODPATH)/src/xml-commons-external-src-dummy.zip $(MODPATH)/src/$(XERCESJ_DIR)/tools/xml-commons-external-src.zip
	$(AT)echo " . . . Fixed external dependencies";\

# Build the distribution using the standard procedure.
# Copy xercesImpl.jar to ../lib/endorsed, from where it can be installed.
#
$(MODPATH)/lib/endorsed/xercesImpl.jar: $(MODPATH)/src/$(XERCESJ_DIR)/src/org/apache/xerces/xpointer/XPointerHandler.java $(MODPATH)/src/$(XERCESJ_DIR)/tools/resolver.jar $(MODPATH)/src/$(XERCESJ_DIR)/tools/bin $(MODPATH)/src/$(XERCESJ_DIR)/tools/xml-commons-external-src.zip | $(MODPATH)/lib/endorsed
	$(AT)cd $(MODPATH)/src/$(XERCESJ_DIR); ant jar
	$(AT)cp $(MODPATH)/src/$(XERCESJ_DIR)/build/xercesImpl.jar $@
