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
 * This exception represents an error that happens when the same option
 * appears in the command line twice or more.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
class MoreThanOneOptionException extends ParseException {

    /**
     * This constructor instantiates MoreThanOneArgumentException with
     * the given option name.
     *
     * @param opt Option name.
     */
    public MoreThanOneOptionException(String opt) {
        super("The option -" + opt + " appears twice or more. " +
              "You can specify only one.");
    }
}
