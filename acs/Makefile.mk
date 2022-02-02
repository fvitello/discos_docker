MAKEID:=acs
BASEDIR:=$(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
GRP_PATH:=$(MAKEDIR)
GRP_NAME:=$(notdir $(GRP_PATH))
MAKEDIR:=$(if $(filter prepare,$(MAKECMDGOALS)),$(BASEDIR)/LGPL/Kit/acs/src,$(BASEDIR))
MAKEDIRTMP:=$(if $(wildcard $(MAKEDIR)/../include/InclusiveMakefile.mk),$(MAKEDIR)/../include,$(shell searchFile include/InclusiveMakefile.mk)/include)
$(if $(filter #error#%,$(MAKEDIRTMP)),$(error "InclusiveMakefile.mk was not found."),$(eval include $(MAKEDIRTMP)/InclusiveMakefile.mk))
$(eval $(call genGroup,$(GRP_NAME),$(GRP_PATH)))
