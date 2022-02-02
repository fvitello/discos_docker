#
# The complete contents of this directory is fully generate
#
#            UNDER LINUX
#
###all:
###	echo ====> Cleaning idl, cpp, java and py doxigen documentation
###	rm -rf cpp idl java py
###	echo ====> Copying documentation tree from $(ACSROOT)/man
###	cp -r $(ACSROOT)/man/cpp .
###	cp -r $(ACSROOT)/man/idl .
###	cp -r $(ACSROOT)/man/java .
###	cp -r $(ACSROOT)/man/py .
###	chmod -R ugo+r cpp idl java py
###
###clean:	
###	echo ====> Cleaning idl, cpp, java and py doxigen documentation
###	rm -rf cpp idl java py

all: cpp_doc idl_doc java_doc py_doc schemas_doc errors_doc
	@echo "... all done"

clean:	cpp_clean idl_clean java_clean py_clean schemas_clean errors_clean
	@echo "... clean done"

cpp_doc: cpp_clean
	@echo "====> Copying cpp documentation tree from $(ACSROOT)/man"
	@cp -r $(ACSROOT)/man/cpp .
	@chmod -R ugo+r cpp

cpp_clean:	
	@echo "====> Cleaning cpp"
	@rm -rf cpp

idl_doc: idl_clean
	@echo "====> Copying idl documentation tree from $(ACSROOT)/man"
	@cp -r $(ACSROOT)/man/idl .
	@chmod -R ugo+r idl

idl_clean:	
	@echo "====> Cleaning idl"
	@rm -rf idl

java_doc: java_clean
	@echo "====> Copying java documentation tree from $(ACSROOT)/man"
	@cp -r $(ACSROOT)/man/java .
	@chmod -R ugo+r java

java_clean:	
	@echo "====> Cleaning java"
	@rm -rf java

py_doc: py_clean
	@echo "====> Copying py documentation tree from $(ACSROOT)/man"
	@cp -r $(ACSROOT)/man/py .
	@chmod -R ugo+r py

py_clean:	
	@echo "====> Cleaning py"
	@rm -rf py

schemas_doc: schemas_clean
	@echo "====> Copying schemas documentation tree from $(ACSROOT)/man"
	@cp -r $(ACSROOT)/man/schemas .
	@chmod -R ugo+r schemas

schemas_clean:	
	@echo "====> Cleaning schemas"
	@rm -rf schemas

errors_doc: errors_clean
	@echo "====> Copying errors documentation tree from $(ACSROOT)/man"
	@cp -r $(ACSROOT)/man/errors .
	@chmod -R ugo+r errors

errors_clean:	
	@echo "====> Cleaning errors"
	@rm -rf errors

