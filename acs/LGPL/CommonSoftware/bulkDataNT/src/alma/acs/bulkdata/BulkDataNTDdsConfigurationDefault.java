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

/**
 * The getter methods of this class return the default configuration
 * values that are common to both Stream and Flow.
 *
 * @author Takashi Nakamoto
 */
public abstract class BulkDataNTDdsConfigurationDefault
    implements BulkDataNTDdsConfiguration {
    /**
     * This method returns the default QoS XML library name for BulkData:
     * {@value #DEFAULT_QOS_LIBRARY_NAME}
     *
     * @return The default QoS XML library name for BulkData.
     */
    @Override public String getQosLibraryName() { return DEFAULT_QOS_LIBRARY_NAME; }

    /**
     * The default QoS XML library name for BulkData.
     */
    protected static final String
        DEFAULT_QOS_LIBRARY_NAME = "BulkDataQoSLibrary";
}
