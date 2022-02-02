GROUPS:=
MODULES:=alarmCommon laser-extlib acs-jms cmw-mom laser-util laser-source laser-source-cpp laser-source-python gp-openide gp laser-core alarmHibernate laser-client laser-definition laser-console alarm-clients demo managerTest baciPropsTest alarmTests containerTest

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
