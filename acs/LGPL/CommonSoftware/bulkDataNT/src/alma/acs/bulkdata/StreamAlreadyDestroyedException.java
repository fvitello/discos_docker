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
 * This exception is thrown when the application tries to do something with
 * a destroyed Stream.
 * 
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class StreamAlreadyDestroyedException extends Exception {
    private final BulkDataNTStream stream;

    /**
     * Constructs this exception with the given detailed message and the
     * reference to the stream that has been already destroyed.
     *
     * @param msg The detailed message of this exception.
     * @param stream The Stream that has been already destroyed.
     */
    public StreamAlreadyDestroyedException(String msg,
                                           BulkDataNTStream stream) {
        super(msg);

        Validate.notNull(stream);

        this.stream = stream;
    }

    /**
     * The instance of Stream that was already destroyed.
     *
     * @return The stream that was already destroyed.
     */
    public BulkDataNTStream getStream() {
        return stream;
    }
}
