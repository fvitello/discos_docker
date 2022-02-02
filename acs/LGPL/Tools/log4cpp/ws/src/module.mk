LOG4CPP_VER:=1.0+
LOG4CPP_TAR:=1.0
LOG4CPP_PATCH:=configure doc/Doxyfile.in include/log4cpp/Appender.hh include/log4cpp/Category.hh include/log4cpp/config-vxworks.h include/log4cpp/FileAppender.hh \
	include/log4cpp/PatternLayout.hh include/log4cpp/Portability.hh include/log4cpp/Priority.hh include/log4cpp/PropertyConfigurator.hh include/log4cpp/RemoteSyslogAppender.hh \
	include/log4cpp/TimeStamp.hh m4/ACX_PTHREAD.m4 src/Appender.cpp src/BasicConfigurator.cpp src/BasicLayout.cpp src/Category.cpp src/FileAppender.cpp src/HierarchyMaintainer.cpp \
	src/Localtime.cpp src/PatternLayout.cpp src/PortabilityImpl.cpp src/Priority.cpp src/PropertyConfigurator.cpp src/PropertyConfiguratorImpl.cpp src/PropertyConfiguratorImpl.hh \
	src/RemoteSyslogAppender.cpp src/SimpleConfigurator.cpp src/TimeStamp.cpp tests/Clock.cpp tests/Clock.hh tests/testbench.cpp tests/testCategory.cpp tests/testConfig.cpp \
	tests/testErrorCollision.cpp tests/testFilter.cpp tests/testFixedContextCategory.cpp tests/testmain.cpp tests/testNDC.cpp tests/testNTEventLog.cpp tests/testPattern.cpp \
	tests/testPriority.cpp tests/testProperties.cpp tests/testPropertyConfig.cpp
LOG4CPP_SCRS:=log4cpp-config
LOG4CPP_LIBS:=liblog4cpp.a liblog4cpp.so liblog4cpp.so.4 liblog4cpp.so.4.0.6
LOG4CPP_INCS:=log4cpp/
LOG4CPP_MISC:=share/ doc/


$(MODRULE)all: $(MODPATH) $(MODDEP) $(addprefix $(MODPATH)/,$(addprefix bin/,$(LOG4CPP_SCRS)) $(addprefix lib/,$(LOG4CPP_LIBS))) | $(addprefix $(MODPATH)/,$(addprefix include/,$(LOG4CPP_INCS)) $(LOG4CPP_MISC))
	$(AT)echo ". . . building . . ."
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo ". . . installing log4cpp . . ."
	$(AT)$(MAKE) -C $(MODPATH)/src/ -f tests.Makefile install
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(MODPATH)/src/log4cpp-$(LOG4CPP_VER)/Makefile),$(MAKE) $(MAKE_PARS) -C $(MODPATH)/src/log4cpp-$(LOG4CPP_VER) clean)
	#$(AT)$(if $(wildcard $(MODPATH)/src/log4cpp-$(LOG4CPP_VER)/Makefile),$(MAKE) $(MAKE_PARS) -C $(MODPATH)/src/log4cpp-$(LOG4CPP_VER) distclean)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo ". . . clean_dist . . ."
	$(AT)$(if $(wildcard $(MODPATH)/src/log4cpp-$(LOG4CPP_VER)), rm -rf $(MODPATH)/src/log4cpp-$(LOG4CPP_VER))
	$(AT)$(if $(wildcard $(MODPATH)/src/log4cpp-$(LOG4CPP_TAR)), rm -rf $(MODPATH)/src/log4cpp-$(LOG4CPP_TAR))
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/src/,configure.log log4cpp_teststimes.log log4cpp_tests.log log4cpp_testmain.log)),rm $(wildcard $(addprefix $(MODPATH)/src/,configure.log log4cpp_teststimes.log log4cpp_tests.log log4cpp_testmain.log)),)
	$(AT)echo " . . . $@ done"

$(addprefix $(MODPATH)/,$(addprefix bin/,$(LOG4CPP_SCRS)) $(addprefix lib/,$(LOG4CPP_LIBS)) $(addprefix include/,$(LOG4CPP_INCS)) $(LOG4CPP_MISC)): $(MODRULE)compile

.INTERMEDIATE: $(MODRULE)compile
$(MODRULE)compile: $(MODPATH)/src/log4cpp-$(LOG4CPP_VER)/Makefile
	$(AT)echo ". . . compiling . . ."
	$(AT)$(MAKE) $(MAKE_PARS) -C $(MODPATH)/src/log4cpp-$(LOG4CPP_VER) all
	$(AT)$(MAKE) -C $(MODPATH)/src -f tests.Makefile all
	$(AT)$(MAKE) $(MAKE_PARS)  -C $(MODPATH)/src/log4cpp-$(LOG4CPP_VER) install

.PHONY : man
man :
	$(MAKE) $(MAKE_PARS) -C log4cpp-$(LOG4CPP_VER)/doc all install
	@echo " . . . man page(s) done"

# Do Nothing, for compatibility
.PHONY: db
$(MODRULE)db:
	@echo " . . . ../DB done"

$(MODPATH)/src/log4cpp-$(LOG4CPP_TAR): $(MODPATH)/src/log4cpp-$(LOG4CPP_TAR).tar.gz
	$(AT)echo ". . . unpacking the tar files . . ."
	$(AT)tar -C $(MODPATH)/src -xzf $(MODPATH)/src/log4cpp-$(LOG4CPP_TAR).tar.gz
	$(AT)touch $@
	$(AT)echo " . . . unpacking the tar files done"
	
$(addprefix $(MODPATH)/src/log4cpp-$(LOG4CPP_VER)/,$(LOG4CPP_PATCH)): $(MODRULE)patch

.INTERMEDIATE: $(MODRULE)patch
$(MODRULE)patch: $(MODPATH)/src/log4cpp-$(LOG4CPP_VER).patch | $(MODPATH)/src/log4cpp-$(LOG4CPP_VER)
	$(AT)echo ". . . patching . . ."
	$(AT)patch -d $(MODPATH)/src/log4cpp-$(LOG4CPP_VER) -p1 < $(MODPATH)/src/log4cpp-$(LOG4CPP_VER).patch
	$(AT)echo " . . . patch applied"

$(MODPATH)/src/log4cpp-$(LOG4CPP_VER): | $(MODPATH)/src/log4cpp-$(LOG4CPP_TAR)
	$(AT)cp -Rp $(MODPATH)/src/log4cpp-$(LOG4CPP_TAR) $(MODPATH)/src/log4cpp-$(LOG4CPP_VER)
	$(AT)touch $@

$(MODPATH)/src/log4cpp-$(LOG4CPP_VER)/Makefile: $(addprefix $(MODPATH)/src/log4cpp-$(LOG4CPP_VER)/,$(LOG4CPP_PATCH))
	$(AT)echo ". . . running configure . . ."
	$(AT)echo "   log4cpp tar file is: log4cpp-$(LOG4CPP_TAR).tar.gz" >configure.log
	$(AT)echo "   log4cpp version  is: $(LOG4CPP_VER)" >>configure.log
	$(AT)cd $(MODPATH)/src/log4cpp-$(LOG4CPP_VER); ./configure --enable-shared --prefix=$(MODPATH) >> $(MODPATH)/src/configure.log 2>&1
	$(AT)echo " . . . configuration file created"

.PHONY : printenv 
printenv:
	env | sort
