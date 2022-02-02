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
 * This method is thrown if the specified QoS profile does not exist.
 * 
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class QosProfileNotExistException extends Exception {
    private final String profileName;
    private final Set<String> availableProfileNames;
    private final String libraryName;

    /**
     * Instantiate this exception with the given profile name that did not
     * exist, and the names of available profile names.
     *
     * @param profileName The profile name that did not exist.
     *                    Must not be null. 
     * @param availableProfileNames The set of available profile names.
     *                              Must not be null.
     * @param libraryName The name of the library that did not contain the
     *                    profile name. Must not be null.
     */
    public QosProfileNotExistException(String profileName,
                                       Set<String> availableProfileNames,
                                       String libraryName) {
        super();

        Validate.notNull(profileName);
        Validate.notNull(availableProfileNames);
        Validate.notNull(libraryName);

        this.profileName = profileName;
        this.availableProfileNames = availableProfileNames;
        this.libraryName = libraryName;
    }

    /**
     * Returns the profile name that did not exist.
     *
     * @return The profile name that did not exist. Never null.
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns the array of the available profile names.
     *
     * @return The array of the available profile names. Never null.
     */
    public Set<String> getAvailableProfileNames() {
        return availableProfileNames;
    }

    /**
     * Returns the library name that did not contain the profile name 
     * returned by {@link #getProfileName()}.
     *
     * @return The profile name that did not contain the profile name.
     */
    public String getLibraryName() {
        return libraryName;
    }

    @Override
    public String getMessage() {
        return String.format("QoS profile \"%s\" does not exist " +
                             "in the library \"%s\". " +
                             "Available profiles are: %s.",
                             profileName,
                             libraryName,
                             availableProfileNames.stream()
                             .collect(Collectors.joining(", ")));
    }
}
