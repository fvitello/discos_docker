ANTLR_VER_2 := 2.7.7
ANTLR_VER_3 := 3.0.1

ANTLR_BINS:=antlr antlr-config
ANTLR_LIBS:=libantlr.a antlr.jar
ANTLR_INCS:=antlr/
ANTLR_MISC:=share/antlr-$(ANTLR_VER_2)/ share/doc/

INSTALL_FILES:=../lib/antlr-$(ANTLR_VER_3).jar $(addprefix ../bin/,$(ANTLR_BINS)) $(addprefix ../lib/,$(ANTLR_LIBS)) $(addprefix ../,$(ANTLR_MISC))
INCLUDES:=$(ANTLR_INCS)

#$(MODDEP)_PREQS:=$(addprefix $(MODPATH)/bin/,$(ANTLR_BINS)) $(addprefix $(MODPATH)/lib/,$(ANTLR_LIBS)) $(addprefix $(MODPATH)/include/,$(ANTLR_INCS)) $(addprefix $(MODPATH)/,$(ANTLR_MISC))

$(MODRULE)all: $(MODPATH) $(MODDEP) $(addprefix $(MODPATH)/bin/,$(ANTLR_BINS)) $(addprefix $(MODPATH)/lib/,$(ANTLR_LIBS)) $(addprefix $(MODPATH)/include/,$(ANTLR_INCS)) $(addprefix $(MODPATH)/,$(ANTLR_MISC))
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/bin/,$(ANTLR_BINS))),rm -f $(wildcard $(addprefix $(MODPATH)/bin/,$(ANTLR_BINS))),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/lib/,$(ANTLR_LIBS))),rm -f $(wildcard $(addprefix $(MODPATH)/lib/,$(ANTLR_LIBS))),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/include/,$(ANTLR_INCS))),rm -rf $(wildcard $(addprefix $(MODPATH)/include/,$(ANTLR_INCS))),)
	$(AT)$(if $(wildcard $(MODPATH)/share),rm -rf $(MODPATH)/share,)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . removing the antlr distribution directory . . . "
	$(AT)$(if $(wildcard $(MODPATH)/src/antlr-$(ANTLR_VER_2)),rm -rf $(MODPATH)/src/antlr-$(ANTLR_VER_2),)
	$(AT)echo " . . . $@ done"

$(MODPATH)/src/antlr-$(ANTLR_VER_2)/lib/cpp/antlr/CharScanner.hpp: $(MODPATH)/src/gcc44.patch | $(MODPATH)/src/antlr-$(ANTLR_VER_2)
	$(AT)patch -d $(MODPATH)/src -p0 < $(MODPATH)/src/gcc44.patch

$(MODPATH)/src/antlr-$(ANTLR_VER_2): $(MODPATH)/src/antlr-$(ANTLR_VER_2).tar.gz
	$(AT)echo " . . . unpacking antlr . . . "
	$(AT)gtar -C $(MODPATH)/src -xzf $(MODPATH)/src/antlr-$(ANTLR_VER_2).tar.gz
	$(AT)touch $(MODPATH)/src/antlr-$(ANTLR_VER_2)
#	@gtar -C $(MODPATH)/src -xzf $(MODPATH)/src/antlr-$(ANTLR_VER_3).tar.gz

$(MODPATH)/src/antlr-$(ANTLR_VER_2)/Makefile: | $(MODPATH)/src/antlr-$(ANTLR_VER_2)
	$(AT)echo " . . . configure . . . "
	$(AT)cd $(MODPATH)/src/antlr-$(ANTLR_VER_2); ./configure --prefix=$(MODPATH) --disable-csharp --disable-python
	$(AT)touch $(MODPATH)/src/antlr-$(ANTLR_VER_2)/Makefile

$(addprefix $(MODPATH)/bin/,$(ANTLR_BINS)) $(addprefix $(MODPATH)/lib/,$(ANTLR_LIBS)) $(addprefix $(MODPATH)/include/,$(ANTLR_INCS)) $(addprefix $(MODPATH)/,$(ANTLR_MISC)): $(MODRULE)compile

.INTERMEDIATE: $(MODRULE)compile
$(MODRULE)compile: $(MODPATH)/src/antlr-$(ANTLR_VER_2)/Makefile $(MODPATH)/src/antlr-$(ANTLR_VER_2)/lib/cpp/antlr/CharScanner.hpp
	$(AT)echo " . . . building antlr . . . "
	$(AT)cd $(MODPATH)/src/antlr-$(ANTLR_VER_2); $(MAKE) $(MAKE_PARS) SUBDIRS="antlr lib"
	$(AT)cd $(MODPATH)/src/antlr-$(ANTLR_VER_2); make SUBDIRS="antlr lib" install
	$(AT)sed -i "s/  cygwin|mingw|msys)/  mingw|msys)/" $(MODPATH)/bin/antlr
	$(AT)touch $(addprefix $(MODPATH)/bin/,$(ANTLR_BINS)) $(addprefix $(MODPATH)/lib/,$(ANTLR_LIBS)) $(addprefix $(MODPATH)/include/,$(ANTLR_INCS)) $(addprefix $(MODPATH)/,$(ANTLR_MISC))
