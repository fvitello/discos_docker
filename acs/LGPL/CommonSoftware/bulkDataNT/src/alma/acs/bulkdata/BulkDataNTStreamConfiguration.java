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
 * This interface defines the methods to obtain the configuration values
 * to create a new {@link BulkDataNTStream}.
 *
 * @author Takashi Nakamoto
 */
interface BulkDataNTStreamConfiguration
    extends BulkDataNTDdsConfiguration {
    /**
     * This method returns the domain ID that the Stream will belong to.
     *
     * <p>
     * Domain ID of the Receiver and the Sender must be the same to 
     * communicate with each other.
     * 
     * @return The Domain ID that the Stream will belong to.
     */
    public int getDomainId();
}

