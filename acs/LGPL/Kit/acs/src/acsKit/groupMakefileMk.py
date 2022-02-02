#/usr/bin/env python

def groupMakefileMk():
    return '''MAKEID:=acs
MAKEDIR:=$(patsubst %/,%,$(dir $(abspath $(lastword $(MAKEFILE_LIST)))))
GRP_PATH:=$(MAKEDIR)
GRP_NAME:=$(notdir $(GRP_PATH))
MAKEDIRTMP:=$(shell searchFile include/InclusiveMakefile.mk)/include
$(if $(filter #error#%,$(MAKEDIRTMP)),$(error "InclusiveMakefile.mk was not found."),$(eval include $(MAKEDIRTMP)/InclusiveMakefile.mk))
$(eval $(call genGroup,$(GRP_NAME),$(GRP_PATH)))'''

if __name__ == '__main__':
    print(groupMakefileMk())
