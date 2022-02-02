INCLUDES      =  DDSHelper.h DDSPublisher.h DDSSubscriber.h \
		 DataReaderListener.h acsddsncDataReaderListener.h \
		 acsddsncCDBProperties.h 

LIBRARIES       = acsddsnc
LIBRARIES_L     =

acsddsnc_OBJECTS  = DDSHelperImpl DDSPublisherImpl DDSSubscriberImpl DataReaderListenerImpl acsddsncCDBPropertiesImpl
acsddsnc_LIBS	  = OpenDDS_Dcps maci

SCRIPTS         = startDCPSInfoRepo
SCRIPTS_L       =

$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"
