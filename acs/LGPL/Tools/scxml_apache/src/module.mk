SCXML_VER:=0.9
SCXML_NAME:=commons-scxml-$(SCXML_VER)-src
SCXML_DIR:=$(MODPATH)/src/$(SCXML_NAME)
SCXML_LIBDIR:=$(SCXML_DIR)/target/lib
SCXML_TARBALL:=$(MODPATH)/src/$(SCXML_NAME).tar.gz
SCXML_JAR:=commons-scxml-$(SCXML_VER).jar

#
# other files to be installed
#----------------------------
INSTALL_FILES = ../lib/$(SCXML_JAR)

$(MODRULE)all: $(MODPATH) $(MODDEP) $(SCXML_DIR) $(MODPATH)/lib/$(SCXML_JAR)
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(SCXML_DIR)),rm -r $(SCXML_DIR),)
	$(AT)$(if $(wildcard $(MODPATH)/lib/$(SCXML_JAR)),rm $(MODPATH)/lib/$(SCXML_JAR),)
	$(AT)$(if $(wildcard $(MODPATH)/src/.purifydir),rm -r $(MODPATH)/src/.purifydir)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"

# We construct the classpath outside of ANT to avoid that the build.xml script will try to download the required jar files from the web.
# See "build.sysclasspath=only" below. An alternative would be to patch the build.xml file.
REMOTE_JARS_CLASSPATH:=$(call findDep,commons-logging-1.2.jar,jar,lib,1):$(call findDep,xalan.jar,jar,lib,1):$(call findDep,commons-digester-2.1.jar,jar,lib,1):$(call findDep,commons-beanutils-1.8.3.jar,jar,lib,1):$(call findDep,commons-jexl-1.1.jar,jar,lib,1)
# The following way of getting the classpath is more flexible, but requires a module refactoring 
# because acsGetSpecificJars depends on ACS/LGPL/CommonSoftware/acsutilpy/src/AcsutilPy/FindFile.py
# On 2013-01-16 Ale and Heiko decided to tackle this after the SVN migration.
# REMOTE_JARS_CLASSPATH = $(shell acsGetSpecificJars : commons-logging-1.1.1.jar:xalan.jar:commons-digester-2.1.jar:commons-beanutils-1.8.3.jar:commons-jexl-1.1.jar)


# For the local path we need a few jars that do not need to be installed though. 
# Uncomment the next 3 lines to include all of these jars, whereas now we just list them explicitly.
#empty :=
#space := $(empty) $(empty)
#LOCAL_JARS_CLASSPATH = $(subst $(space),:,$(abspath $(wildcard ../lib/*.jar)))
LOCAL_JARS_CLASSPATH:=$(abspath $(MODPATH)/lib/commons-el-1.0.jar):$(abspath $(MODPATH)/lib/jsp-api-2.0.jar):$(abspath $(MODPATH)/lib/myfaces-api-1.1.5.jar):$(abspath $(MODPATH)/lib/servlet-api-2.4.jar)

$(SCXML_DIR): $(SCXML_TARBALL)
	$(AT)echo " . . . unpack original distribution"
	$(AT)$(if $(wildcard $(SCXML_DIR)),rm -r $(SCXML_DIR),)
	$(AT)gtar -C $(MODPATH)/src -xzf $(SCXML_TARBALL)

# build the jar file.
$(MODPATH)/lib/$(SCXML_JAR): $(SCXML_DIR)
	$(AT)echo ". . . building apache scxml . . . "
	$(AT)cd $(SCXML_DIR); CLASSPATH=$(LOCAL_JARS_CLASSPATH):$(REMOTE_JARS_CLASSPATH); ant -Dbuild.sysclasspath=only jar
	$(AT)cp -r --preserve=timestamps $(SCXML_DIR)/src/main/java $(SCXML_DIR)/target/src
	$(AT)jar uf $(SCXML_DIR)/target/$(SCXML_JAR) -C $(SCXML_DIR)/target src
	$(AT)rm -r $(SCXML_DIR)/target/src
	$(AT)cp $(SCXML_DIR)/target/$(SCXML_JAR) $(MODPATH)/lib
