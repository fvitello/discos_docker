INSTALL_JARS = activation.jar commons-beanutils-1.8.3.jar commons-cli-1.2.jar commons-digester-2.1.jar commons-jexl-1.1.jar commons-lang-2.5.jar \
	commons-math-2.1.jar commons-xml-resolver-1.2.jar hamcrest-core-1.3.jar hamcrest-library-1.3.jar hsqldb.jar jakarta-oro-2.0.5.jar jakarta-regexp-1.2.jar \
	jdom.jar jta-1.1.jar junit-dep-4.10.jar mockito-core-1.9.5.jar objenesis-1.2.jar prevayler-1.02.001.jar sqltool.jar xalan.jar xalan_serializer.jar \
	xmlunit-1.3.jar mysql-connector-java-5.1.20-bin.jar jchart2d-3.3.2.jar

$(MODRULE)all: $(MODPATH) $(MODDEP) $(MODPATH)/lib/slf4j-api-1.7.6.jar
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(MODPATH)/lib/slf4j-api-1.7.6.jar),rm $(MODPATH)/lib/slf4j-api-1.7.6.jar,)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODPATH)/lib/slf4j-api-1.7.6.jar: $(JACORB_HOME)/lib/slf4j-api-1.7.6.jar
	$(AT)echo "== Copying slf4j-api-1.7.6.jar used for building jacorb to this module."
	$(AT)cp -a $(JACORB_HOME)/lib/slf4j-api-1.7.6.jar $(MODPATH)/lib/slf4j-api-1.7.6.jar
