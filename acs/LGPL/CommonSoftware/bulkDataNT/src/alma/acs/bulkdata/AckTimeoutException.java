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

import com.rti.dds.infrastructure.RETCODE_ERROR;

/**
 * This exception indicates that the operation timed out before receiving
 * all the acknowledgments of the data that have been previously sent
 * from all the connected Receivers.
 * 
 * @author Takashi Nakamoto
 */
@SuppressWarnings("serial")
public class AckTimeoutException extends DdsException {
    /**
     * This constructor instantiates this exception with the given message,
     * the throwable, the stream and the flow.
     *
     * @param msg The detailed message about why this exception happens.
     * @param cause The instance of RETCODE_ERROR or its descendant thrown
     *              by the underlying DDS library.
     * @param stream The Stream associated with this exception. This can be
     *               null if this exception is not associated with a certain
     *               Stream.
     * @param flow The Flow associated with this exception. This can be null
     *             if this exception is not associated with a certain Flow.
     */
    public AckTimeoutException(String msg,
                               RETCODE_ERROR cause,
                               BulkDataNTStream stream,
                               BulkDataNTFlow flow) {
        super(msg, cause, stream, flow);
    }
}
