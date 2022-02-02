#
# The directories for previous releases are normally not built
# but simply kept as they are.
# If necessary (but should also be checked if this is possible at all!)
# they can be built passing the proper tag.
# This Makefile has to be updated with the latest tag for each official release.
#
# For older releases there are no tags in the CVS archive.

#####
##### TODO After ACS 3.1 release, create and test first "old version"
#####      entry for ACS 3.1
#####

#
# ACS 4.0 (latest release is ACS-4_0_0)
#
TAG-ACS-4_0=ACS-4_0_1

.PHONY: ACS-4_0_Docs
ACS-4_0_Docs:
	cd ACS-4_0/Docs; make TAG=${TAG-ACS-4_0} all

#
# Development Head
#
.PHONY: DevelopmentHead_Docs
DevelopmentHead_Docs:
	cd DevelopmentHead/Docs; make all


clean: 
	cd DevelopmentHead/Docs; make clean

