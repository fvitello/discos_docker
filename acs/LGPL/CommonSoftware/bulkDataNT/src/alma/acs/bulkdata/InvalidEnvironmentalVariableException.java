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
 * This exception is thrown if a certain environmental variable contains
 * an invalid value, or if it is not defined.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class InvalidEnvironmentalVariableException extends Exception {
    private final String variableName;
    private final String value;

    /**
     * This exception instantiates this exception with the given message,
     * the variable name and the value.
     *
     * @param msg Detailed message of this exception.
     * @param variableName The name of the environmental variable.
     *                     Must not be null.
     * @param value The value in the environmental variable. Specify null
     *              if the variable is not defined.
     */
    public InvalidEnvironmentalVariableException(String msg,
                                                 String variableName,
                                                 String value) {
        super(msg);

        Validate.notNull(variableName);

        this.variableName = variableName;
        this.value = value;
    }

    /**
     * This method returns the name of the environmental variable.
     *
     * @return The name of the environmental variable.
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * This method returns the value in the environmental variable. Null
     * is returned if the variable is not defined.
     *
     * @return The value in the environmental variable, or null if it is
     *         not defined.
     */
    public String getValue() {
        return value;
    }
}
