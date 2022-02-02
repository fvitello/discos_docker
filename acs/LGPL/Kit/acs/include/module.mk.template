LIBRARIES:=
LIBRARIES_L:=
EXECUTABLES:=
EXECUTABLES_L:=
SCRIPTS:= acsMakeTclScript	acsMakeTclLib acsMan doxygenize acsChangeEnv \
		acsSwitchEnv instAlmaTarball JacPrep acsCheckGroupPermissions \
      cvs2cl acsUserConfig acsMakeCheckUnresolvedSymbols acsMakeJavaClasspath \
		acsMakeCopySources acsMakeLogInstallation acsMakeInstallFiles searchFile
SCRIPTS_L:=
PY_SCRIPTS:= acsConfigReport acsGetAllJars acsGetSpecificJars acsSearchPath
PY_SCRIPTS_L:=
PY_MODULES:= acsSearchPath
PY_MODULES_L:=
PY_PACKAGES:=
PY_PACKAGES_L:=
TCL_SCRIPTS:=acsReplace
acsReplace_OBJECTS:=acsReplace
acsReplace_TCLSH:=tcl
TCL_SCRIPTS_L:=
INCLUDES:=acsPort.h
CONFIGS:=
INSTALL_FILES:= ../include/acsMakefile ../include/acsNewMakefile.mk \
      ../include/acsMakefileDefinitions.mk ../include/acsMakefileCore.mk \
      ../include/Makefile_LCU.template ../include/Makefile_WS.template \
      ../include/Makefile_PACKAGE.template $(wildcard ../config/acsPackageInfo*.rpmref) \
      ../config/XSDIncludeDependencies.xml

MANSECTIONS = 1 5 n
MAN1 = acsMan acsConfigReport.py
MAN5 = ../include/acsMakefile Makefile.doc
MANn = acsReplace.tcl
MANl = $(SCRIPTS)

###############
###############
#Unsupported!
###############
###############

#PY_PACKAGES        =    acsConfigReportModule
#PY_PACKAGES_L      =
#pppppp_MODULES     =

$(MODRULE)all: $(MODDEP) do_acsPort
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP) $(PRJTOP)/include/vltPort.h
	$(AT)echo " . . . $@ done"

$(PRJTOP)/include/vltPort.h: $(PRJTOP)/include/acsPort.h
	$(AT)rm -f $(PRJTOP)/include/vltPort.h
	$(AT)ln -s $(PRJTOP)/include/acsPort.h $(PRJTOP)/include/vltPort.h

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	-$(AT) cp $</include/acsPort.h.DUMMY $</include/acsPort.h
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)rm -f $(PRJTOP)/include/vltPort.h
	$(AT)echo " . . . $@ done"

man   : do_man
	@echo " . . . man page(s) done"

do_acsPort: $(MODPATH)
	-$(AT) chmod 666   $</include/acsPort.h; cp $</include/acsPort.h.UNSUPPORTED $</include/acsPort.h; chmod 666   $</include/acsPort.h
	-$(AT) if [ "`uname`" = "Linux" ]; then cp $</include/acsPort.h.Linux $</include/acsPort.h; chmod 666 $</include/acsPort.h; fi
	-$(AT) if [ "`uname`" = "$(CYGWIN_VER)" ]; then cp $</include/acsPort.h.Cygwin $</include/acsPort.h; chmod 666 $</include/acsPort.h; fi
