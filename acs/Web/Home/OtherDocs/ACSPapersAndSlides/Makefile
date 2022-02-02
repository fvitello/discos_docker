#
# The following directory are FULLY generated under Linux:
#     abeans
#     acsabeans
#     acsexmpl
#     jcontexmpl
#
# The following directories have a recursive make:
#     ACS_docs
#
# The following files need to be generated/copied by hand
#     All PDF files in this directory
#     Vltsw   directory
#

TAG=HEAD

.PHONY: ACS-Course
ACS-Course:
	echo ====> Updating ACS course structure
	rm -rf ACS-Course
	mkdir -p ACS-Course
	cvs export -r $(TAG) -d ACS-Course ACS/Documents/ACS-Course
	chmod -R ugo+r ACS-Course
	cd ACS-Course; make all


all: ACS-Course
	echo ... done.

clean: 
	rm -rf ACS-Course
	echo ... done
