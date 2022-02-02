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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import com.rti.dds.infrastructure.ProductVersion_t;


/**
 * This JUnit test checks the behavior of {@link BulkDataNTQosXmlSchemaTest}.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTQosXmlSchemaTest {

    /**
     * This is the temporary directory to place XML Schema file for the
     * test.
     */
    @Rule
    public TemporaryFolder testFs = new TemporaryFolder();

    private static String nddsVerStr;

    /**
     * This method prepares temporary directories and files that mimic
     * RTI DDS product distribution for the test. This method creates
     * the directory hierarchy shown below:
     *
     * (root of temoprary directory)
     *  |- nddshome_correct/
     *  |   |- resource/
     *  |       |- qos_profiles_x.y.z/
     *  |           |- schema/
     *  |               |- rti_dds_qos_profiles.xsd
     *  |                  (copied from real RTI DDS product distribution)
     *  |- nddshome_wrong1/
     *  |   |- resource/
     *  |       |- qos_profiles_x.y.z/
     *  |           |- schema/
     *  |               |- rti_dds_qos_profiles.xsd.wrong
     *  |                   (file name is incorrect)
     *  |- nddshome_wrong2/
     *  |   |- resource/
     *  |       |- qos_profiles_x.y.z/
     *  |           |- schema_wrong/     (directory name is incorrect)
     *  |               |- rti_dds_qos_profiles.xsd
     *  |- nddshome_wrong3/
     *  |   |- resource/
     *  |       |- qos_profiles_0.0.0/   (directory name is incorrect)
     *  |           |- schema/
     *  |               |- rti_dds_qos_profiles.xsd
     *  |- nddshome_wrong4/
     *  |   |- resource_wrong/           (directory name is incorrect)
     *  |       |- qos_profiles_x.y.z/
     *  |           |- schema/
     *  |               |- rti_dds_qos_profiles.xsd
     *  |- nddshome_wrong5/
     *  |   |- resource/
     *  |       |- qos_profiles_x.y.z/
     *  |           |- schema/
     *  |               |- rti_dds_qos_profiles.xsd
     *  |                  (file name is OK, but the content is not XML at all)
     *  |- nddshome_wrong6/
     *  |   |- resource/
     *  |       |- qos_profiles_x.y.z/
     *  |           |- schema/
     *  |               |- rti_dds_qos_profiles.xsd
     *  |                  (file name is OK, but XML in it has syntax error)
     *  |- nddshome_wrong7/
     *  |   |- resource/
     *  |       |- qos_profiles_x.y.z/
     *  |           |- schema/
     *  |               |- rti_dds_qos_profiles.xsd
     *  |                  (file name is OK, but XML in it does not follow
     *  |                   XML Schema 1.0 standard)
     *  |- nddshome_wrong8/
     *  |   |- resource/
     *  |       |- qos_profiles_x.y.z/
     *  |           |- schema/
     *  |               |- rti_dds_qos_profiles.xsd/
     *  |                  (this is not file, but directory)
     *  |                   |- dummy
     *  |- nddshome_wrong9/
     *      |- resource/
     *          |- qos_profiles_x.y.z/
     *              |- schema/
     *                  |- rti_dds_qos_profiles.xsd
     *                     (file name is OK, but without read permission)
     *
     * where x.y.z is the version number of RTI DDS product. Note that it
     * is assumed that x.y.z is the version number of the product, and
     * not the core library.
     */
    @Before
    public void prepareNddsHome()
        throws IOException {
        ProductVersion_t nddsVersion
            = com.rti.ndds.config.Version.get_instance().get_product_version();
        nddsVerStr = String.format("%d.%d.%d",
                                   (int)nddsVersion.major,
                                   (int)nddsVersion.minor,
                                   (int)nddsVersion.release);
        String realSchemaPath[]
            = { System.getenv("NDDSHOME"),
                "resource",
                "qos_profiles_" + nddsVerStr,
                "schema",
                "rti_dds_qos_profiles.xsd" };

        // First, find the correct XML schema in RTI DDS product distribution,
        // and copy the contents to the byte array for further copy.
        File realSchemaFile = new File(String.join(File.separator,
                                                   realSchemaPath));
        byte[] realSchemaData = FileUtils.readFileToByteArray(realSchemaFile);

        // Create the simulated RTI DDS product distribution in the temporary
        // directory.
        String[] basePath = replace(realSchemaPath, 0, "nddshome_correct");
        FileUtils.writeByteArrayToFile(newFile(basePath), realSchemaData);

        // Wrong hierarchy.
        FileUtils.writeByteArrayToFile(newFile(replace(basePath,
                                                       0, "nddshome_wrong1",
                                                       4, "rti_dds_qos_profiles.xsd.wrong")),
                                       realSchemaData);
        FileUtils.writeByteArrayToFile(newFile(replace(basePath,
                                                       0, "nddshome_wrong2",
                                                       3, "schema_wrong")),
                                       realSchemaData);
        FileUtils.writeByteArrayToFile(newFile(replace(basePath,
                                                       0, "nddshome_wrong3",
                                                       2, "qos_profiles_0.0.0")),
                                       realSchemaData);
        FileUtils.writeByteArrayToFile(newFile(replace(basePath,
                                                       0, "nddshome_wrong4",
                                                       1, "resource_wrong")),
                                       realSchemaData);

        // Wrong XML contents
        String simpleText = "Hello world!";
        FileUtils.writeStringToFile(newFile(replace(basePath,
                                                    0, "nddshome_wrong5")),
                                    simpleText);

        String wrongXml
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n"
            + "elementFormDefault=\"qualified\"\n" 
            + "attributeFormDefault=\"unqualified\">\n"
            + "<xs:simpleType name=\"aaa\">" // not closed
            + "</xs:schema>";
        FileUtils.writeStringToFile(newFile(replace(basePath,
                                                    0, "nddshome_wrong6")),
                                    wrongXml);

        String invalidXml
            = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n"
            + "elementFormDefault=\"qualified\"\n" 
            + "attributeFormDefault=\"unqualified\">\n"
            + "<xs:simple></xs:simple>" // not a correct schema
            + "</xs:schema>";
        FileUtils.writeStringToFile(newFile(replace(basePath,
                                                    0, "nddshome_wrong7")),
                                    invalidXml);

        // the schema file is not actually file, but a directory
        FileUtils.writeStringToFile(newFile(add(replace(basePath,
                                                        0, "nddshome_wrong8"),
                                                "dummy")),
                                    "dummy text");

        // The schema file exists in the correct place, but does not have
        // read permission.
        File nonReadableFile = newFile(replace(basePath,
                                               0, "nddshome_wrong9"));
        FileUtils.writeByteArrayToFile(nonReadableFile, realSchemaData);
        nonReadableFile.setReadable(false);


        // Dump the temporary directory hierarchy.
        /*
        System.out.println("=== Hierarchy of temporary directory for the test ===");
        for (File f: FileUtils.listFiles(testFs.getRoot(),
                                         TrueFileFilter.TRUE,
                                         TrueFileFilter.TRUE)) {
            System.out.println(f);
        }
        System.out.println("=====================================================");
        */
    }

    /**
     * Test the successful procedure of loading QoS XML schema from NDDSHOME.
     */
    @Test
    public void testLoadQosXmlSchemaCorrect()
        throws CannotLoadQosXmlSchemaException,
               IOException {
        File nddshome = new File(testFs.getRoot(), "nddshome_correct");
        BulkDataNTGlobalConfiguration.setenv("NDDSHOME",
                                             nddshome.getAbsolutePath());
        File schemaFile = new File(nddshome,
                                   String.join(File.separator,
                                               "resource",
                                               "qos_profiles_" + nddsVerStr,
                                               "schema",
                                               "rti_dds_qos_profiles.xsd"));

        BulkDataNTQosXmlSchema schema 
            = BulkDataNTQosXmlSchema.loadQosXmlSchema();
        assertNotNull(schema.getSchema());
        assertNotNull(schema.getPath());
        assertTrue(Files.isSameFile(schema.getPath(),
                                    schemaFile.toPath()));
    }

    /**
     * Test failure procedures of loading QoS XML schema under the
     * condition that NDDSHOME holds a valid path.
     */
    @Test
    public void testLoadQosXmlSchemaWrong1() {
        String[] homes
            = new String[] { "nddshome_wrong1",
                             "nddshome_wrong2",
                             "nddshome_wrong3",
                             "nddshome_wrong4",
                             "nddshome_wrong5",
                             "nddshome_wrong6",
                             "nddshome_wrong7",
                             "nddshome_wrong8",
                             "nddshome_wrong9" };
        for (String home: homes) {
            File dir = new File(testFs.getRoot(), home);
            BulkDataNTGlobalConfiguration.setenv("NDDSHOME",
                                                 dir.getAbsolutePath());
            try {
                BulkDataNTQosXmlSchema.loadQosXmlSchema();
                fail(home);
            } catch (CannotLoadQosXmlSchemaException ex) {
                assertNotNull(ex.getMessage());
                assertFalse(ex.getMessage().isEmpty());
            }
        }
    }

    /**
     * Test failure procedures of loading QoS XML schema under the
     * condition that NDDSHOME is not properly defined.
     */
    @Test
    public void testLoadQosXmlSchemaWrong2() {
        File notExistDir = new File(testFs.getRoot(), "nddshome_not_exist");
        File invalidPath = new File(testFs.getRoot(), "\u0000nddshome_correct");
        String nddshomes[]
            = new String[] { null,
                             notExistDir.getAbsolutePath(),
                             invalidPath.getAbsolutePath() };

        for (String nddshome: nddshomes) {
            BulkDataNTGlobalConfiguration.setenv("NDDSHOME", nddshome);
            try {
                BulkDataNTQosXmlSchema.loadQosXmlSchema();
                fail(nddshome);
            } catch (CannotLoadQosXmlSchemaException ex) {
                assertNotNull(ex.getMessage());
                assertFalse(ex.getMessage().isEmpty());
                if (nddshome != null &&
                    nddshome.contains("\u0000")) {
                    assertTrue(ex.getCause() instanceof InvalidPathException);
                }
            }
        }
    }

    private static String[] replace(String[] strs, int index, String goal) {
        String[] ret = Arrays.copyOf(strs, strs.length);
        ret[index] = goal;
        return ret;
    }

    private static String[] replace(String[] strs,
                                    int index1, String goal1,
                                    int index2, String goal2) {
        String[] ret = Arrays.copyOf(strs, strs.length);
        ret[index1] = goal1;
        ret[index2] = goal2;
        return ret;
    }

    private static String[] add(String[] strs, String addStr) {
        String[] ret = Arrays.copyOf(strs, strs.length + 1);
        ret[strs.length] = addStr;
        return ret;
    }


    private static String[] folder(String[] strs) {
        return Arrays.copyOfRange(strs, 0, strs.length - 1);
    }
    
    private File newFile(String[] path) {
        File dir = new File(testFs.getRoot(),
                            String.join(File.separator, folder(path)));
        dir.mkdirs();
        return new File(testFs.getRoot(),
                        String.join(File.separator, path));
    }
    
    @After
    public void tearDown() {
        BulkDataNTGlobalConfiguration.setenv("NDDSHOME",
                                             System.getenv("NDDSHOME"));

    }
}
