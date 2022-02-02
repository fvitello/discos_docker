USER_CFLAGS = -DLIBCGETOPT=0 -DWITH_GETTEXT=0 -DLOCALEDIR=\"$(localedir)\" -DNOT_UTIL_LINUX -D__GNU_LIBRARY__

EXECUTABLES     = getopt

getopt_OBJECTS   = getopt GNUgetopt GNUgetopt1	

ifeq ($(OS),SunOS )
$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"
else
$(MODRULE)all:
	@echo "Nothing to be done on OSs different than SunOS" 

$(MODRULE)install:
	@echo "Nothing to be clean on OSs different than SunOS"

$(MODRULE)clean:
	@echo "Nothing to be clean_dist on OSs different than SunOS"

$(MODRULE)clean_dist:
	@echo "Nothing to be installed on OSs different than SunOS"
endif
