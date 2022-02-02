PROFILE:=Unix
MAKEPROFILEDIR:=$(if $(wildcard $(MAKEDIR)/../include/acsMakefileProfile.$(PROFILE).mk),$(MAKEDIR)/..,$(shell searchFile include/acsMakefileProfile.$(PROFILE).mk))
$(if $(filter $(MAKEPROFILEDIR),#error#), $(error "$(PROFILE) Makefile Profile couldn't be found."),$(eval MAKEPROFILEDIR:=$(MAKEPROFILEDIR)/include)$(eval include $(MAKEPROFILEDIR)/acsMakefileProfile.$(PROFILE).mk))
CPP:=gcc -MM -MG
CC:=gcc
CXX:=g++
LD:=g++
NOSHARED_ON:=-Xlinker -Bstatic
NOSHARED_OFF:=-Xlinker -Bdynamic
SHLIB_EXT:=dll
RANLIB:=ranlib
INC_DEFAULT:=-I/usr/local/include
LIB_DEFAULT:=-L/usr/local/lib
XINC_DEFAULT:=-I/usr/X11R6/include/X11
XLIB_DEFAULT:=-L/usr/X11R6/lib
CXX_FOR_VERSION_TEST:=gcc
XARGS = xargs

# Todo: In ALMA/Apex ACS_GXX_4_OR_BETTER=1 is always fullfilled!
# Only one reference - should remove legacy branch
ACS_CXX_VERSION := $(shell $(CXX_FOR_VERSION_TEST) -dumpversion)
ifeq (cmd,$(findstring cmd,$(SHELL)))
    ACS_CXX_MAJOR_VERSION := $(shell $(CXX_FOR_VERSION_TEST) -dumpversion | sed -e "s/[^0-9\.]//g" | sed -e "s/\..*$$//")
else
    ACS_CXX_MAJOR_VERSION := $(shell $(CXX_FOR_VERSION_TEST) -dumpversion | sed -e 's/[^0-9\.]//g' | sed -e 's/\..*$$//')
endif
ifeq ($(findstring $(ACS_CXX_MAJOR_VERSION),1 2 3),$(ACS_CXX_MAJOR_VERSION))
       ACS_GXX_4_OR_BETTER := 0
else
       ACS_GXX_4_OR_BETTER := 1
endif
.LIBPATTERNS = lib%.dll lib%.dll.a lib%.a
TAO_IDLFLAGS += -Gxhst -Gxhsk -Gxhsv -Gxhex -Gxhcn
