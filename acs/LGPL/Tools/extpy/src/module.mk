#Module helper variables
PYXB_VER:=1.1.2
PYXB_SCRS:=pyxbdump pyxbgen pyxbwsdl
PYXB_PKGS:=pyxb
PYXB_INSF:=PyXB-$(PYXB_VER)-py2.7.egg-info
PYXML_VER:=0.8.4
PYXML_SCRS:=xmlproc_parse xmlproc_val
PYXML_PKGS:=_xmlplus
PYXML_INSF:=PyXML-$(PYXML_VER)-py2.7.egg-info

PY_EXT_DOC_L	   = pexpect-doc
PY_DISUTIL	   = PyXB-$(PYXB_VER) PyXML-$(PYXML_VER) Pmw 
#PY_DISUTIL	   = PyXB-$(PYXB_VER) PyXML-$(PYXML_VER) Pmw Numeric-24.2 numarray-1.3.3 python-ldap-2.0.1

#ACS Variables
PY_SCRIPTS         = pythfilter
PY_SCRIPTS_L       =

PY_MODULES         = acs_python 
PY_MODULES_L       =

PY_PACKAGES        = Pmw
PY_PACKAGES_L      =
pppppp_MODULES	   =

INSTALL_PY_SCRIPTS:=$(PYXB_SCRS) $(PYXML_SCRS)
INSTALL_PY_PACKAGES:=$(PYXB_PKGS) $(PYXML_PKGS)
INSTALL_FILES:=$(addprefix ../lib/python/site-packages/,$(PYXB_INSF) $(PYXML_INSF))

#ACS Rules
$(MODRULE)all: PY_DISUTIL_BUILD $(MODPATH) $(MODDEP) | $(MODPATH)/src/Pmw
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/,doc/doc doc/api doc/idl)),rm -r $(wildcard $(addprefix $(MODPATH)/,doc/doc doc/api doc/idl)),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/bin/,$(PYXB_SCRS))),rm $(wildcard $(addprefix $(MODPATH)/bin/,$(PYXB_SCRS))),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/lib/python/site-packages/,$(PYXB_PKGS))),rm -r $(wildcard $(addprefix $(MODPATH)/lib/python/site-packages/,$(PYXB_PKGS))),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/lib/python/site-packages/,$(PYXB_INSF))),rm $(wildcard $(addprefix $(MODPATH)/lib/python/site-packages/,$(PYXB_INSF))),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/bin/,$(PYXML_SCRS))),rm $(wildcard $(addprefix $(MODPATH)/bin/,$(PYXML_SCRS))),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/lib/python/site-packages/,$(PYXML_PKGS))),rm -r $(wildcard $(addprefix $(MODPATH)/lib/python/site-packages/,$(PYXML_PKGS))),)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/lib/python/site-packages/,$(PYXML_INSF))),rm $(wildcard $(addprefix $(MODPATH)/lib/python/site-packages/,$(PYXML_INSF))),)
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)$(if $(wildcard $(addprefix $(MODPATH)/src/,$(PY_DISUTIL))),rm -r $(wildcard $(addprefix $(MODPATH)/src/,$(PY_DISUTIL))),)
	$(AT)echo " . . . $@ done"

#Module Helper Rules
#PY_DISUTIL_BUILD : PYXML NUMERIC NUMARRAY LDAP PYXML
#PY_DISUTIL_BUILD : PYXB PYXML LXML NUMERIC SUDS
PY_DISUTIL_BUILD: PYXB PYXML 
	echo "== Building/Installing external Python modules now..."

PYXML: $(addprefix $(MODPATH)/,$(addprefix bin/,$(PYXML_SCRS)) $(addprefix lib/python/site-packages/,$(PYXML_PKGS)) $(addprefix lib/python/site-packages/,$(PYXML_INSF)))
$(addprefix $(MODPATH)/,$(addprefix bin/,$(PYXML_SCRS)) $(addprefix lib/python/site-packages/,$(PYXML_PKGS)) $(addprefix lib/python/site-packages/,$(PYXML_INSF))): $(MODRULE)pyxml_compile

.INTERMEDIATE: $(MODRULE)pyxml_compile
$(MODRULE)pyxml_compile: | $(MODPATH)/src/PyXML-$(PYXML_VER)
	@cd $(MODPATH)/src/PyXML-$(PYXML_VER); python setup.py build; python setup.py install --home=$(MODPATH) --install-purelib=$(MODPATH)/lib/python/site-packages --install-platlib=$(MODPATH)/lib/python/site-packages

$(MODPATH)/src/PyXML-$(PYXML_VER): $(MODPATH)/src/PyXML-$(PYXML_VER).tar.gz
	@echo "== Building PYXML"
	@gtar -C $(MODPATH)/src -zxvf $(MODPATH)/src/PyXML-$(PYXML_VER).tar.gz; 
	$(AT)touch $@

