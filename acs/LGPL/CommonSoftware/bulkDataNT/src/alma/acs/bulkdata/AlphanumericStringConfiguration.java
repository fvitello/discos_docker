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

import org.apache.commons.lang.StringUtils;

/**
 * This class represents one alphanumeric string configuration. It ensures
 * that the configuration value only contains ASCII alphanumeric characters
 * (a-z, A-Z, 0-9). It also ensures that the configuration value
 * is not null or an empty string.
 *
 * @author Takashi Nakamoto
 */
class AlphanumericStringConfiguration extends StringConfiguration {
    /**
     * This constructor defines one alphanumeric string configuration
     * with the given default value.
     *
     * @param defaultValue Default value of this configuration. It must be
     *                     not be null or an empty string. It also must
     *                     consist of only ASCII alphanumeric characters.
     * @param argName Argument name that is used when printing the help.
     *                It must not be null or an empty string.
     * @param description Description of this option for printing the help.
     *                    It must not be null.
     * @param optStr Option string (or character) to change this configuration
     *               in the command line option. It must not be null or
     *               an empty string.
     *
     * @throw IllegalArgumentException if the given arguments do not fulfill
     *                                 the prescribed preconditions.
     */
    public AlphanumericStringConfiguration(String defaultValue,
                                           String argName,
                                           String description,
                                           String optStr) {
        super(defaultValue, argName, description, optStr);
    }

    @Override
    protected ValidationResult validate(String str) {
        String msg;
        if (!StringUtils.isAlphanumeric(str)) {
            msg = "Must not contain non-alphanumeric character(s).";
            return new ValidationResult(false, msg);
        } else if (!StringUtils.isAsciiPrintable(str)) {
            msg = "Must not contain non-ASCII-printable character(s).";
            return new ValidationResult(false, msg);
        } else {
            return new ValidationResult(true, null);
        }
    }
}
