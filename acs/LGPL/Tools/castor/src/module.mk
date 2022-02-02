CLASSPATH := $(CLASSPATH):$(ANT_HOME)/lib/ant.jar

JARFILES:=castor
castor_DIRS:=org
castor_EXTRAS:=org/exolab/castor/castor.properties \
        org/exolab/castor/builder/castorbuilder.properties \
        org/exolab/castor/util/resources/messages.properties \
        org/exolab/castor/util/resources/SimpleTypes.properties \
        org/exolab/castor/util/resources/SimpleTypesMapping.properties
castor_JARS:=xercesImpl xalan commons-logging-1.2 jakarta-oro-2.0.5
castor_ENDORSED:=on

#
# java sources in Jarfile on/off
DEBUG:=on

RUN_ANT_PRE:=resurrectPatchedSource

$(MODDEP)_PREQS:=| $(MODPATH)/src/org

$(MODRULE)all: $(MODPATH) $(MODDEP) | $(MODPATH)/src/org
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)ant -f $(MODPATH)/src/build.xml clean
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODPATH)/src/org: $(MODPATH)/src/castor.patch
	$(AT)export CLASSPATH=""; ant -f $(MODPATH)/src/build.xml -DDEBUG=$(DEBUG) -DINTROOT=$(INTROOT) -DACSROOT=$(ACSROOT) $(RUN_ANT_PRE)
