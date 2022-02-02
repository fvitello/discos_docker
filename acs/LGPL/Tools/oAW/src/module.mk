INSTALL_FILES = \
	../lib/ecj-4.4.2.jar \
         ../lib/antlr-generator-3.0.1.jar \
         ../lib/com.google.collect_0.8.0.v201008251220.jar \
         ../lib/com.google.inject_2.0.0.v201003051000.jar \
         ../lib/com.ibm.icu_4.2.1.v20100412.jar \
         ../lib/org.antlr.runtime_3.0.0.v200803061811.jar \
         ../lib/org.eclipse.emf.codegen_2.6.0.v20100914-1218.jar \
         ../lib/org.eclipse.emf.codegen.ecore_2.6.1.v20100914-1218.jar \
         ../lib/org.eclipse.emf.common_2.6.0.v20100914-1218.jar \
         ../lib/org.eclipse.emf.ecore_2.6.1.v20100914-1218.jar \
         ../lib/org.eclipse.emf.ecore.xmi_2.5.0.v20100521-1846.jar \
         ../lib/org.eclipse.emf.mapping.ecore2xml_2.5.0.v20100521-1847.jar \
         ../lib/org.eclipse.emf.mwe2.runtime_1.0.1.v201008251113.jar \
         ../lib/org.eclipse.emf.mwe.core_1.0.0.v201008251122.jar \
         ../lib/org.eclipse.emf.mwe.utils_1.0.0.v201008251122.jar \
         ../lib/org.eclipse.equinox.common_3.6.0.v20100503.jar \
         ../lib/org.eclipse.text_3.5.0.v20100601-1300.jar \
         ../lib/org.eclipse.uml2.common_1.5.0.v201005031530.jar \
         ../lib/org.eclipse.uml2.uml_3.1.1.v201008191505.jar \
         ../lib/org.eclipse.uml2.uml.resources_3.1.1.v201008191505.jar \
         ../lib/org.eclipse.xpand_1.0.1.v201008251147.jar \
         ../lib/org.eclipse.xtend_1.0.1.v201008251147.jar \
         ../lib/org.eclipse.xtend.typesystem.emf_1.0.1.v201008251147.jar \
         ../lib/org.eclipse.xtend.typesystem.uml2_1.0.1.v201008251147.jar \
         ../lib/org.eclipse.xtend.util.stdlib_1.0.1.v201008251147.jar \
         ../lib/org.eclipse.xtext_1.0.1.v201008251220.jar \
         ../lib/org.eclipse.xtext.generator_1.0.1.v201008251220.jar \
         ../lib/org.eclipse.xtext.util_1.0.1.v201008251220.jar \
         ../lib/org.eclipse.xtext.xtend_1.0.1.v201008251220.jar

$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"
