SHELL=/bin/bash
SHUNIT_VER = 2.1.5

$(MODRULE)all: $(MODPATH) $(MODDEP) $(MODPATH)/src/shunit2-$(SHUNIT_VER)/build
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo ". . . installing shunit2 . . . "
	$(AT)cp $(MODPATH)/src/shunit2-$(SHUNIT_VER)/build/shunit2 $(INSTDIR)/bin
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(MODPATH)/src/shunit2-$(SHUNIT_VER)),PWD=$(MODPATH)/src/shunit2-$(SHUNIT_VER) $(MAKE) -C $(MODPATH)/src/shunit2-$(SHUNIT_VER) build-clean,)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo "\n . . . removing the shunit2 directory . . ."
	$(AT)$(if $(wildcard $(MODPATH)/src/shunit2-$(SHUNIT_VER)),rm -r $(MODPATH)/src/shunit2-$(SHUNIT_VER),)
	$(AT)echo " . . . $@ done"

$(MODPATH)/src/shunit2-$(SHUNIT_VER): $(MODPATH)/src/shunit2-$(SHUNIT_VER).tgz
	$(AT)echo ". . . unpacking the tar files . . . "
	$(AT)gtar -C $(MODPATH)/src -xzf $(MODPATH)/src/shunit2-$(SHUNIT_VER).tgz

$(MODPATH)/src/shunit2-$(SHUNIT_VER)/build: $(MODPATH)/src/shunit2-$(SHUNIT_VER)
	$(AT)echo ". . . building . . . "
	$(AT)PWD=$(MODPATH)/src/shunit2-$(SHUNIT_VER) $(MAKE) -C $(MODPATH)/src/shunit2-$(SHUNIT_VER) $(MAKE_PARS) build
