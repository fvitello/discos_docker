##
##
## 2003-11-05 A. Caproni Created
##



# Temporary directory
export TMPLIB=$(PWD)/templib

#The directory where the generated files are stored
#(this is not used by the install tag)
export WEBDIR=$(PWD)/../web

#The modules to compile (with make)
MODULES=Shared_Acs ObjectExplorer WEB-INF

#Other modules to compile (with ant)
ANTMODULES=

all: do_jnlp do_html do_pictures do_livetest antmodules modules do_jar

clean: 
	@ rm -rf $(WEBDIR)
	@ rm -rf $(TMPLIB)
	@ rm -rf *~
	@ for testo in $(ANTMODULES) ; do \
		echo "### $${testo}"; \
		cd $${testo}/src; \
		if [ -a build.xml ] ;\
		then \
			ant clean;\
		else \
			echo "build.xml not found!";\
		fi; \
		cd ../..; \
	done;
	@ for testo in $(MODULES) ; do \
		echo "### $${testo}"; \
		cd $${testo}; \
		if [ -a Makefile ] ;\
		then \
			$(MAKE) clean;\
		else \
			echo "Makefile not found!";\
		fi; \
		cd ..; \
	done;

setup:
	@ mkdir -p $(TMPLIB)
	@ mkdir -p $(WEBDIR)

do_jnlp: setup
	cp jnlp/*.* $(WEBDIR)

do_jar: setup
	@ echo "### wsAssistant.py";
ifdef STOREPASS
	@ mkdir -p $(WEBDIR)/lib
	./wsAssistant.py -v -keystore=EsoKeystore -storepass=$(STOREPASS) -alias=eso \
	   -classpath=$(CLASSPATH) \
	   -include=$(TMPLIB) \
	   -include=$(ACSROOT)/lib/endorsed \
	   -include=$(ACSROOT)/lib/ACScomponents \
	   -include=$(JACORB_HOME)/lib \
	   jnlp/*.jnlp $(ACSROOT) $(WEBDIR)
else
	@ echo ""
	@ echo "--> Can't sign jars. You need to define the STOREPASS variable for access to EsoKeystore"
	@ echo ""
endif

do_html: setup
	cp -u ./html/*.* $(WEBDIR)

do_pictures: setup
	@ mkdir -p $(WEBDIR)/pictures
	cp -u pictures/*.* $(WEBDIR)/pictures

do_livetest: setup
	@ mkdir -p $(WEBDIR)/livetest
	cp -u livetest/*.* $(WEBDIR)/livetest

antmodules:
	@ for testo in $(ANTMODULES) ; do \
		echo "### $${testo} "; \
		cd $${testo}/src; \
		if [ -a build.xml ] ;\
		then \
			ant all ;\
		else \
			echo "Makefile not found!";\
		fi; \
		cd ../..; \
	done;

modules: setup
	@ for jarFile in `ls $(ACSROOT)/lib/*jar` ; do \
		export CLASSPATH=$${CLASSPATH}:$${jarFile}; \
	done; \
	for jarFile in `ls $(JACORB_HOME)/lib/*jar` ; do \
		export CLASSPATH=$${CLASSPATH}:$${jarFile}; \
	done; \
	for testo in $(MODULES) ; do \
		echo "### $${testo}"; \
		cd $${testo}; \
		if [ -a Makefile ] ;\
		then \
			$(MAKE) all install;\
		else \
			echo "Makefile not found!";\
		fi; \
		cd ..; \
	done;

install:
	@ echo "Refer to the README file for details on installation"
	
	
	
	
	
	