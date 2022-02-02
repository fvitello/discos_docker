/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 */
package alma.acs.tmcdb.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.exolab.castor.xml.XMLException;
import org.hibernate.type.StringType;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.SQLGrammarException;

import alma.acs.tmcdb.BACIPropArchMech;
import alma.acs.tmcdb.BACIProperty;
import alma.acs.tmcdb.Component;
import alma.acs.tmcdb.ComponentType;
import alma.acs.tmcdb.Configuration;
import alma.acs.tmcdb.generated.lrutype.BaciPropertyT;
import alma.acs.tmcdb.generated.lrutype.LruType;

import com.cosylab.cdb.jdal.hibernate.HibernateUtil;
import com.cosylab.cdb.jdal.hibernate.plugin.HibernateWDALPlugin;
import com.cosylab.cdb.jdal.hibernate.plugin.PluginFactory;
import com.cosylab.cdb.jdal.hibernate.HibernateDBUtil;

import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.impl.xs.XSLoaderImpl;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;



public class MonitoringSyncTool {
    private static final String TMCDB_CONFIGURATION_NAME = "TMCDB_CONFIGURATION_NAME";
    private static final Logger logger = Logger.getLogger(MonitoringSyncTool.class.getName());

    private boolean commit = false;
    private String configuration;
    private String component;
    private String componentType;

    protected Configuration config = null;
    protected HibernateUtil hibernateUtil;
    protected HibernateDBUtil hibernateDBUtil;
    protected HibernateWDALPlugin plugin;
    protected XSModel xsModel;


    private class AttChange {
        private String attName;
        private String originalValue;
        private String newValue;

        public AttChange(String attName, String originalValue,
                String newValue) {
            this.attName = attName;
            this.originalValue = originalValue;
            this.newValue = newValue;
        }

        public String getAttName() {
            return attName;
        }

        public void setAttName(String attName) {
            this.attName = attName;
        }

        public String getOriginalValue() {
            return originalValue;
        }

        public void setOriginalValue(String originalValue) {
            this.originalValue = originalValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

        public String getDescription() {
            return attName + " differ: original value '" + originalValue +
                "', new value '" + newValue + "'";
        }
    }

