#*******************************************************************************
# ALMA - Atacama Large Millimeter Array
# Copyright (c) Associated Universities Inc., 2020
# (in the framework of the ALMA collaboration).
# All rights reserved.
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
#*******************************************************************************

#*******************************************************************************
# This Makefile follows ACS Standards (see Makefile(5) for more).
#*******************************************************************************
# REMARKS
#    None
#-------------------------------------------------------------------------------

#
# Scripts (public and local)
# ----------------------------
SCRIPTS         = hibernateCdbJDal MonitoringSyncTool xjc
SCRIPTS_L       =

#
# Python stuff (public and local)
# ----------------------------
PY_SCRIPTS         =
PY_SCRIPTS_L       =

PY_MODULES         =
PY_MODULES_L       =

PY_PACKAGES        =
PY_PACKAGES_L      =
pppppp_MODULES	   =

#
# Configuration Database Files
# ----------------------------
CDB_SCHEMAS = ControlDevice

#
# Jarfiles and their directories
#
JARFILES=cdb_rdb AcsTmcdbUtils

cdb_rdb_DIRS=com alma/TMCDB alma/acs/tmcdb/logic
cdb_rdb_EXTRAS=alma/TMCDB/maci/hibernate-mappings-maci.hbm.xml \
               alma/TMCDB/baci/hibernate-mappings-baci.hbm.xml \
               acsOnly-cdb_rdb-hibernate.cfg.xml
cdb_rdb_JLIBS := TMCDBswconfigStrategy cdbrdb-pojos 

AcsTmcdbUtils_DIRS := alma/acs/tmcdb/generated/lrutype alma/acs/tmcdb/utils
AcsTmcdbUtils_JLIBS := cdb_rdb
AcsTmcdbUtils_JARS:=tmcdbGenerator

#
# java sources in Jarfile on/off
DEBUG=on

#
# other files to be installed
#----------------------------
POJOS_JAR = cdbrdb-pojos.jar
INSTALL_FILES = ../lib/$(POJOS_JAR) ../lib/TMCDBswconfigStrategy.jar ../lib/commons-cli-1.2.jar \
		../lib/jaxb-api-2.3.1.jar ../lib/jaxb-core-2.3.0.1.jar ../lib/jaxb-impl-2.3.2.jar \
		../lib/jaxb-xjc-2.3.2.jar ../lib/pfl-basic-4.0.1.jar

DDLDATA=$(ACSDATA)/config/DDL

$(MODRULE)all: $(MODPATH) $(MODDEP) gen .done_generating_sql ../lib/TMCDBswconfigStrategy.jar .done_generating_classes ../lib/$(POJOS_JAR) do_all
	$(AT)echo " . . . $@ done"