#$(MODPATH)/src/lxml-2.2.6: $(MODPATH)/src/lxml-2.2.6.tar.gz
#	@echo "== Building LXML"
#	@gtar -C $(MODPATH)/src -zxvf $(MODPATH)/src/lxml-2.2.6.tar.gz; 
#	@cd $(MODPATH)/src/lxml-2.2.6; python setup.py build; python setup.py install --home=$(MODPATH) --install-purelib=$(MODPATH)/lib/python/site-packages --install-platlib=$(MODPATH)/lib/python/site-packages

PYXB: $(addprefix $(MODPATH)/,$(addprefix bin/,$(PYXB_SCRS)) $(addprefix lib/python/site-packages/,$(PYXB_PKGS)) $(addprefix lib/python/site-packages/,$(PYXB_INSF)))
$(addprefix $(MODPATH)/,$(addprefix bin/,$(PYXB_SCRS)) $(addprefix lib/python/site-packages/,$(PYXB_PKGS)) $(addprefix lib/python/site-packages/,$(PYXB_INSF))): $(MODRULE)pyxb_compile

.INTERMEDIATE: $(MODRULE)pyxb_compile
$(MODRULE)pyxb_compile: $(MODPATH)/src/PyXB-$(PYXB_VER)/pyxb/binding/datatypes.py
	@cd $(MODPATH)/src/PyXB-$(PYXB_VER); python setup.py build; python setup.py install --home=$(MODPATH) --install-purelib=$(MODPATH)/lib/python/site-packages --install-platlib=$(MODPATH)/lib/python/site-packages

$(MODPATH)/src/PyXB-$(PYXB_VER)/pyxb/binding/datatypes.py: | $(MODPATH)/src/PyXB-$(PYXB_VER)
	$(AT)patch -d $(MODPATH)/src/PyXB-$(PYXB_VER) -p1 < $(MODPATH)/src/pyxb-datatypes-repr.patch

$(MODPATH)/src/PyXB-$(PYXB_VER): $(MODPATH)/src/PyXB-full-$(PYXB_VER).tar.gz
	@echo "== Building PyXB"
	@gtar -C $(MODPATH)/src -zxvf $(MODPATH)/src/PyXB-full-$(PYXB_VER).tar.gz;
	$(AT)touch $@

$(MODPATH)/src/Pmw: $(MODPATH)/src/Pmw.1.2.tar.gz
	$(AT)echo "== Building Python Mega Widgets"
	$(AT)gtar -C $(MODPATH)/src -zxvf $(MODPATH)/src/Pmw.1.2.tar.gz;
	$(AT)touch $@

#$(MODPATH)/src/Numeric-24.2: $(MODPATH)/src/Numeric-24.2.tar.gz
#	@echo "== Building Numeric"
#	@gtar -C $(MODPATH)/src/ -zxvf $(MODPATH)/src/Numeric-24.2.tar.gz;
#	@cd Numeric-24.2; patch -p1 < ../Numeric-gettimeofday.patch; python setup.py install --home=$(MODPATH) --install-purelib=$(MODPATH)/lib/python/site-packages --install-platlib=$(MODPATH)/lib/python/site-packages; 

#$(MODPATH)/src/numarray-1.3.3: $(MODPATH)/src/numarray-1.3.3.tar.gz
#	@echo "== Building numarray"
#	@gtar -C $(MODPATH)/src/ -zxvf $(MODPATH)/src/numarray-1.3.3.tar.gz;
#	@cd $(MODPATH)/src/numarray-1.3.3; python setup.py install --gencode; 

#$(MODPATH)/src/python-ldap-2.0.1: $(MODPATH)/src/python-ldap-2.0.1.tar.gz
#	@echo "== Building Python LDAP"
#	@gtar -C $(MODPATH)/src/ -zxvf $(MODPATH)/src/python-ldap-2.0.1.tar.gz;
#	@cd $(MODPATH)/src/python-ldap-2.0.1; cat setup.cfg | sed 's/local\/openldap-REL_ENG_2_1\///' >> setup.cfg.tmp
#	@mv $(MODPATH)/src/python-ldap-2.0.1/setup.cfg.tmp $(MODPATH)/src/python-ldap-2.0.1/setup.cfg 
#	@cd $(MODPATH)/src/python-ldap-2.0.1; python setup.py install;

#$(MODPATH)/src/suds-0.4: $(MODPATH)/src/suds-0.4.tar.gz
#	@echo "== Building suds"
#	@gtar -C $(MODPATH)/src/ -zxvf $(MODPATH)/src/suds-0.4.tar.gz;
#	@cd $(MODPATH)/src/suds-0.4; python setup.py install --home=$(MODROOT) --install-purelib=$(MODROOT)/lib/python/site-packages --install-platlib=$(MODROOT)/lib/python/site-packages;

PY_DOC:
	$(foreach file, $(PY_EXT_DOC_L), - $(AT) cd ../doc; if [ -e $(file).tgz ]; then echo "== Extracting external documentation: $(file).tgz"; gtar -zxvf $(file).tgz; fi )
