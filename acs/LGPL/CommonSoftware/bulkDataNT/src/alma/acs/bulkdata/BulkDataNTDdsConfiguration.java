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

import java.util.Optional;
import java.util.logging.Logger;

/**
 * This interface defines the methods to obtain configuration values
 * that are common to Stream and Flow.
 *
 * @author Takashi Nakamoto
 */
interface BulkDataNTDdsConfiguration {
    /**
     * Returns QoS XML library name.
     * @return QoS XML library name. It must be neither null nor empty.
     */
    public String getQosLibraryName();

    /**
     * Returns QoS XML profile name.
     * @return QoS XML profile name. It must be neither null nor empty.
     */
    public String getQosProfileName();

    /**
     * Returns the logger for logging the activity of the Stream or Flow.
     * This can be empty if you do not want to log messages.
     *
     * @return Logger to which Stream or Flow has to output log messages.
     */
    public Optional<Logger> getLogger();
}