$(MODRULE)install: $(MODPATH) install_$(MODDEP)
	$(AT)mkdir -p $(DDLDATA)/generic
	$(AT)mkdir -p $(DDLDATA)/oracle/TMCDB_swconfigcore
	$(AT)mkdir -p $(DDLDATA)/oracle/TMCDB_swconfigext
	$(AT)mkdir -p $(DDLDATA)/hsqldb/TMCDB_swconfigcore
	$(AT)mkdir -p $(DDLDATA)/hsqldb/TMCDB_swconfigext
	$(AT)mkdir -p $(DDLDATA)/mysql/TMCDB_swconfigcore
	$(AT)mkdir -p $(DDLDATA)/mysql/TMCDB_swconfigext
	$(AT)echo "== Copying generic .ddl files to $(DDLDATA)/generic"
	$(AT)cp $(MODPATH)/src/generic/TMCDB_swconfigcore.ddl $(DDLDATA)/generic
	$(AT)cp $(MODPATH)/src/generic/TMCDB_swconfigext.ddl $(DDLDATA)/generic
	$(AT)echo "== Copying generated Oracle .sql files to $(DDLDATA)/oracle"
	$(AT)cp $(MODPATH)/config/TMCDB_swconfigcore/oracle/* $(DDLDATA)/oracle/TMCDB_swconfigcore
	$(AT)cp $(MODPATH)/config/TMCDB_swconfigext/oracle/* $(DDLDATA)/oracle/TMCDB_swconfigext
	$(AT)echo "== Copying generated HSQLDB .sql files to $(DDLDATA)/hsqldb"
	$(AT)cp $(MODPATH)/config/TMCDB_swconfigcore/hsqldb/* $(DDLDATA)/hsqldb/TMCDB_swconfigcore
	$(AT)cp $(MODPATH)/config/TMCDB_swconfigext/hsqldb/* $(DDLDATA)/hsqldb/TMCDB_swconfigext
	$(AT)echo "== Copying generated MySQL .sql files to $(DDLDATA)/mysql"
	$(AT)cp $(MODPATH)/config/TMCDB_swconfigcore/mysql/* $(DDLDATA)/mysql/TMCDB_swconfigcore
	$(AT)cp $(MODPATH)/config/TMCDB_swconfigext/mysql/* $(DDLDATA)/mysql/TMCDB_swconfigext
	$(AT)echo " . . . $@ done"

$(MODRULE)clean: $(MODPATH) clean_$(MODDEP)
	$(AT)echo "== Deleting generated code"
	$(AT)rm -rf $(MODPATH)/config/TMCDB_swconfigcore
	$(AT)rm -rf $(MODPATH)/config/TMCDB_swconfigext
	$(AT)rm -rf $(MODPATH)/src/alma/acs/tmcdb/generated
	$(AT)rm -rf $(MODPATH)/src/tmcdb
	$(AT)rm -rf $(MODPATH)/src/gen
	$(AT)rm -f $(MODPATH)/src/.done_generating_sql
	$(AT)rm -f $(MODPATH)/src/.done_generating_classes
	$(AT)rm -f $(MODPATH)/src/CreateHsqldbTables.sql
	$(AT)rm -f $(MODPATH)/src/alma/acs/tmcdb/translator/Column2Attribute_*
	$(AT)rm -f $(MODPATH)/src/alma/acs/tmcdb/translator/Table2Class_*
	$(AT)rm -f $(MODPATH)/src/alma/acs/tmcdb/translator/TableInheritance_*
	$(AT)rm -f $(MODPATH)/src/alma/acs/tmcdb/translator/*.class
	$(AT)rm -f $(MODPATH)/lib/$(POJOS_JAR)
	$(AT)rm -f $(MODPATH)/lib/TMCDBswconfigStrategy.jar
	$(AT)echo " . . . $@ done"

$(MODRULE)clean_dist: $(MODPATH) clean_dist_$(MODDEP)
	$(AT)echo " . . . $@ done"

gen:
	acsStartJava -endorsed --addToJarpath $(call findDep,jACSUtil.jar,jar,lib,0,lib) org.exolab.castor.builder.SourceGenerator \
            -i $(MODPATH)/config/CDB/schemas/LRU.xsd  -package alma.acs.tmcdb.generated.lrutype

$(MODPATH)/src/.done_generating_sql : $(MODPATH)/src/generic/TMCDB_swconfigcore.ddl $(MODPATH)/src/generic/TMCDB_swconfigext.ddl
	$(AT)echo "=="
	$(AT)echo "== Generating SQL code"
	$(AT)echo "=="
	$(AT)generateTmcdbSchemas $(MODPATH)/src/generic/TMCDB_swconfigcore.ddl $(MODPATH)/config
	$(AT)generateTmcdbSchemas $(MODPATH)/src/generic/TMCDB_swconfigext.ddl $(MODPATH)/config
	$(AT)echo "=="
	$(AT)echo "== Generating SQL/Java translation code"
	$(AT)echo "=="
	$(AT)generateTmcdbHibernateStrategy $(MODPATH)/src/generic/TMCDB_swconfigcore.ddl $(MODPATH)/src
	$(AT)generateTmcdbHibernateStrategy $(MODPATH)/src/generic/TMCDB_swconfigext.ddl $(MODPATH)/src
	$(AT)touch $(MODPATH)/src/.done_generating_sql

$(MODPATH)/src/.done_generating_classes: ../lib/TMCDBswconfigStrategy.jar
	$(AT)echo "=="
	$(AT)echo "== Generating Java domain classes"
	$(AT)echo "=="
	$(AT)rm -rf $(MODPATH)/src/tmcdb
	$(AT)mkdir $(MODPATH)/src/tmcdb
	$(AT)rm -f CreateHsqldbTables.sql
	$(AT)cat $(MODPATH)/config/TMCDB_swconfigcore/hsqldb/CreateHsqldbTables.sql $(MODPATH)/config/TMCDB_swconfigext/hsqldb/CreateHsqldbTables.sql > $(MODPATH)/src/CreateHsqldbTables.sql
	$(AT)acsStartJava --addToJarpath $(call findDep,jACSUtil.jar,jar,lib,0,lib) org.hsqldb.cmdline.SqlTool --rcFile sqltool.rc $(MODPATH)/src/tmcdb $(MODPATH)/src/CreateHsqldbTables.sql
	$(AT)CLASSPATH="$(shell acsMakeJavaClasspath)" ant -verbose generate
	$(AT)echo "Java domain classes generated"
	$(AT)touch $(MODPATH)/src/.done_generating_classes

$(MODPATH)/lib/$(POJOS_JAR): $(MODPATH)/src/.done_generating_classes
	$(AT)echo "=="
	$(AT)echo "== Compiling generated domain classes"
	$(AT)cd $(MODPATH)/src/gen;\
	    CLASSPATH="$(shell acsMakeJavaClasspath)" javac alma/acs/tmcdb/*.java; \
	    jar cf ../../lib/$(POJOS_JAR) alma/acs/tmcdb/*.class; cd ..; 
	    mv gen src; jar uf ../lib/$(POJOS_JAR) src/alma/acs/tmcdb/*.java; mv src gen/; \
	    jar uf ../lib/$(POJOS_JAR) -C ../config/TMCDB_swconfigcore/ SwCore-orm.xml; \
	    jar uf ../lib/$(POJOS_JAR) -C ../config/TMCDB_swconfigext/ SwExt-orm.xml;

$(MODPATH)/lib/TMCDBswconfigStrategy.jar: $(MODPATH)/src/.done_generating_sql 
	$(AT)echo "== Compiling TMCDBswconfigStrategy.jar "
	$(AT)CLASSPATH="$(shell acsMakeJavaClasspath)" javac $(MODPATH)/src/alma/acs/tmcdb/translator/*.java; jar cf $(MODPATH)/lib/TMCDBswconfigStrategy.jar $(MODPATH)/src/alma/acs/tmcdb/translator/*.class; jar uf $(MODPATH)/lib/TMCDBswconfigStrategy.jar $(MODPATH)/src/alma/acs/tmcdb/translator/*.java;
	$(AT)rm -f $(MODPATH)/src/alma/acs/tmcdb/translator/*.class
