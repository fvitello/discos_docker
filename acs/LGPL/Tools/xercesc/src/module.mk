SHELL=/bin/bash 
PLATFORM := $(shell uname)
XERCES_VER = src_2_8_0
XERCES_MAJOR_VER = 28
XERCESC_LIBS:=libxerces-c.so libxerces-c.so.$(XERCES_MAJOR_VER) libxerces-c.so.$(XERCES_MAJOR_VER).0 libxerces-depdom.so libxerces-depdom.so.$(XERCES_MAJOR_VER) libxerces-depdom.so.$(XERCES_MAJOR_VER).0
XERCESC_INCLUDE:=xercesc/
export XERCESCROOT=$(MODPATH)/src/xerces-c-$(XERCES_VER)

INSTALL_FILES:=$(addprefix ../lib/,$(XERCESC_LIBS))
INCLUDES:=$(XERCESC_INCLUDE)

$(MODRULE)all: $(MODPATH) $(MODDEP) $(addprefix $(MODPATH)/lib/,$(XERCESC_LIBS)) $(addprefix $(MODPATH)/include/,$(XERCESC_INCLUDE))
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/lib/,$(XERCESC_LIBS))),rm $(wildcard $(addprefix $(MODPATH)/lib/,$(XERCESC_LIBS))))
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/include/,$(XERCESC_INCLUDE))),rm -r $(wildcard $(addprefix $(MODPATH)/include/,$(XERCESC_INCLUDE))))
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo "\n . . . removing the xerces-c directory and log file . . ."
	$(AT)$(if $(wildcard $(MODPATH)/src/xerces-c-$(XERCES_VER)),rm -r $(MODPATH)/src/xerces-c-$(XERCES_VER),)
	$(AT)$(if $(wildcard $(MODPATH)/src/configure.log),rm $(MODPATH)/src/configure.log,)
	$(AT)echo " . . . $@ done"

$(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/configure.in: $(MODPATH)/src/xerces-c-$(XERCES_VER).tar.gz
	$(AT)echo ". . . unpacking the tar file . . . "
	$(AT)gtar -C $(MODPATH)/src -xzf $(MODPATH)/src/xerces-c-$(XERCES_VER).tar.gz
	$(AT)touch $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/configure.in

$(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/configure: $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/configure.in
	$(AT)cd $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc; autoconf
	$(AT)touch $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/configure

$(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/Makefile: $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/configure
	$(AT)echo ". . . running configure . . . "
	$(AT)cd $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc; ./runConfigure -P $(MODPATH) -plinux -cgcc -xg++ -minmem -nsocket -tnative -rpthread > configure.log 2>&1
	$(AT)touch $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/Makefile

$(addprefix $(MODPATH)/lib/,$(XERCESC_LIBS)) $(addprefix $(MODPATH)/include/,$(XERCESC_INCLUDE)): $(MODRULE)compilation

.INTERMEDIATE: $(MODRULE)compilation
$(MODRULE)compilation: $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc/Makefile
	$(AT)echo ". . . building xerces. . . "
	$(AT)cd $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc; make -j 1
	$(AT)$(if $(wildcard $(MODPATH)/src/xerces-c-$(XERCES_VER)/lib/libxerces-c.so.$(XERCES_MAJOR_VER)),unlink $(MODPATH)/src/xerces-c-$(XERCES_VER)/lib/libxerces-c.so.$(XERCES_MAJOR_VER),)
	$(AT)$(if $(wildcard $(MODPATH)/src/xerces-c-$(XERCES_VER)/lib/libxerces-c.so),unlink $(MODPATH)/src/xerces-c-$(XERCES_VER)/lib/libxerces-c.so,)
	$(AT)$(if $(wildcard $(MODPATH)/src/xerces-c-$(XERCES_VER)/lib/libxerces-depdom.so.$(XERCES_MAJOR_VER)),unlink $(MODPATH)/src/xerces-c-$(XERCES_VER)/lib/libxerces-depdom.so.$(XERCES_MAJOR_VER),)
	$(AT)$(if $(wildcard $(MODPATH)/src/xerces-c-$(XERCES_VER)/lib/libxerces-depdom.so),unlink $(MODPATH)/src/xerces-c-$(XERCES_VER)/lib/libxerces-depdom.so,)
	#$(AT)make -C $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc install
	$(AT)cd $(MODPATH)/src/xerces-c-$(XERCES_VER)/src/xercesc; make install
	$(AT)touch $(addprefix $(MODPATH)/lib/,$(XERCESC_LIBS)) $(addprefix $(MODPATH)/include/,$(XERCESC_INCLUDE))
