INSTALL_FILES = ../lib/vtd-xml.jar

$(MODRULE)all: $(MODPATH) $(MODDEP) $(MODPATH)/lib/vtd-xml.jar
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(if $(wildcard $(MODPATH)/src/ximpleware_2.4_java),rm -r $(MODPATH)/src/ximpleware_2.4_java,)
	$(if $(wildcard $(MODPATH)/lib/vtd-xml.jar),rm -rf $(MODPATH)/lib/vtd-xml.jar)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODPATH)/lib/vtd-xml.jar: $(MODPATH)/src/ximpleware_2.4_java.zip |$(MODPATH)/lib
	$(AT)unzip -o $(MODPATH)/src/ximpleware_2.4_java.zip -d $(MODPATH)/src >/dev/null
	$(AT)cp $(MODPATH)/src/ximpleware_2.4_java/vtd-xml.jar $(MODPATH)/lib/
	$(AT)rm -rf $(MODPATH)/src/ximpleware_2.4_java
