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
import org.junit.Test;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * This JUnit test checks the behavior of
 * {@link BulkDataNTSenderStreamConfigurationDefault}.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTSenderStreamConfigurationDefaultTest {
    /**
     * Test default values.
     */
    @Test
    public void testDefaultValues() throws IllegalDomainIdException {
        BulkDataNTGlobalConfiguration.setenv("ACS_INSTANCE", "0");

        BulkDataNTSenderStreamConfigurationDefault
            configuration = new BulkDataNTSenderStreamConfigurationDefault();
        assertConfiguration(null, configuration,
                            "BulkDataQoSLibrary",
                            "SenderStreamDefaultQosProfile",
                            0,
                            Logger.getLogger(BulkDataNTSenderStream.class.getName()));
    }

    /**
     * Check if domain ID changes according to ACS_INSTANCE.
     */
    @Test
    public void testDomainId() throws IllegalDomainIdException {
    	
        @SuppressWarnings("serial")
		Map<String, Integer> inputAndAnswers
            = new HashMap<String, Integer>() {
            {
                put(null, 0);
                put("0", 0);
                put("1", 1);
                put("2", 2);
                put("3", 3);
                put("4", 4);
                put("5", 5);
                put("6", 6);
                put("7", 7);
                put("8", 8);
                put("9", 9);

                // The values below are not valid as the value of
                // ACS_INSTANCE (0 ~ 9), but domain ID can be any
                // 32-bit integer value and should work well without
                // any problem
                put("00", 0);
                put("09", 9);
                put("10", 10);
                put("11", 11);
                put("2147483647", Integer.MAX_VALUE);
                put("-2147483648", Integer.MIN_VALUE);
                put("-1", -1);
            }
        };

        for (Map.Entry<String, Integer> entry: inputAndAnswers.entrySet()) {
            BulkDataNTGlobalConfiguration.setenv("ACS_INSTANCE",
                                                 entry.getKey());
            BulkDataNTSenderStreamConfigurationDefault
                configuration = new BulkDataNTSenderStreamConfigurationDefault();
            
            assertConfiguration("ACS_INSTANCE=" + entry.getKey(),
                                configuration,
                                "BulkDataQoSLibrary",
                                "SenderStreamDefaultQosProfile",
                                entry.getValue().intValue(),
                                Logger.getLogger(BulkDataNTSenderStream.class.getName()));
        }
    }

    /**
     * Check InvalidAcsInstanceException is thrown when ACS_INSTANCE does not
     * hold a 32-bit integer value.
     */
    @Test
        public void testInvalidAcsInstance() {
        String[] invalidVals
            = new String[] { "abc",
                             "1a\b2",
                             "0x01",
                             "2147483648", // 2^31
                             "-2147483649", // -2^31 - 1
                             "" };
        for (String invalidVal: invalidVals) {
            String msg = "ACS_INSTANCE=" + invalidVal;

            BulkDataNTGlobalConfiguration.setenv("ACS_INSTANCE", invalidVal);
            try {
                new BulkDataNTSenderStreamConfigurationDefault();
                fail(msg);
            } catch (IllegalDomainIdException ex) {
                assertNotNull(msg, ex.getMessage());
                assertFalse(msg, ex.getMessage().isEmpty());
                assertNotNull(msg, ex.getValue());
                assertEquals(msg, invalidVal, ex.getValue());
            }
        }
    }

    /**
     * Check if domain ID changes according to the value passed to the
     * constructor.
     */
    @Test
    public void testDomainIdConstructor() {
        int[] vals = new int[] { Integer.MIN_VALUE, -1,
                                 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                 10, 11, 12, Integer.MAX_VALUE };
        for (int val: vals) {
            BulkDataNTSenderStreamConfigurationDefault configuration
                = new BulkDataNTSenderStreamConfigurationDefault(val);

            assertConfiguration("Domain ID:" + val,
                                configuration,
                                "BulkDataQoSLibrary",
                                "SenderStreamDefaultQosProfile",
                                val,
                                Logger.getLogger(BulkDataNTSenderStream.class.getName()));
        }
    }

    /**
     * Test overriding QoS library name with the constructor that takes
     * no argument.
     */
    @Test
    public void testOverride1() throws IllegalDomainIdException {
        BulkDataNTGlobalConfiguration.setenv("ACS_INSTANCE", "0");

        BulkDataNTSenderStreamConfigurationDefault
            configuration = new BulkDataNTSenderStreamConfigurationDefault() {
                    @Override public String getQosLibraryName() {
                        return "TestLibrary";
                    }
                    @Override public String getQosProfileName() {
                        return "TestProfile";
                    }
                    @Override public Optional<Logger> getLogger() {
                        return Optional.of(Logger.getLogger("TestLogger"));
                    }
                };

        assertConfiguration(null, configuration,
                            "TestLibrary",
                            "TestProfile",
                            0,
                            Logger.getLogger("TestLogger"));
    }

    /**
     * Test overriding QoS library name with the constructor that takes
     * one argument (domain ID).
     */
    @Test
    public void testOverride2() {
        BulkDataNTSenderStreamConfigurationDefault
            configuration = new BulkDataNTSenderStreamConfigurationDefault(5) {
                    @Override public String getQosLibraryName() {
                        return "TestLibrary2";
                    }
                    @Override public String getQosProfileName() {
                        return "TestProfile2";
                    }
                    @Override public Optional<Logger> getLogger() {
                        return Optional.of(Logger.getLogger("TestLogger2"));
                    }
                };

        assertConfiguration(null, configuration,
                            "TestLibrary2",
                            "TestProfile2",
                            5,
                            Logger.getLogger("TestLogger2"));
    }

    /**
     * Test empty logger.
     */
    @Test
    public void testEmptyLogger() throws IllegalDomainIdException {
        BulkDataNTGlobalConfiguration.setenv("ACS_INSTANCE", "9");

        BulkDataNTSenderStreamConfigurationDefault
            configuration = new BulkDataNTSenderStreamConfigurationDefault() {
                    @Override public Optional<Logger> getLogger() {
                        return Optional.ofNullable(null);
                    }
                };

        assertEquals("BulkDataQoSLibrary",
                     configuration.getQosLibraryName());
        assertEquals("SenderStreamDefaultQosProfile",
                     configuration.getQosProfileName());
        assertEquals(9, configuration.getDomainId());
        assertFalse(configuration.getLogger().isPresent());
    }

    public void assertConfiguration(String message,
                                    BulkDataNTSenderStreamConfiguration conf,
                                    String expectedLibrary,
                                    String expectedProfile,
                                    int expectedDomainId,
                                    Logger expectedLogger) {
        assertEquals(message,
                     expectedLibrary,
                     conf.getQosLibraryName());
        assertEquals(message,
                     expectedProfile,
                     conf.getQosProfileName());
        assertEquals(message, expectedDomainId, conf.getDomainId());
        assertEquals(message,
                     expectedLogger,
                     conf.getLogger().get());
    }
}
