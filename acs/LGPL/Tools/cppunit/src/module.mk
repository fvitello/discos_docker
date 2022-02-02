#SHELL=/bin/bash
#PLATFORM := $(shell uname)
#LD := ld
CPPUNIT_VER = 1.12.1
CPPUNIT_TAR = 1.12.1
CPPUNIT_EXES:=DllPlugInTester
CPPUNIT_SCRS:=cppunit-config
CPPUNIT_LIBS:=libcppunit.a
CPPUNIT_INCS:=cppunit/
CPPUNIT_MISC:=share/

SCRIPTS_L:= configure_script
INSTALL_EXECUTABLES:=$(CPPUNIT_EXES)
INSTALL_SCRIPTS:=$(CPPUNIT_SCRS)
INSTALL_LIBRARIES:=$(patsubst lib%.a,%,$(CPPUNIT_LIBS))
INCLUDES:=$(CPPUNIT_INCS)
INSTALL_FILES:=

$(MODRULE)all: $(MODPATH) $(MODDEP) $(addprefix $(MODPATH)/,$(addprefix bin/,$(CPPUNIT_EXES) $(CPPUNIT_SCRS)) $(addprefix lib/,$(CPPUNIT_LIBS))) | $(addprefix $(MODPATH)/include/,$(CPPUNIT_INCS))
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo ". . . installing cppunit . . . "
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(MODPATH)/src/cppunit-$(CPPUNIT_VER)/Makefile),cd $(MODPATH)/src/cppunit-$(CPPUNIT_VER); make clean > /dev/null)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . removing the cppunit directory and log file . . ."
	$(AT)$(if $(wildcard $(MODPATH)/src/cppunit-$(CPPUNIT_VER)),rm -rf $(wildcard $(MODPATH)/src/cppunit-$(CPPUNIT_VER)))
	$(AT)$(if $(wildcard $(MODPATH)/src/configure_script.log),rm -f $(MODPATH)/src/configure_script.log)
	$(AT)echo " . . . $@ done"

$(MODPATH)/src/cppunit-$(CPPUNIT_TAR): $(MODPATH)/src/cppunit-$(CPPUNIT_TAR).tar.gz
	$(AT)gtar -C $(MODPATH)/src -xzf $(MODPATH)/src/cppunit-$(CPPUNIT_TAR).tar.gz
	$(AT)touch $@

$(MODPATH)/src/cppunit-$(CPPUNIT_VER)/doc/Doxyfile.in: | $(MODPATH)/src/cppunit-$(CPPUNIT_TAR)
	$(AT)echo ". . . updating Doxyfile (ingnore error messages here) . . . "
	$(AT)cd $(MODPATH)/src/cppunit-$(CPPUNIT_VER)/doc; doxygen -u Doxyfile.in

$(MODPATH)/src/cppunit-$(CPPUNIT_VER)/examples/hierarchy/ChessTest.h: $(MODPATH)/src/cppunit-gcc-3_4_3.patch | $(MODPATH)/src/cppunit-$(CPPUNIT_TAR)
#	$(AT)patch -d $(MODPATH)/src/cppunit-$(CPPUNIT_VER) -p1 < $(MODPATH)/src/cppunit-gcc-3_4_3.patch
	$(AT)touch $@
	$(AT)echo " . . . patch applied"

$(MODPATH)/src/cppunit-$(CPPUNIT_VER)/Makefile: | $(MODPATH)/src/cppunit-$(CPPUNIT_TAR)
	$(AT)echo ". . . running configure . . . "
	#$(AT)$(MODPATH)/bin/configure_script cppunit-$(CPPUNIT_TAR).tar.gz $(CPPUNIT_VER) > configure_script.log 2>&1
	$(AT)cd $(MODPATH)/src/cppunit-$(CPPUNIT_VER); ./configure CC=gcc CXX=g++ LD=g++ RANLIB=ranlib --prefix=$(MODPATH)

$(addprefix $(MODPATH)/,$(addprefix bin/,$(CPPUNIT_EXES) $(CPPUNIT_SCRS)) $(addprefix lib/,$(CPPUNIT_LIBS)) $(addprefix include/,$(CPPUNIT_INCS))): $(MODRULE)compile

.INTERMEDIATE:$(MODRULE)compile
$(MODRULE)compile: $(MODPATH)/src/cppunit-$(CPPUNIT_VER)/Makefile $(MODPATH)/src/cppunit-$(CPPUNIT_VER)/examples/hierarchy/ChessTest.h $(MODPATH)/src/cppunit-$(CPPUNIT_VER)/doc/Doxyfile.in
	$(AT)echo ". . . building . . . "
	$(AT)cd $(MODPATH)/src/cppunit-$(CPPUNIT_VER); $(MAKE) $(MAKE_PARS) all
	$(AT)cd $(MODPATH)/src/cppunit-$(CPPUNIT_VER); $(MAKE) install

$(MODRULE)preparePatch:
	mv cppunit-gcc-3_4_3.patch cppunit-gcc-3_4_3.patch.old
	rm -rf tmp_unpack; mkdir -p tmp_unpack
	cd tmp_unpack; gtar -xzf ../cppunit-$(CPPUNIT_VER).tar.gz; mv cppunit-$(CPPUNIT_VER) cppunit-$(CPPUNIT_VER).orig
	LC_ALL=C TZ=UTC0 diff -Naur tmp_unpack/cppunit-$(CPPUNIT_VER).orig cppunit-$(CPPUNIT_VER)  >cppunit-gcc-3_4_3.patch; true
	rm -rf tmp_unpack
	@echo " . . . patch file prepared"

