/*
* ALMA - Atacama Large Millimiter Array
* Copyright (c) National Astronomical Observatory of Japan, 2017 
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
* 
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*/
package alma.acs.bulkdata;

import com.rti.dds.infrastructure.ProductVersion_t;

import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang.StringEscapeUtils;

import org.xml.sax.SAXException;

/**
 * This class holds the singleton instance of the schema for QoS XML in 
 * a local file system.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTQosXmlSchema {
    private static BulkDataNTQosXmlSchema instance;

    /**
     * This method returns the singleton instance of this class. In the
     * first call of this method, this method loads QoS XML schema file
     * in RTI DDS product distribution.
     *
     * @throws CannotLoadQosXmlSchemaException
     * if QoS XML schema file cannot be loaded correctly from RTI DDS
     * product distribution.
     *
     * @return The singleton instance of this class.
     */
    public static synchronized BulkDataNTQosXmlSchema getInstance()
        throws CannotLoadQosXmlSchemaException {
        if (instance == null) {
            instance = loadQosXmlSchema();
        }

        return instance;
    }

    /**
     * This method loads QoS XML Schema file in RTI DDS product distribution
     * and returns the instance of {@link Schema} for further validation.
     *
     * <p>
     * This method uses NDDSHOME environmental variable to identify the root
     * directory of RTI DDS product distribution. Thus, this variable must be
     * set before calling this method.
     *
     * <p>
     * The schema file is loaded from
     * ${NDDSHOME}/resource/qos_profiles_x.y.z/schema/rti_dds_qos_profiles.xsd
     * which is the standard location of the schema file as described in
     * "17.7.2 XML File Validation During Editing" of
     * <a href="https://community.rti.com/rti-doc/510/ndds.5.1.0/doc/pdf/RTI_CoreLibrariesAndUtilities_UsersManual.pdf">the RTI DDS 5.1.0 user's manual</a>.
     *
     * @return An instance of {@link Schema} that can be used for validating
     *         QoS XML files.
     *
     * @throws CannotLoadQosXmlSchemaException
     * if QoS XML schema file cannot be loaded correctly from RTI DDS
     * product distribution.
     */
    static BulkDataNTQosXmlSchema loadQosXmlSchema()
        throws CannotLoadQosXmlSchemaException {
        // W3C XML Schema 1.0 must be supported by any implementation of JVM.
        // Thus, IllegalArgumentException must not happen here.
        SchemaFactory schemaFactory
            = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);        

        // It is not 100% sure if "5.x.y" in the user manual is the version
        // of RTI Connext product version, or the version of the core library.
        // This code assumes that it is the RTI Connext product version, so
        // here we obtain the version number from get_product_version() instead
        // of get_core_version().
        ProductVersion_t nddsVersion
            = com.rti.ndds.config.Version.get_instance().get_product_version();
        assert nddsVersion.major == 5;
        FileSystem fs = FileSystems.getDefault();
        String schemaFileName = "rti_dds_qos_profiles.xsd";
        Path schemaRelPath
            = fs.getPath("resource",
                         String.format("qos_profiles_%d.%d.%d",
                                       (int)nddsVersion.major,
                                       (int)nddsVersion.minor,
                                       (int)nddsVersion.release),
                         "schema",
                         schemaFileName);

        String nddsHome = BulkDataNTGlobalConfiguration.getenv("NDDSHOME");
        if (nddsHome == null) {
            String msg =
                "Environmental variable NDDSHOME is not defined. " +
                "It must be defined because the QoS XML Schema file is " +
                "loaded from ${NDDSHOME}/" + schemaRelPath + ".";
            throw new CannotLoadQosXmlSchemaException(msg);
        }

        Path nddsHomePath;
        try {
            nddsHomePath = fs.getPath(nddsHome);
        } catch (InvalidPathException ex) {
            String msg
                = "The value set to NDDSHOME environmental variable "
                + StringEscapeUtils.escapeJava(nddsHome)
                + " is not valid as a path. It must be defined " 
                + "correctly because the QoS XML Schema is loaded from "
                + "${NDDSHOME}/" + schemaRelPath + ".";
            throw new CannotLoadQosXmlSchemaException(msg, ex);
        }

        if (!Files.exists(nddsHomePath)) {
            String msg
                = "The path set to NDDSHOME environmental variable "
                + StringEscapeUtils.escapeJava(nddsHome)
                + " does not exist. It must exist because "
                + "the QoS XML Schema is loaded from ${NDDSHOME}/"
                + schemaRelPath + ".";
            throw new CannotLoadQosXmlSchemaException(msg);
        }

        Path schemaPath = nddsHomePath.resolve(schemaRelPath);
        if (!Files.isReadable(schemaPath)) {
            String msg
                = "Cannot read the file "
                + StringEscapeUtils.escapeJava(schemaPath.toString())
                + ". Check if this file exists and is readable.";
            throw new CannotLoadQosXmlSchemaException(msg);
        }

        try {
            return new BulkDataNTQosXmlSchema(schemaFactory
                                              .newSchema(schemaPath.toFile()),
                                              schemaPath);
        } catch (SAXException ex) {
            String msg
                = "Parse error happens when reading QoS XML Schema "
                + schemaPath + ".";
            throw new CannotLoadQosXmlSchemaException(msg, ex);
        }
    }


    private Schema schema;
    private Path path;

    /**
     * This method returns the instance of {@link Schema} that can be used
     * to create validator for QoS XML.
     *
     * @return The instance of {@link Schema} that can be used to create the
     *         validator for QoS XML.
     */
    public Schema getSchema() { return schema; }

    /**
     * This method returns the path of QoS XML schema file.
     *
     * @return The path of QoS XML schema file.
     */
    public Path getPath() { return path; }

    private BulkDataNTQosXmlSchema(Schema schema, Path path) {
        this.schema = schema;
        this.path = path;
    }
}
