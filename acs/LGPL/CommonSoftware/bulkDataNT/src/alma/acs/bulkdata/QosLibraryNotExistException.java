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

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

/**
 * This method is thrown if the specified QoS library does not exist.
 * 
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class QosLibraryNotExistException extends Exception {
    private final String libraryName;
    private final Set<String> availableLibraryNames;

    /**
     * Instantiate this exception with the given library name that did not
     * exist, and the names of available library names.
     *
     * @param libraryName The library name that did not exist. Must not be null.
     * @param availableLibraryNames The set of available library names.
     *                              Must not be null.
     */
    public QosLibraryNotExistException(String libraryName,
                                       Set<String> availableLibraryNames) {
        super();

        Validate.notNull(libraryName);
        Validate.notNull(availableLibraryNames);

        this.libraryName = libraryName;
        this.availableLibraryNames = availableLibraryNames;
    }

    /**
     * Returns the library name that did not exist.
     *
     * @return The library name that did not exist. Never null.
     */
    public String getLibraryName() {
        return libraryName;
    }

    /**
     * Returns the array of the available library names.
     *
     * @return The array of the available library names. Never null.
     */
    public Set<String> getAvailableLibraryNames() {
        return availableLibraryNames;
    }


    @Override
    public String getMessage() {
        return String.format("QoS library \"%s\" does not exist. " +
                             "Available libraries are: %s.",
                             libraryName,
                             availableLibraryNames.stream()
                             .collect(Collectors.joining(", ")));
    }
}
