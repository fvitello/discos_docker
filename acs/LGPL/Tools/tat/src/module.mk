ifdef ACSROOT
    NOCCS=1
endif

ifndef NOCCS
    VCC_LIB = vcc
endif

#
# TCL scripts (public and local)
# ------------------------------
TCL_SCRIPTS     = tat \
		  tatRemExec \
		  tatCleanShm \
		  tatGetClock \
		  tatEnvStatus \
                  tatTestSpawner

tat_OBJECTS  = tat
tat_LIBS = tatlib $(VCC_LIB)
tat_TCLSH = seqSh

tatRemExec_OBJECTS  = tatRemExec
tatRemExec_TCLSH = expect

tatCleanShm_OBJECTS  = tatCleanShm
tatCleanShm_LIBS = tatlib
tatCleanShm_TCLSH = tcl

tatGetClock_OBJECTS  = tatGetClock
tatGetClock_LIBS = tatlib
tatGetClock_TCLSH = tcl

tatEnvStatus_OBJECTS  = tatEnvStatus
tatEnvStatus_LIBS = tatlib $(VCC_LIB)
tatEnvStatus_TCLSH = seqSh

tatTestSpawner_OBJECTS  =    tatTestSpawner
tatTestSpawner_TCLSH    =    seqSh

TCL_LIBRARIES   = tatlib

tatlib_OBJECTS =tatLib
ifndef NOCCS
tatlib_OBJECTS := $(tatlib_OBJECTS) \
		  tatMakeLCUEnv \
		  tatMakeRTAPEnv \
		  tatCleanLCUEnv \
	          tatCleanRTAPEnv \
	          tatGetRTAPEnv \
                  tatGetLCUEnv \
                  tatGetQsemuEnv \
		  tatMakeQsemuEnv \
		  tatCleanQsemuEnv
endif


# man pages to be done
# --------------------
MANSECTIONS = 1
MAN1 = tat.tcl tatCleanShm.tcl tatEnvStatus.tcl tatTestSpawner.tcl
ifndef NOCCS
MAN1 := $(MAN1) tatMakeLCUEnv.tcl tatMakeRTAPEnv.tcl tatCleanLCUEnv.tcl                tatCleanRTAPEnv.tcl tatGetRTAPEnv.tcl tatGetQsemuEnv.tcl tatGetLCUEnv.tcl 
endif

$(MODRULE)all: $(MODPATH) $(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"
