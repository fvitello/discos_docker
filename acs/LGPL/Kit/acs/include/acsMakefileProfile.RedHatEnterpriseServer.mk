PROFILE:=Linux
MAKEPROFILEDIR:=$(if $(wildcard $(MAKEDIR)/../include/acsMakefileProfile.$(PROFILE).mk),$(MAKEDIR)/..,$(shell searchFile include/acsMakefileProfile.$(PROFILE).mk))
$(if $(filter $(MAKEPROFILEDIR),#error#), $(error "$(PROFILE) Makefile Profile couldn't be found."),$(eval MAKEPROFILEDIR:=$(MAKEPROFILEDIR)/include)$(eval include $(MAKEPROFILEDIR)/acsMakefileProfile.$(PROFILE).mk))
CXXSTD:=-std=gnu++11
CSTD:=-std=gnu11
export ECHO:=echo -e
