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

import org.apache.commons.lang.Validate;

/**
 * This exception indicates that the string value provided as the Domain ID
 * is not valid.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class IllegalDomainIdException extends Exception {
    private final String value;

    /**
     * Instantiate this exception with the given message and the value
     * that were considered as invalid.
     *
     * @param msg Detailed message of this exception. Explain where that value
     *            was taken from, and why it is considered as invalid.
     * @param value The string representation of the value that was considered
     *              as invalid. It must not be null.
     *
     * @throws IllegalArgumentException
     * if {@code value} is null.
     */
    public IllegalDomainIdException(String msg, String value) {
        super(msg);
        this.value = value;

        Validate.notNull(value);
    }

    /**
     * This method returns the string representation of the value that
     * was considered as invalid for the Domain ID.
     *
     * @return The string representation of the value that was considered as
     *         invalid for the Domain ID.
     */
    public String getValue() {
        return value;
    }
}
