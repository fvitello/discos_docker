GROUPS:=
MODULES:=contLogTest contNcTest corbaRefPersistenceTest contHandleTest

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
