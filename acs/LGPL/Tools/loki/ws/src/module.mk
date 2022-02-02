USER_CFLAGS = -DLOKI_OBJECT_LEVEL_THREADING -DLOKI_MAKE_DLL 

INCLUDES        = lokiAbstractFactory.h lokiAssocVector.h lokiConstPolicy.h \
	lokiDataGenerators.h lokiEmptyType.h lokiFactory.h \
	lokiFunction.h lokiFunctor.h lokiHierarchyGenerators.h \
	lokiLockingPtr.h lokiExport.h lokiTypeInfo.h \
	lokiMultiMethods.h lokiNullType.h lokiOrderedStatic.h \
	lokiPimpl.h lokiRefToValue.h lokiRegister.h \
	lokiSafeFormat.h lokiScopeGuard.h lokiSequence.h \
	lokiSingleton.h lokiSmallObj.h lokiSmartPtr.h \
	lokiStatic_check.h lokiStrongPtr.h lokiThreads.h \
	lokiTuple.h lokiTypelist.h lokiTypelistMacros.h \
	lokiTypeManip.h lokiTypeTraits.h lokiVisitor.h

LIBRARIES       = loki

loki_OBJECTS   = lokiOrderedStatic lokiSafeFormat lokiSingleton lokiSmallObj lokiSmartPtr lokiStrongPtr
#loki_LDFLAGS = -lstdc++

$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"
