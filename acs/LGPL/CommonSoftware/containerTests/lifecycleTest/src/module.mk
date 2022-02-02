INCLUDES        = lifeCycleTestImpl.h lifeCycleCharTestImpl.h

LIBRARIES       = lifeCycleTestImpl lifeCycleCharTestImpl
LIBRARIES_L     =

lifeCycleTestImpl_OBJECTS   = lifeCycleTestImpl
lifeCycleTestImpl_LIBS = lifecycleTest_IFStubs acscomponent

lifeCycleCharTestImpl_OBJECTS   = lifeCycleCharTestImpl
lifeCycleCharTestImpl_LIBS = lifecycleTest_IFStubs baci

PY_PACKAGES        = lifecycleTestImpl 
PY_PACKAGES_L      =

IDL_FILES = lifecycleTest_IF
IDL_TAO_FLAGS =
USER_IDL =

lifecycleTest_IFStubs_LIBS = acscomponentStubs ACSErrTypeCommonStubs baciStubs

JARFILES = TestLifeCycleComp # TestLifeCycleCharComp

TestLifeCycleComp_DIRS = alma/lifecycleTest/TestLifeCycleCompImpl
TestLifeCycleComp_JLIBS = lifecycleTest_IF
#TestLifeCycleCharComp_DIRS = alma/lifecycleTest/TestLifeCycleCharCompImpl

DEBUG=on 
COMPONENT_HELPERS=on

$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"
