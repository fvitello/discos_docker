export USER_IDL = -I$(ACE_ROOT)/TAO/orbsvcs -I$(ACE_ROOT)/TAO/

MAKE_NOIFR_CHECK = on # jagonzal: there is a cyclic dependency between acsstartupIrFeed and acsstartupLoadIFR (checker)

MAKE_ONLY=C++
MAKE_ONLY=Java
IDL_TO_INSTALL=NotificationServiceMC NotifyExt 
IDL_FILES_L = Monitor_Types Monitor $(IDL_TO_INSTALL) NotifyMonitoringExt
MAKE_ONLY = Python
IDL_FILES = NotifyExt 
IDL_FILES_L = NotifyMonitoringExt DsLogAdmin

DEBUG=on

$(MODRULE)all: $(MODPATH) $(MODDEP) do_copy do_pidl
	@$(MAKE) -f Makefile.java  all
	@$(MAKE) -f Makefile.python  all
	@$(MAKE) -f Makefile.c++  all
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	@$(MAKE) -f Makefile.java  install
	@$(MAKE) -f Makefile.python install
	@$(MAKE) -f Makefile.c++  install
	@cp -r $(MODPATH)/lib/python/site-packages/* $(PRJTOP)/lib/python/site-packages/
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP) do_remove
	@$(MAKE) -f Makefile.java  clean
	@$(MAKE) -f Makefile.python  clean
	@$(MAKE) -f Makefile.c++  clean
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP) do_remove
	@$(MAKE) -f Makefile.java  clean_dist
	@$(MAKE) -f Makefile.python  clean_dist
	@$(MAKE) -f Makefile.c++  clean_dist
	$(AT)echo " . . . $@ done"

# to be able to compile pidl files for Java we have to create links!
do_copy:
	@mkdir -p $(MODPATH)/idl
	@cp -f $(ACE_ROOT)/TAO/orbsvcs/orbsvcs/NotifyExt.idl $(MODPATH)/idl
	@cp -f $(ACE_ROOT)/TAO/orbsvcs/orbsvcs/Notify/MonitorControl/NotificationServiceMC.idl $(MODPATH)/idl
	@cp -f $(ACE_ROOT)/TAO/orbsvcs/orbsvcs/Notify/MonitorControlExt/NotifyMonitoringExt.idl $(MODPATH)/idl
	@cp -f $(ACE_ROOT)/TAO/orbsvcs/orbsvcs/DsLogAdmin.idl $(MODPATH)/idl
	@cp -f $(ACE_ROOT)/TAO/tao/TimeBase.pidl $(MODPATH)/idl
	@cp -f $(ACE_ROOT)/TAO/tao/StringSeq.pidl $(MODPATH)/idl
	@cp -f $(ACE_ROOT)/TAO/tao/Monitor/Monitor.pidl $(MODPATH)/idl
	@ln -fs $(MODPATH)/idl/Monitor.pidl $(MODPATH)/idl/Monitor.idl
	@cp -f $(ACE_ROOT)/TAO/tao/Monitor/Monitor_Types.pidl $(MODPATH)/idl
	@ln -fs $(MODPATH)/idl/Monitor_Types.pidl $(MODPATH)/idl/Monitor_Types.idl

do_remove:
	@rm -rf $(MODPATH)/idl

do_pidl:
	$(AT) $(OMNI_IDL)  -I$(OMNI_ROOT)/idl/ $(MK_IDL_PATH) $(TAO_MK_IDL_PATH) -bacs_python -C$(MODPATH)/lib/python/site-packages  $(MODPATH)/idl/TimeBase.pidl
