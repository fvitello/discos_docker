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

import org.apache.commons.cli.ParseException;

/**
 * This exception represents an error that happens when a given argument
 * string does not represent an integer value.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
class NotIntegerArgumentException extends ParseException {

    /**
     * This constructor instantiates NonIntegerArgumentException with
     * the given argument string and the given option name.
     *
     * @param arg An argument string that a parser judged as not an integer.
     * @param opt Option name.
     */
    public NotIntegerArgumentException(String arg, String opt) {
        super("The given argument \"" + arg + "\" for -" + opt +
              " option is not a 32-bit signed integer.");
    }

    /**
     * This constructor instantiates NonIntegerArgumentException with
     * the given argument string.
     *
     * @param arg An argument string that a parser judged as not an integer.
     * @param opt Option name.
     */
    public NotIntegerArgumentException(String arg) {
        super("The given argument \"" + arg +
              "\" is not a 32-bit signed integer.");
    }
}
