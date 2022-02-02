GROUPS:=
#MODULES:=doxygen emacs tat expat loki extjars antlr hibernate extpy cppunit getopt astyle xercesc xercesj castor xsddoc extidl vtd-xml oAW shunit2 log4cpp scxml_apache
MODULES:=doxygen tat expat loki extjars antlr hibernate extpy cppunit getopt astyle xercesc xercesj castor xsddoc vtd-xml oAW shunit2 log4cpp scxml_apache

$(GRPRULE)build: $(GRPRULE)clean $(GRPRULE)all $(GRPRULE)install
	$(AT)echo " . . . '$(GRPRULE)build' done" 

$(GRPRULE)all: $(GRPDEP)
	$(AT)echo " . . . '$@' done" 

$(GRPRULE)install: install_$(GRPDEP)
	$(AT)echo " . . . '$@' done" 

$(GRPRULE)clean: clean_$(GRPDEP)
	$(AT)echo " . . . '$@' done" 

$(GRPRULE)clean_dist: clean_dist_$(GRPDEP)
	$(AT)echo " . . . '$@' done" 
