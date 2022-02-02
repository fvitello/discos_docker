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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This JUnit test checks the behavior of {@link BulkDataNTDdsLogVerbosity}.
 *
 * @author Takashi Nakamoto
 */
public class BulkDataNTDdsLogVerbosityTest {
    /**
     * Checks if the log levels are in the specified range.
     */
    @Test
    public void testLogLevelRange() {
        for (BulkDataNTDdsLogVerbosity v: BulkDataNTDdsLogVerbosity.values()) {
            assertTrue(BulkDataNTDdsLogVerbosity.MIN_LOG_LEVEL <= v.getLogLevel() &&
                       v.getLogLevel() <= BulkDataNTDdsLogVerbosity.MAX_LOG_LEVEL);
        }
    }

    /**
     * Check the uniqueness of log levels.
     */
    @Test
    public void testLogLevelUniqueness() {
        Set<Integer> seen = new HashSet<>();

        String duplicated = 
            Arrays.stream(BulkDataNTDdsLogVerbosity.values())
            .filter(v -> seen.add(v.getLogLevel()) == false)
            .map(v -> String.valueOf(v.getLogLevel()))
            .collect(Collectors.joining(", "));
        assertTrue("Found duplicated log levels: " + duplicated,
                   duplicated.isEmpty());
    }
}
