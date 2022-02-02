#
# The following directory are FULLY generated under Linux:
#     acsabeans
#     acsexmpl
#     jcontexmpl
#
# The following directories have a recursive make:
#     ACS_docs
#
# PDF files are generated automatically if they are originated from MS word
#     documents in CVS:ACS/Documents.
#
# The following files need to be generated/copied by hand
#     Few PDF files in this directory
#     Vltsw   directory
#

TAG=branches/ACS-12_2-B
CURRDIR=$(shell pwd)

PLATFORM := $(shell uname)

.PHONY: checkEnv
checkEnv:
	@echo "====> Checking basic execution environment"
	@echo "   Path: $(PATH)"
	@echo "   Current directory: $(PWD)"
	@echo "   TAG: $(TAG)"

.PHONY: architecture
architecture:
	@echo "====> Updating architecture HTML and online UML model"
	@rm -rf ACSArchitecture
	@svn co https://alma-svn.hq.eso.org/p2/$(TAG)/ACS/Documents/ACSArchitecture
	@chmod -R ugo+r ACSArchitecture

.PHONY: ACSCommandCenter
ACSCommandCenter:
	@echo "====> Updating ACSCommandCenter module"
	@rm -rf ACSCommandCenter
	@cvs export -r $(TAG)  -d ACSCommandCenter ACS/Documents/ACSCommandCenter
	@chmod -R ugo+r ACSCommandCenter

.PHONY: WebStart
WebStart:
	@echo "====> Updating WebStart documentation module"
	@rm -rf WebStart
	@cvs export -r $(TAG)  -d WebStart ACS/Documents/WebStart
	@chmod -R ugo+r WebStart

.PHONY: acsexmpl
acsexmpl:
	@echo "====> Updating acsexmpl module"
	@rm -rf acsexmpl
	@cvs export -r $(TAG)  -d acsexmpl ACS/LGPL/CommonSoftware/acsexmpl
	@acsBUILDCheckModuleTree.sh acsexmpl
	@cd acsexmpl/ws/src; make man
	@chmod -R ugo+r acsexmpl

.PHONY: jcontexmpl
jcontexmpl:
	@echo "====> Updating jcontexmpl module"
	@rm -rf jcontexmpl
	@cvs export -r $(TAG)  -d jcontexmpl ACS/LGPL/CommonSoftware/jcontexmpl
	@acsBUILDCheckModuleTree.sh jcontexmpl
	@cd jcontexmpl/src; make man
	@chmod -R ugo+r jcontexmpl

.PHONY: ACS_docs
ACS_docs:
	@cd ACS_docs; make all

.PHONY: acscookbook
acscookbook:
	@echo "====> Updating cookbook"
	@rm -rf ACSCookbook
	@cvs -Q export -r $(TAG) -d ACSCookbook ACS/Documents/ACSCookbook
	@chmod -R ugo+r ACSCookbook
	@rm -rf ACSCookbook.tmp

all: checkEnv architecture umlModel ACSCommandCenter WebStart  \
     acsexmpl jcontexmpl ACS_docs acscookbook docs
	@echo ... done.

clean: 
	@rm -rf architecture ACSCommandCenter WebStart \
                acsexmpl jcontexmpl ACSCookbook
	@cd ACS_docs; make clean

docs:
	@echo "====> Updating PDF for MS Word and Open Office documents"
	@echo "      Changing directory to ../../../../../Documents"
	@echo "      Running: $(MAKE) DESTDIR=$(CURRDIR) listDocs"
	@cd ../../../../../Documents; \
	   $(MAKE) DESTDIR=$(CURRDIR) all


.PHONY: umlModel
umlModel: 
	@echo "====> Updating online ACS Architecture UML Model "
	@echo "      Changing directory to ./ACSArchitecture"
	@cd ./ACSArchitecture; $(MAKE) model