    public boolean getCommit() {
        return commit;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    /**
     * Synchronizes BACIProperties with respect to the information found in the
     * code-generated TMCDBXXXAdd.xml files. If the member variable component is
     * null, it will iterate through all the components found in the
     * Configuration.
     *
     * By default it won't do anything in the database. In order to synchronize
     * the database, the tool needs to be executed with the --commit option. The
     * rationale for this is that the log file should be examined carefully
     * before deciding to commit the changes in the database.
     *
     * There are three cases both for the BACIProperties:
     *
     * 1) if a BACIProperty is in both the database and the the XML file, the
     * attributes are compared. If any difference is found, the database is
     * updated with the values found in the XML file.
     *
     * 2) if a BACIProperty from the XML file is not found in the database, then
     * it is added.
     *
     * 3) If the BACIProperty is in the database, but not in the XML file, then
     * the BACIProperty is deleted from the database.
     *
     * @throws XMLException
     * @throws IOException
     * @throws Exception
     * @throws DbConfigException
     */
    public void synchronizeProperties() throws XMLException, IOException, Exception {
        String confName;
        if (configuration == null) {
            confName = System.getenv(TMCDB_CONFIGURATION_NAME);
            if (confName == null) {
                Exception ex = new Exception("Null TMCDB_CONFIGURATION_NAME environment variable");
                throw ex;
            }
        } else {
            confName = configuration;
        }
        logger.info("using configuration " + confName);

        hibernateUtil = HibernateUtil.getInstance(logger);
        plugin = PluginFactory.getPlugin(logger);
        hibernateDBUtil = new HibernateDBUtil(logger, plugin);
        hibernateDBUtil.setUp(false, false);
        Session session = hibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        Configuration configuration = getSwConfiguration(confName, session);

        // Parse all the TMCDBXXXAdd.xml files.
        String[] hwConfFiles = LruLoader.findTmcdbHwConfigFiles();

        Map<String, LruType> lruTypes = new HashMap<String, LruType>();
        for (String hwConfFile : hwConfFiles) {
            logger.info("parsing " + hwConfFile);
            LruType xmllru = LruType.unmarshalLruType(new FileReader(hwConfFile));
            lruTypes.put(xmllru.getName(), xmllru);
        }
        String qstr;
        List<Component> components = null;
        if ( (component == null) && (componentType == null) ) {
            // Update all Components under the Configuration.
            qstr = "FROM Component WHERE configuration = :conf";
            Query query = session.createQuery(qstr);
            query.setParameter("conf", configuration, hibernateUtil.getSessionFactory().getTypeHelper().entity(Component.class));;
            components = query.list();
        } else if ( (component == null) && (componentType != null) ) {
            // Update all Components which have the given ComponentType and Configuration.
            qstr = "FROM ComponentType WHERE IDL LIKE :idl";
            Query query = session.createQuery(qstr);
            query.setParameter("idl", componentType, StringType.INSTANCE);
            List<ComponentType> compTypes = query.list();
            if ( compTypes.size() == 0 ) {
                Exception ex =
                    new Exception(String.format("component type '%s' not found", componentType));
                throw ex;
            }
            components = new ArrayList<Component>();
            for (ComponentType compType : compTypes) {
                qstr = "FROM Component WHERE componentType = :type AND configuration = :conf";
                query = session.createQuery(qstr);
                query.setParameter("type", compType, session.getTypeHelper().entity(Component.class));
                query.setParameter("conf", configuration, session.getTypeHelper().entity(Component.class));
                components.addAll(query.list());
            }
        } else {
            // Update only the given Component.
            int idx = component.lastIndexOf('/');
            String compname = component.substring(idx+1);
            String path = component.substring(0, idx);
            qstr = "FROM Component WHERE configuration = :conf AND componentName = :name AND path = :path";
            Query query = session.createQuery(qstr);
            query.setParameter("conf", configuration, session.getTypeHelper().entity(Component.class));
            query.setParameter("name", compname, StringType.INSTANCE);
            query.setParameter("path", path, StringType.INSTANCE);
            components = query.list();
            if (components.size() == 0) {
                logger.severe("component not found. componentName: " + compname + "; path: " + path);
            }
        }

        String baciQueryStr = "FROM BACIProperty WHERE component = :comp";
        Query baciQuery = session.createQuery(baciQueryStr);
        for (Component comp : components) {
            logger.info("===============================================================================");
            logger.info("synchronizing component " + comp.getPath() + "/" + comp.getComponentName());

            baciQuery.setParameter("comp", comp, session.getTypeHelper().entity(BACIProperty.class));
            List<BACIProperty> properties = baciQuery.list();
            logger.info("# of properties found for this component: " + properties.size());

            String type = getComponentTypeFromComponentIDL(comp.getComponentType().getIDL());
            logger.fine("using LRU type: " + type);
            LruType lruType = lruTypes.get(type);
            if (lruType != null) {
                loadLruTypeParents(type);
                // A list to collect all properties in the XML file that have a corresponding
                // property in the database. At the end of processing, if a property in the XML
                // file is not in this list, it means that it was added.
                List<String> xmlFoundBaciProperties = new ArrayList<String>();
                for (BACIProperty prop : properties) {
                    logger.info("-------------------------------------------------------------------------------");
                    logger.info("synchronizing property " + prop.getPropertyName());
                    BaciPropertyT xmlBaciProperty = null;
                    for (BaciPropertyT xmlbp : getXmlBaciProperties(lruType, lruTypes)) {
                        if (xmlbp.getPropertyname().equals(prop.getPropertyName())) {
                            xmlBaciProperty = xmlbp;
                            xmlFoundBaciProperties.add(xmlbp.getPropertyname());
                            break;
                        }
                    }
                    if (xmlBaciProperty != null) {
                        BACIProperty newBaciProperty = toBACIProperty(xmlBaciProperty, comp);
                        AttChange[] bpdiffs = updateBACIProperty(prop, newBaciProperty);
                        if ( bpdiffs.length > 0 ) {
                            // The BACIProperty from the database exists in the XML, but
                            // one or more attributes are different. We update the database in this case.
                            logger.warning("updating property " + prop.getPropertyName());
                            for (AttChange diff : bpdiffs) {
                                logger.warning("  " + diff.getDescription());
                            }
                            if (commit) {
                                session.saveOrUpdate(prop);
                            }
                        }
                    } else {
                        // The BACIProperty is in the database but not in the XML, so
                        // it must have been deleted from the spreadsheet.
                        logger.warning("property will be deleted: " + prop.getPropertyName());
                        if (commit) {
                            session.delete(prop);
                        }
                    }
                }
                for (BaciPropertyT xmlbp : getXmlBaciProperties(lruType, lruTypes)) {
                    if (!xmlFoundBaciProperties.contains(xmlbp.getPropertyname())) {
                        // This property was added in the XML, so it should be added in the
                        // database.
                        BACIProperty newBp = toBACIProperty(xmlbp, comp);
                        logger.warning("property will be added: " + xmlbp.getPropertyname());
                        if (commit) {
                            session.save(newBp);
                        }
                    }
                }
            } else {
                logger.info("no XML file was found for this type: " + type);
            }
        }
        if (commit) {
            tx.commit();
        } else {
            tx.rollback();
        }
        session.close();
        hibernateUtil.getSessionFactory().close();
        HibernateUtil.clearInstance();
        System.out.println("Synchronization finished");
    }

    /**
     * Retrieves the Software Configuration from DB.
     *
     * @param confName Configuration name
     * @param session Hibernate Session
     * @return Software Configuration Hibernate POJO
     * @throws TmcdbException If the Configuration is not found in the DB.
     */
    private Configuration getSwConfiguration(String confName,
            Session session) throws Exception {
        String qstr = "FROM Configuration WHERE configurationname = '"
                + confName + "'";
        Configuration configuration = null;
        try {
            configuration = (Configuration) session.createQuery(qstr).uniqueResult();
            if (configuration == null)
                throw new Exception("Configuration not found in TMCDB: " + confName);
        }catch(SQLGrammarException sqlEx){
            throw new Exception("Configuration not found in TMCDB: " + confName);
        }
        logger.info("SW Configuration \"" + configuration.getConfigurationName() + "\" found in the TMCDB. Proceeding.");
        return configuration;
    }

    /**
     * Returns the BaciPropertyT properties for a given LruType, which is created from the
     * TMCDBAddXXX.xml files, adding the inherited properties if necessary.
     * TODO: This code depends on knowledge and structures form ALMA CONTROL software. This
     * should be replaced by an auto-inheritance resolver from the XML Device type.
     * @param type
     * @return
     */
    private List<BaciPropertyT> getXmlBaciProperties(LruType type, Map<String, LruType> types) {
        List<BaciPropertyT> retVal = new ArrayList<BaciPropertyT>();
        if (type != null) {
            for (BaciPropertyT p : type.getAssemblyType().getBaciProperty()) {
                retVal.add(p);
            }
            String parentType = getComponentTypeParent(type.getName(), types);
            if (parentType != null) {
                retVal.addAll(getXmlBaciProperties(types.get(parentType), types));
            }
        }
        return retVal;
    }

    private void loadLruTypeParents(String type) {
        Map<String, String> schemaFiles = LruLoader.findSchemaFiles();
        String schemaLocations = "";
        for (String key : schemaFiles.keySet()) {
            schemaLocations += "urn:schemas-cosylab-com:" + key + ":1.0 " + schemaFiles.get(key) + " ";
        }
        System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            XSImplementationImpl impl = (XSImplementationImpl) registry.getDOMImplementation("XS-Loader");
            XSLoaderImpl schemaLoader = (XSLoaderImpl) impl.createXSLoader(null);
            schemaLoader.setParameter("http://apache.org/xml/properties/schema/external-schemaLocation", schemaLocations);
            if (schemaFiles.get(type) != null) {
                File schema = new File(schemaFiles.get(type));
                this.xsModel = schemaLoader.loadURI(schema.toURI().toString());
            } else {
                throw new Exception("The schema file " + type + " was not found in the installation areas.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getComponentTypeParent(String type, Map<String, LruType> types) {
        XSNamedMap xsMap = this.xsModel.getComponents(XSConstants.TYPE_DEFINITION);
        XSTypeDefinition xsTDef = (XSTypeDefinition) xsMap.itemByName("urn:schemas-cosylab-com:" + type + ":1.0", type);
        while (xsTDef != null && xsTDef.getName().compareTo("anyType") != 0) {
            xsTDef = xsTDef.getBaseType();
            if (types.containsKey(xsTDef.getName()))
                return xsTDef.getName();
        }
        return null;
    }

    /**
     * Updates a BACIProperty according to the changes represented in another
     * BACIProperty.
     * @param origBp Original BACIProperty
     * @param updBp Updated BACIProperty
     * @return True if the original BACIProperty was changed, False if there were no
     * changes.
     */
    private AttChange[] updateBACIProperty(BACIProperty origBp, BACIProperty updBp) {
        List<AttChange> attchs = new ArrayList<AttChange>();
        if ((origBp.getDescription() != null) && !origBp.getDescription().equals(updBp.getDescription())) {
            attchs.add(new AttChange("Description", origBp.getDescription(), updBp.getDescription()));
            origBp.setDescription(updBp.getDescription());
        }
        if ((origBp.getFormat() != null) && !origBp.getFormat().equals(updBp.getFormat())) {
            attchs.add(new AttChange("Format", origBp.getFormat(), updBp.getFormat()));
            origBp.setFormat(updBp.getFormat());
        }
        if ((origBp.getUnits() != null) && !origBp.getUnits().equals(updBp.getUnits())) {
            attchs.add(new AttChange("Units", origBp.getUnits(), updBp.getUnits()));
            origBp.setUnits(updBp.getUnits());
        }
        if ((origBp.getResolution() != null) && !origBp.getResolution().equals(updBp.getResolution())) {
            attchs.add(new AttChange("Resolution", origBp.getResolution(), updBp.getResolution()));
            origBp.setResolution(updBp.getResolution());
        }
        if ((origBp.getArchive_priority() != null) && !origBp.getArchive_priority().equals(updBp.getArchive_priority())) {
            attchs.add(new AttChange("Archive_priority", origBp.getArchive_priority().toString(),
                    updBp.getArchive_priority().toString()));
            origBp.setArchive_priority(updBp.getArchive_priority());
        }
        if ((origBp.getArchive_min_int() != null) && !origBp.getArchive_min_int().equals(updBp.getArchive_min_int())) {
            attchs.add(new AttChange("Archive_min_int", origBp.getArchive_min_int().toString(),
                    updBp.getArchive_min_int().toString()));
            origBp.setArchive_min_int(updBp.getArchive_min_int());
        }
        if ((origBp.getArchive_max_int() != null) && !origBp.getArchive_max_int().equals(updBp.getArchive_max_int())) {
            attchs.add(new AttChange("Archive_max_int", origBp.getArchive_max_int().toString(),
                    updBp.getArchive_max_int().toString()));
            origBp.setArchive_max_int(updBp.getArchive_max_int());
        }
        if ((origBp.getDefault_timer_trig() != null) && !origBp.getDefault_timer_trig().equals(updBp.getDefault_timer_trig())) {
            attchs.add(new AttChange("Default_timer_trig", origBp.getDefault_timer_trig().toString(),
                    updBp.getDefault_timer_trig().toString()));
            origBp.setDefault_timer_trig(updBp.getDefault_timer_trig());
        }
        if ((origBp.getMin_timer_trig() != null) && !origBp.getMin_timer_trig().equals(updBp.getMin_timer_trig())) {
            attchs.add(new AttChange("Min_timer_trig", origBp.getMin_timer_trig().toString(),
                    updBp.getMin_timer_trig().toString()));
            origBp.setMin_timer_trig(updBp.getMin_timer_trig());
        }
        if ((origBp.getInitialize_devio() != null) && !origBp.getInitialize_devio().equals(updBp.getInitialize_devio())) {
            attchs.add(new AttChange("Initialize_devio", origBp.getInitialize_devio().toString(),
                    updBp.getInitialize_devio().toString()));
            origBp.setInitialize_devio(updBp.getInitialize_devio());
        }
        if ((origBp.getMin_delta_trig() != null) && !origBp.getMin_delta_trig().equals(updBp.getMin_delta_trig())) {
            attchs.add(new AttChange("Min_delta_trig", origBp.getMin_delta_trig().toString(),
                    updBp.getMin_delta_trig().toString()));
            origBp.setMin_delta_trig(updBp.getMin_delta_trig());
        }
        if ((origBp.getDefault_value() != null) && !origBp.getDefault_value().equals(updBp.getDefault_value())) {
            attchs.add(new AttChange("Default_value", origBp.getDefault_value(), updBp.getDefault_value()));
            origBp.setDefault_value(updBp.getDefault_value());
        }
        if ((origBp.getGraph_min() != null) && !origBp.getGraph_min().equals(updBp.getGraph_min())) {
            attchs.add(new AttChange("Graph_min", origBp.getGraph_min().toString(),
                    updBp.getGraph_min().toString()));
            origBp.setGraph_min(updBp.getGraph_min());
        }
        if ((origBp.getGraph_max() != null) && !origBp.getGraph_max().equals(updBp.getGraph_max())) {
            attchs.add(new AttChange("Graph_max", origBp.getGraph_max().toString(),
                    updBp.getGraph_max().toString()));
            origBp.setGraph_max(updBp.getGraph_max());
        }
        if ((origBp.getMin_step() != null) && !origBp.getMin_step().equals(updBp.getMin_step())) {
            attchs.add(new AttChange("Min_step", origBp.getMin_step().toString(),
                    updBp.getMin_step().toString()));
            origBp.setMin_step(updBp.getMin_step());
        }
        if ((origBp.getArchive_delta() != null) && !origBp.getArchive_delta().equals(updBp.getArchive_delta())) {
            attchs.add(new AttChange("Archive_delta", origBp.getArchive_delta().toString(),
                    updBp.getArchive_delta().toString()));
            origBp.setArchive_delta(updBp.getArchive_delta());
        }
        if ((origBp.getAlarm_high_on() != null) && !origBp.getAlarm_high_on().equals(updBp.getAlarm_high_on())) {
            attchs.add(new AttChange("Alarm_high_on", origBp.getAlarm_high_on().toString(),
                    updBp.getAlarm_high_on().toString()));
            origBp.setAlarm_high_on(updBp.getAlarm_high_on());
        }
        if ((origBp.getAlarm_low_on() != null) && !origBp.getAlarm_low_on().equals(updBp.getAlarm_low_on())) {
            attchs.add(new AttChange("Alarm_low_on", origBp.getAlarm_low_on().toString(),
                    updBp.getAlarm_low_on().toString()));
            origBp.setAlarm_low_on(updBp.getAlarm_low_on());
        }
        if ((origBp.getAlarm_high_off() != null) && !origBp.getAlarm_high_off().equals(updBp.getAlarm_high_off())) {
            attchs.add(new AttChange("Alarm_high_off", origBp.getAlarm_high_off().toString(),
                    updBp.getAlarm_high_off().toString()));
            origBp.setAlarm_high_off(updBp.getAlarm_high_off());
        }
        if ((origBp.getAlarm_low_off() != null) && !origBp.getAlarm_low_off().equals(updBp.getAlarm_low_off())) {
            attchs.add(new AttChange("Alarm_low_off", origBp.getAlarm_low_off().toString(),
                    updBp.getAlarm_low_off().toString()));
            origBp.setAlarm_low_off(updBp.getAlarm_low_off());
        }
        if ((origBp.getAlarm_timer_trig() != null) && !origBp.getAlarm_timer_trig().equals(updBp.getAlarm_timer_trig())) {
            attchs.add(new AttChange("Alarm_timer_trig", origBp.getAlarm_timer_trig().toString(),
                    updBp.getAlarm_timer_trig().toString()));
            origBp.setAlarm_timer_trig(updBp.getAlarm_timer_trig());
        }
        if ((origBp.getMin_value() != null) && !origBp.getMin_value().equals(updBp.getMin_value())) {
            attchs.add(new AttChange("Min_value", origBp.getMin_value().toString(),
                    updBp.getMin_value().toString()));
            origBp.setMin_value(updBp.getMin_value());
        }
        if ((origBp.getMax_value() != null) && !origBp.getMax_value().equals(updBp.getMax_value())) {
            attchs.add(new AttChange("Max_value", origBp.getMax_value().toString(),
                    updBp.getMax_value().toString()));
            origBp.setMax_value(updBp.getMax_value());
        }
        if ((origBp.getBitDescription() != null) && !origBp.getBitDescription().equals(updBp.getBitDescription())) {
            attchs.add(new AttChange("BitDescription", origBp.getBitDescription(), updBp.getBitDescription()));
            origBp.setBitDescription(updBp.getBitDescription());
        }
        if ((origBp.getWhenSet() != null) && !origBp.getWhenSet().equals(updBp.getWhenSet())) {
            attchs.add(new AttChange("WhenSet", origBp.getWhenSet(), updBp.getWhenSet()));
            origBp.setWhenSet(updBp.getWhenSet());
        }
        if ((origBp.getWhenCleared() != null) && !origBp.getWhenCleared().equals(updBp.getWhenCleared())) {
            attchs.add(new AttChange("WhenCleared", origBp.getWhenCleared(), updBp.getWhenCleared()));
            origBp.setWhenCleared(updBp.getWhenCleared());
        }
        if ((origBp.getStatesDescription() != null) && !origBp.getStatesDescription().equals(updBp.getStatesDescription())) {
            attchs.add(new AttChange("StatesDescription", origBp.getStatesDescription(), updBp.getStatesDescription()));
            origBp.setStatesDescription(updBp.getStatesDescription());
        }
        if ((origBp.getCondition() != null) && !origBp.getCondition().equals(updBp.getCondition())) {
            attchs.add(new AttChange("Condition", origBp.getCondition(), updBp.getCondition()));
            origBp.setCondition(updBp.getCondition());
        }
        if ((origBp.getAlarm_on() != null) && !origBp.getAlarm_on().equals(updBp.getAlarm_on())) {
            attchs.add(new AttChange("Alarm_on", origBp.getAlarm_on(), updBp.getAlarm_on()));
            origBp.setAlarm_on(updBp.getAlarm_on());
        }
        if ((origBp.getAlarm_off() != null) && !origBp.getAlarm_off().equals(updBp.getAlarm_off())) {
            attchs.add(new AttChange("Alarm_off", origBp.getAlarm_off(), updBp.getAlarm_off()));
            origBp.setAlarm_off(updBp.getAlarm_off());
        }
        if ((origBp.getData() != null) && !origBp.getData().equals(updBp.getData())) {
            attchs.add(new AttChange("Data", origBp.getData(), updBp.getData()));
            origBp.setData(updBp.getData());
        }
        if ((origBp.getAlarm_fault_family() != null) && !origBp.getAlarm_fault_family().equals(updBp.getAlarm_fault_family())) {
            attchs.add(new AttChange("Alarm_fault_family", origBp.getAlarm_fault_family(), updBp.getAlarm_fault_family()));
            origBp.setAlarm_fault_family(updBp.getAlarm_fault_family());
        }
        if ((origBp.getAlarm_fault_member() != null) && !origBp.getAlarm_fault_member().equals(updBp.getAlarm_fault_member())) {
            attchs.add(new AttChange("Alarm_fault_member", origBp.getAlarm_fault_member(), updBp.getAlarm_fault_member()));
            origBp.setAlarm_fault_member(updBp.getAlarm_fault_member());
        }
        if ((origBp.getAlarm_level() != null) && !origBp.getAlarm_level().equals(updBp.getAlarm_level())) {
            attchs.add(new AttChange("Alarm_level", origBp.getAlarm_level().toString(),
                    updBp.getAlarm_level().toString()));
            origBp.setAlarm_level(updBp.getAlarm_level());
        }
        if ((origBp.getArchive_suppress() != null) && !origBp.getArchive_suppress().equals(updBp.getArchive_suppress())) {
            attchs.add(new AttChange("Archive_suppress", origBp.getArchive_suppress().toString(),
                    updBp.getArchive_suppress().toString()));
            origBp.setArchive_suppress(updBp.getArchive_suppress());
        }
        if ((origBp.getArchive_mechanism() != null) && !origBp.getArchive_mechanism().equals(updBp.getArchive_mechanism())) {
            attchs.add(new AttChange("Archive_mechanism", origBp.getArchive_mechanism().toString(), updBp.getArchive_mechanism().toString()));
            origBp.setArchive_mechanism(updBp.getArchive_mechanism());
        }
        return attchs.toArray(new AttChange[0]);
    }

    private BACIProperty toBACIProperty(BaciPropertyT xmlbp, Component component) {
        BACIProperty bp = new BACIProperty();
        bp.setComponent(component);
        bp.setPropertyName(xmlbp.getPropertyname());
        bp.setDescription(xmlbp.getDescription());
        bp.setFormat(xmlbp.getFormat());
        bp.setUnits(xmlbp.getUnits());
        bp.setResolution(xmlbp.getResolution());
        bp.setArchive_priority(Integer.decode(xmlbp.getArchivePriority()));
        bp.setArchive_min_int(Double.valueOf(xmlbp.getArchiveMinInt()));
        bp.setArchive_max_int(Double.valueOf(xmlbp.getArchiveMaxInt()));
        bp.setDefault_timer_trig(Double.valueOf(xmlbp.getDefaultTimerTrig()));
        bp.setMin_timer_trig(Double.valueOf(xmlbp.getMinTimerTrig()));
        bp.setInitialize_devio(xmlbp.getInitializeDevio().equals("0") ? false : true);
        bp.setMin_delta_trig(Double.valueOf(xmlbp.getMinDeltaTrig()));
        bp.setDefault_value(xmlbp.getDefaultValue());
        bp.setGraph_min(Double.valueOf(xmlbp.getGraphMin()));
        bp.setGraph_max(Double.valueOf(xmlbp.getGraphMax()));
        bp.setMin_step(Double.valueOf(xmlbp.getMinStep()));
        bp.setArchive_delta(Double.valueOf(xmlbp.getArchiveDelta()));
        bp.setAlarm_high_on(Double.valueOf(xmlbp.getAlarmHighOn()));
        bp.setAlarm_low_on(Double.valueOf(xmlbp.getAlarmLowOn()));
        bp.setAlarm_high_off(Double.valueOf(xmlbp.getAlarmHighOff()));
        bp.setAlarm_low_off(Double.valueOf(xmlbp.getAlarmLowOff()));
        bp.setAlarm_timer_trig(Double.valueOf(xmlbp.getAlarmTimerTrig()));
        bp.setMin_value(Double.valueOf(xmlbp.getMinValue()));
        bp.setMax_value(Double.valueOf(xmlbp.getMaxValue()));
        bp.setBitDescription(xmlbp.getBitdescription());
        bp.setWhenSet(xmlbp.getWhenset());
        bp.setWhenCleared(xmlbp.getWhencleared());
        bp.setStatesDescription(xmlbp.getStatedescription());
        bp.setCondition(xmlbp.getCondition());
        bp.setAlarm_on(xmlbp.getAlarmOn());
        bp.setAlarm_off(xmlbp.getAlarmOff());
        bp.setData(xmlbp.getData());
        bp.setAlarm_fault_family(xmlbp.getAlarmFaultFamily());
        bp.setAlarm_fault_member(xmlbp.getAlarmFaultMember());
        bp.setAlarm_level(Integer.decode(xmlbp.getAlarmLevel()));
        bp.setArchive_suppress(xmlbp.getArchiveSuppress().equals("0") ? false : true);
        bp.setArchive_mechanism(BACIPropArchMech.valueOfForEnum(xmlbp.getArchiveMechanism()));
        return bp;
    }

    private String getComponentTypeFromComponentIDL(String idl) {
        String type = idl.replaceAll("IDL:.*/(.*):1.0", "$1");
        type = type.replace("CompSimBase", "");
        return type;
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option helpOpt= new Option("h", "help", false, "print this message");
        Option commitOpt= new Option("c", "commit", false, "commits changes into the database");
        Option verboseOpt= new Option("v", "verbose", false, "outputs more information in the log file (INFO)");
        Option vverboseOpt= new Option("vv", "vverbose", false, "outputs even more information in the log file (ALL)");
        Option logFileOpt = OptionBuilder.withArgName("file")
                                      .hasArg()
                                      .withDescription("output logs in the given file")
                                      .create("logfile");
        Option compNameOpt = OptionBuilder.withArgName("comp_name")
                                          .hasArg()
                                          .withDescription("component to synchronize")
                                          .create("component");
        Option confNameOpt = OptionBuilder.withArgName("conf_name")
                                          .hasArg()
                                          .withDescription("configuration to synchronize")
                                          .create("configuration");
        Option compTypeOpt = OptionBuilder.withArgName("comp_type")
                                          .hasArg()
                                          .withDescription("component type to synchronize (use '%' for wildcard)")
                                          .create("component_type");
        options.addOption(helpOpt);
        options.addOption(commitOpt);
        options.addOption(verboseOpt);
        options.addOption(vverboseOpt);
        options.addOption(logFileOpt);
        options.addOption(compNameOpt);
        options.addOption(confNameOpt);
        options.addOption(compTypeOpt);

        boolean commit = false;
        boolean verbose = false;
        boolean vverbose = false;
        String logFile = "MonitoringSyncTool.log";
        String component = null;
        String configuration = null;
        String compType = null;

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cli = parser.parse(options, args);
            if (cli.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("MonitoringSyncTool", options);
                System.exit(0);
            }
            if (cli.hasOption("commit")) {
                commit = true;
            }
            if (cli.hasOption("verbose")) {
                verbose = true;
            }
            if (cli.hasOption("vverbose")) {
                vverbose = true;
            }
            if (cli.hasOption("logfile")) {
                logFile = cli.getOptionValue("logfile");
            }
            if (cli.hasOption("component")) {
                component = cli.getOptionValue("component");
            }
            if (cli.hasOption("configuration")) {
                configuration = cli.getOptionValue("configuration");
            }
            if (cli.hasOption("component_type")) {
                compType = cli.getOptionValue("component_type");
            }
        } catch (ParseException ex) {
            System.err.println("Error parsing command line options: " + ex.getMessage());
            System.exit(-1);
        }

        // The produced log is very important to understand what the tool will do in
        // the database. The tool should be run in "rehearsal" mode first, the log
        // reviewed and then the tool should be run in commit mode.
        try {
            FileHandler logFileHandler = new FileHandler(logFile);
            logFileHandler.setFormatter(new TmcdbLogFormatter());
            for (Handler h : logger.getHandlers()) {
                logger.removeHandler(h);
            }
            for (Handler h : logger.getParent().getHandlers()) {
                logger.getParent().removeHandler(h);
            }
            logger.addHandler(logFileHandler);
            if (verbose) {
                logger.setLevel(Level.INFO);
            } else if (vverbose) {
                logger.setLevel(Level.ALL);
            } else {
                logger.setLevel(Level.WARNING);
            }
            logger.info("Level set to " + logger.getLevel());
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Finally ask the tool to synchronize the BACI Properties.
        try {
            MonitoringSyncTool tool = new MonitoringSyncTool();
            tool.setCommit(commit);
            tool.setConfiguration(configuration);
            tool.setComponent(component);
            tool.setComponentType(compType);
            tool.synchronizeProperties();
        } catch (XMLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
