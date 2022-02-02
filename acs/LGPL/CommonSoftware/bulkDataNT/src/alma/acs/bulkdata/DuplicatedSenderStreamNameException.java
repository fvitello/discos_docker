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
 * This exception is thrown when there is a Sender Stream with the same name
 * already exists.
 */
@SuppressWarnings("serial")
public class DuplicatedSenderStreamNameException extends Exception {
    private final BulkDataNTSenderStream stream;

    /**
     * Constructs this exception with the Sender Stream that already exists.
     *
     * @param stream The Sender Stream that already exists. Must not be null.
     */
    public DuplicatedSenderStreamNameException(BulkDataNTSenderStream stream) {
        super("Stream \"" + stream.getName() + "\" already exists.");
        Validate.notNull(stream);
        this.stream = stream;
    }

    /**
     * Return the Sender Stream that already exists.
     *
     * @return The Sender Stream that already exists.
     */
    public BulkDataNTSenderStream getSenderStream() {
        return stream;
    }

    /**
     * The name of the Sender Stream that already exists.
     *
     * @return The name of the Sender Stream that already exists.
     */
    public String getName() {
        return stream.getName();
    }
}
