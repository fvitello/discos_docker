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

import org.apache.commons.lang.StringEscapeUtils;

/**
 * This exception indicates that the given Flow name does not fulfill
 * the criteria described in {@link BulkDataNTSenderFlow#getName()}.
 *
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class InvalidFlowNameException extends Exception {
    /**
     * This constructor instantiates InvalidFlowNameException with the
     * given Flow name that was considered as invalid and the reason
     * why it was considered as invalid.
     *
     * @param name Flow name that was considered as invalid.
     * @param reason Description about why the Flow name was considered
     *               as invalid.
     */
    public InvalidFlowNameException(String name, String reason) {
        super(String.format("Flow name \"%s\" is invalid. %s",
                            StringEscapeUtils.escapeJava(name),
                            reason));

        // StringEscapeUtils.escapeJava() is used to avoid any confusion
        // that may be caused by non-printable characters like backspace,
        // or non-ASCII characters.
    }
}
