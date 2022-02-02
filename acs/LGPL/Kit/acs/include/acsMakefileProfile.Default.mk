$(if $(ACSROOT),,$(error ACSROOT is not defined))
$(if $(filter prepare,$(MAKECMDGOALS)),,$(if $(wildcard $(ACSROOT)),,$(error "ACSROOT directory doesn't exist")))
$(if $(value GNU_ROOT),,$(error GNU_ROOT must be defined))
$(if $(value TCLTK_ROOT),,$(error TCLTK_ROOT must be defined))
