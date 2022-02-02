GROUPS:=monitoring containerTests ACSLaser acsGUIs
MODULES:=jacsutil xmljbind xmlpybind acserridl acsidlcommon acsutil acsstartup loggingidl logging acserr acserrTypes acsQoS acsthread acscomponentidl cdbidl maciidl baciidl acsncidl acsjlog repeatGuard loggingts loggingtsTypes jacsutil2 cdb cdbChecker codegen cdb_rdb acsalarmidl acsalarm acsContainerServices acscomponent recovery basenc archiveevents parameter baci enumprop acscallbacks acsdaemonidl jacsalarm jmanager maci task acstime acsnc acsncdds acsdaemon acslog acstestcompcpp acsexmpl jlogEngine acspycommon acsalarmpy acspy comphelpgen XmlIdl define acstestentities jcont jcontnc nsStatisticsService jacsalarmtest jcontexmpl jbaci acssamp mastercomp acspyexmpl nctest acscommandcenter acssim bulkDataNT bulkData acscourse

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
