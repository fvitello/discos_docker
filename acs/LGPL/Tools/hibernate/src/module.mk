INSTALL_FILES:=
INSTALL_JARS:=ant-1.10.1.jar dom4j-2.1.1.jar jandex-2.0.5.Final.jar org.eclipse.core.commands-3.9.300.jar org.eclipse.equinox.common-3.10.300.jar \
	ant-launcher-1.10.1.jar ehcache-3.6.3.jar javassist-3.23.1-GA.jar org.eclipse.core.contenttype-3.7.300.jar org.eclipse.equinox.preferences-3.7.300.jar \
	antlr-2.7.7.jar freemarker-2.3.23.jar javax.activation-api-1.2.0.jar org.eclipse.core.expressions-3.6.300.jar org.eclipse.equinox.registry-3.8.300.jar \
	byte-buddy-1.8.17.jar hibernate-commons-annotations-5.1.0.Final.jar javax.persistence-api-2.2.jar org.eclipse.core.filesystem-1.7.300.jar org.eclipse.jdt.core-3.12.2.jar \
	cache-api-1.0.0.jar hibernate-core-5.3.7.Final.jar jaxen-1.1.6.jar org.eclipse.core.jobs-3.10.300.jar org.eclipse.osgi-3.13.300.jar classmate-1.3.4.jar \
	hibernate-jcache-5.3.7.Final.jar jboss-logging-3.3.2.Final.jar org.eclipse.core.resources-3.13.300.jar org.eclipse.text-3.8.100.jar commons-collections-3.2.2.jar \
	hibernate-jpa-2.1-api-1.0.2.Final.jar jboss-transaction-api_1.2_spec-1.1.1.Final.jar org.eclipse.core.runtime-3.15.200.jar slf4j-api-1.7.23.jar commons-logging-1.2.jar \
	hibernate-tools-5.3.7.Final.jar log4j-1.2.17.jar org.eclipse.equinox.app-1.4.100.jar

$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)cd $(MODPATH); mvn package
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)cd $(MODPATH); mvn clean
	$(AT)$(if $(wildcard $(MODPATH)/lib/*.jar),rm $(wildcard $(MODPATH)/lib/*.jar),)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"
