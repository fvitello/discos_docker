PROFILE:=Default
MAKEPROFILEDIR:=$(if $(wildcard $(MAKEDIR)/../include/acsMakefileProfile.$(PROFILE).mk),$(MAKEDIR)/..,$(shell searchFile include/acsMakefileProfile.$(PROFILE).mk))
$(if $(filter $(MAKEPROFILEDIR),#error#), $(error "$(PROFILE) Makefile Profile couldn't be found."),$(eval MAKEPROFILEDIR:=$(MAKEPROFILEDIR)/include)$(eval include $(MAKEPROFILEDIR)/acsMakefileProfile.$(PROFILE).mk))
export ECHO:=echo
ARCH:=$(shell uname -m)
CPU:=$(if $(filter $(ARCH),x86_64),-m64,-m32)
PLATFORM:=$(shell uname)
VW =
# define UNIX environment   
CCDEP:=gcc -M -MG -ansi
LD:=ld
AR:=ar
RANLIB:=ranlib
CC:=gcc -ansi
CXX:=gcc -ansi
CFLAGS:= $(CFLAGS) -Wall -fPIC
#All warnings left out for the default (see above) must be added here
$(if $(value MAKE_ALL_WARNINGS), $(eval CFLAGS:= $(CFLAGS) -Wshadow -Wcast-qual -Wcast-align -Wundef -W -Wno-unused -Wpointer-arith -Wwrite-strings -Wstrict-prototypes -Wmissing-prototypes -Wmissing-declarations))
$(if $(value MAKE_NO_PERMISSIVE),$(eval CXXFLAGS:=$(CXXFLAGS) -fno-operator-names),$(eval CXXFLAGS:=$(CXXFLAGS) -fno-operator-names -fpermissive))
RM:=rm -rf
XARGS:=env -i xargs
TCL_CHECKER:=tclCheck
WISH:=$(ALMASW_INSTDIR)/tcltk/bin/wish -f
TCL_CHECKER:=$(ALMASW_INSTDIR)/tcltk/bin/tclCheck
PATH_SEP:=:
