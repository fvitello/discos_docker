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
 * This exception indicates that the given configuration instance contains
 * one or more illegal (e.g. out of range) configuration values.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class IllegalConfigurationException extends Exception {

    /**
     * This constructor instantiates IllegalConfigurationException with
     * the given detailed message. The message shall contain the information
     * about which configuration is illegal, and why.
     *
     * @param msg Detailed message.
     */
    public IllegalConfigurationException(String msg) {
        super(msg);
    }
}
